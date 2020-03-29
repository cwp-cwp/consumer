package com.puzek.consumer.mapper;

import com.puzek.consumer.bean.SystemOffset;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemOffsetMapper {

    Long readSystemOffset(@Param("key") String key);

    void initSystemOffset(@Param("key") String key, @Param("offset") long offset);

    void updateSystemOffset(@Param("key") String key, @Param("offset") long offset);

    SystemOffset getSystemOffsetByKey(@Param("key") String key);

}
