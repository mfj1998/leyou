package com.leyou.auth.mapper;

import com.leyou.auth.entity.ApplicationInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface AuthMapper extends Mapper<ApplicationInfo> {

    /**
     * 中间表根据serverId查询出来对应的targetId
     * @param id
     * @return
     */
    List<Long> queryTargetIdList(@Param("sid") Long id);
}
