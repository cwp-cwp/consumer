package com.puzek.consumer.kafka;

import com.puzek.consumer.bean.KafkaTopic;
import com.puzek.consumer.bean.SystemOffset;
import com.puzek.consumer.service.KafkaTopicService;
import com.puzek.consumer.service.SystemOffsetService;
import com.puzek.consumer.utils.JsonUtils;
import com.puzek.consumer.utils.ReadConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

@Component
public class MessageTransfer implements CommandLineRunner {

    private final static Logger LOG = LoggerFactory.getLogger(MessageTransfer.class);

    private final Object LOCK = new Object();

    private List<String> imageTopicList = new ArrayList<>(); // 图片主题

    private boolean imageFlag = true; // 控制循环，有新增主题时，重新订阅新图片主题

    private static ExecutorService threadPool = new ThreadPoolExecutor(
            2,
            Runtime.getRuntime().availableProcessors(),
            Integer.MAX_VALUE,
            TimeUnit.HOURS,
            new LinkedBlockingDeque<>(3),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy());


    private final KafkaTopicService kafkaTopicService;
    private final SystemOffsetService systemOffsetService;

    @Autowired
    public MessageTransfer(KafkaTopicService kafkaTopicService, SystemOffsetService systemOffsetService) {
        this.kafkaTopicService = kafkaTopicService;
        this.systemOffsetService = systemOffsetService;
    }

    @Override
    public void run(String... args) {
        this.getThreeLevelTopic();
        new Thread(() -> {
            long threadId = Thread.currentThread().getId();
            LOG.info("云端接收图片消息线程 threadId: " + threadId);
            this.receiveImageData();
        }).start();
    }

    /**
     * 接收图片
     */
    private void receiveImageData() {
        KafkaConsumer<String, byte[]> imageConsumer = this.getImageKafkaConsumer(); // 创建图片消费者
        imageConsumer.subscribe(imageTopicList); // 消费者订阅的topic, 可同时订阅多个
        LOG.info("成功订阅图片主题 ===> " + JsonUtils.objectToJson(imageTopicList));
        while (true) {
            ConsumerRecords<String, byte[]> records = imageConsumer.poll(100); // 读取数据，读取超时时间为100ms
            for (ConsumerRecord<String, byte[]> record : records) {
                if (this.checkOffset(record.topic(), record.partition(), record.offset())) {
                    continue;
                }
                threadPool.execute(() -> {
                    this.handleImage(record);
                });
            }
            if (!imageFlag) {
                if (imageConsumer != null) {
                    imageConsumer.close();
                    imageConsumer = this.getImageKafkaConsumer();
                    imageConsumer.subscribe(imageTopicList);
                    LOG.info("重新订阅的图片主题 = " + JsonUtils.objectToJson(imageTopicList));
                    imageFlag = true;
                }
            }
        }
    }

    private boolean checkOffset(String topic, int partition, long offset) {
        // TODO 读取系统里面记录消费的位置，避免重复消费数据
        Long systemOffset = this.systemOffsetService.readSystemOffset(topic + "-" + partition);
        if (systemOffset == null) {
            SystemOffset sysOffset = this.systemOffsetService.getSystemOffsetByKey(topic + "-" + partition);
            if (sysOffset == null) {
                this.systemOffsetService.initSystemOffset(topic + "-" + partition, 0);
            }
            systemOffset = 0L;
        }
        if (offset < systemOffset) {
            LOG.info("接收到的 topic_partition_offset = " + topic + "_" + partition + "_" + offset + " 小于系统记录的 offset, systemOffset = " + systemOffset + "\t continue...");
            return true;
        }
        this.systemOffsetService.updateSystemOffset(topic + "-" + partition, offset + 1);
        return false;
    }

    private void handleImage(ConsumerRecord<String, byte[]> record) {
        byte[] data = record.value();
        LOG.info(Thread.currentThread().getName() + " 接收到的图片主题 = " + record.topic() + " \t 分区 = " + record.partition() + "\t offset = " + record.offset() + "\t 接收到的图片长度 = " + data.length + "\t 图片名 = " + record.key());
        ByteArrayInputStream byteArrayInputStream = null;
        BufferedImage bufferedImage;
        OutputStream outputStream = null;
        String imagePath;
        try {
            byteArrayInputStream = new ByteArrayInputStream(data);
            bufferedImage = ImageIO.read(byteArrayInputStream);
            //TODO 和巡检车约好的 key:  B6666@20190921194334007@-2687_84422_181935_1568634238676_35_0_0_0_0_无_前_1.jpg
            String[] split = record.key().split("@");
            imagePath = this.getImagePath() + "/" + split[0] + "/" + split[1];
            LOG.info(Thread.currentThread().getName() + " 图片路径 imagePath = " + imagePath);
            File file;
            if (split[2].split("/").length > 1) {
                file = new File(imagePath + "/" + split[2].split("/")[0]);
            } else {
                file = new File(imagePath);
            }
            if (!file.exists()) {
                file.mkdirs();//创建文件夹
            }
            outputStream = new FileOutputStream(imagePath + "/" + split[2]);
            if (bufferedImage != null && outputStream != null) {
                ImageIO.write(bufferedImage, "jpg", outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.close(byteArrayInputStream, outputStream);
        }
    }

    private void close(ByteArrayInputStream byteArrayInputStream, OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (byteArrayInputStream != null) {
            try {
                byteArrayInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 创建图片消费者
     */
    private KafkaConsumer<String, byte[]> getImageKafkaConsumer() {
        Properties props = new Properties();
        // 定义kakfa 服务的地址，不需要将所有broker指定上
        props.put("bootstrap.servers", this.getKafkaIpAndPort());
//        props.put("bootstrap.servers", "192.168.154.129:9092");
        // 制定consumer group
        props.put("group.id", "test");
        // 是否自动确认offset
        props.put("enable.auto.commit", "true");
        // 自动确认offset的时间间隔
        props.put("auto.commit.interval.ms", "1000");
        // key的序列化类
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        // value的序列化类
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        // 定义consumer
        return new KafkaConsumer<>(props);
    }

    private String getKafkaIpAndPort() {
        String kafkaIpPort = ReadConfig.readProperties("kafkaIpPort"); // Kafka 服务地址
        if (kafkaIpPort == null || "".equals(kafkaIpPort.replace(" ", ""))) {
            kafkaIpPort = "127.0.0.1:9092";
        }
        return kafkaIpPort.replace(" ", "");
    }

    private String getImagePath() {
        String imagePath = ReadConfig.readProperties("imagePath"); // 图片保存路径
        if (imagePath == null || "".equals(imagePath.replace(" ", ""))) {
            imagePath = "/opt/images";
        }
        return imagePath.replace(" ", "");
    }

    /**
     * 定时从数据库中获取所有三级主题
     */
    @Scheduled(fixedRate = 3000)
    private void getThreeLevelTopic() {
        synchronized (LOCK) {
            List<KafkaTopic> treeLevelTopic = this.kafkaTopicService.getAllThreeLevelTopic();
            for (KafkaTopic t : treeLevelTopic) {
                // TODO 图片主题统一命名 request-image-车牌号 (例如 request-image-B6666)
                String imageTopic = t.getTopicName().split("-")[0] + "-image-" + t.getTopicName().split("-")[1];
                if (this.imageTopicList.contains(imageTopic)) {
                    continue;
                }
                this.imageTopicList.add(imageTopic);
                imageFlag = false; // 发现有新增主题，改变 flag, 重新订阅主题
            }
        }
    }
}
