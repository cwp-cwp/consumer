package com.puzek.consumer.bean;

import com.puzek.consumer.utils.FormatClassInfo;

/**
 * 消息主题实体类
 * Created by chen_wp on 2019-09-20.
 */
public class KafkaTopic {

    private int id; // id自增
    private String topicLevel; // 主题级别
    private String topicName; // 主题名称

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTopicLevel() {
        return topicLevel;
    }

    public void setTopicLevel(String topicLevel) {
        this.topicLevel = topicLevel;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    @Override
    public String toString() {
        return FormatClassInfo.format(this);
    }
}
