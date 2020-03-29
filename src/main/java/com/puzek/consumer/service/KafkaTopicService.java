package com.puzek.consumer.service;

import com.puzek.consumer.bean.KafkaTopic;
import com.puzek.consumer.mapper.KafkaTopicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class KafkaTopicService {

    @Autowired
    private KafkaTopicMapper kafkaTopicMapper;

    public List<KafkaTopic> getAllThreeLevelTopic() {
        return this.kafkaTopicMapper.getAllThreeLevelTopic();
    }

}
