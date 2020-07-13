package com.leyou.item.service;

import com.leyou.item.dto.SpecParamDTO;

import java.util.List;

public interface SpecParamService {

    /**
     * 根据规格组的id查询出来规格项
     * @param gid
     * @return
     */
    List<SpecParamDTO> querySpecParamBySpecGroupId(Long gid,Long cid,Boolean searching);

    /**
     * 添加一个新的规格参数
     * @param specParamDTO
     */
    void saveSpecParam(SpecParamDTO specParamDTO);

    /**
     * 修改规格参数
     * @param specParamDTO
     */
    void updateSpecParam(SpecParamDTO specParamDTO);

    void deleteSpecParam(Long pid);

}
