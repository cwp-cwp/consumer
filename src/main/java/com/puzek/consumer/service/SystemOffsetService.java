package com.puzek.consumer.service;

import com.puzek.consumer.bean.SystemOffset;
import com.puzek.consumer.mapper.SystemOffsetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SystemOffsetService {

    @Autowired
    private SystemOffsetMapper systemOffsetMapper;

    public Long readSystemOffset(String key) {
        return this.systemOffsetMapper.readSystemOffset(key);
    }

    public void initSystemOffset(String key, long offset) {
        this.systemOffsetMapper.initSystemOffset(key, offset);
    }

    public void updateSystemOffset(String key, long offset) {
        this.systemOffsetMapper.updateSystemOffset(key, offset);
    }

    public SystemOffset getSystemOffsetByKey(String key) {
        return this.systemOffsetMapper.getSystemOffsetByKey(key);
    }

}
