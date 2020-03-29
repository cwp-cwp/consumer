package com.puzek.consumer.utils;

import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 读取配置文件
 * Created by chen_wp on 2019-09-20.
 */
public class ReadConfig {

    private static Properties properties;

    static {
        properties = getResource();
    }

    private static Properties getResource() {
        Properties properties = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        try {
            String path = ResourceUtils.getFile("classpath:").getPath() + "/conf/server.properties";
            inputStream = new FileInputStream(path);
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            properties = new Properties();
            properties.load(inputStreamReader);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return properties;
    }

    public static String readProperties(String key) {
        if (properties == null) {
            properties = getResource();
        }
        return properties.getProperty(key);
    }
}
