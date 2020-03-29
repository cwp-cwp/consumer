package com.puzek.consumer.bean;

import com.puzek.consumer.utils.FormatClassInfo;

/**
 * 系统记录 Kafka 消费的 offset
 * Created by chen_wp on 2020-01-04.
 */
public class SystemOffset {

    private String key;
    private long offset;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return FormatClassInfo.format(this);
    }
}
