package com.leyou.item.service;

import com.leyou.item.dto.SpecGroupDTO;

import java.util.List;

public interface SpecGroupService {

    /**
     * 根据cid查询出来全部的规格组
     * @param cid
     * @return
     */
    List<SpecGroupDTO> querySpecGroupByCategoryId(Long cid);

    /**
     * 新增一个规格组
     * @param specGroupDTO
     */
    void saveSpecGGroup(SpecGroupDTO specGroupDTO);

    /**
     * 修改规格组名
     * @param group
     */
    void updateSpecGroup(SpecGroupDTO group);

    /**
     * 根据gid删除
     * @param gid
     */
    void deleteSpecGroup(Long gid);

    /**
     * 根据其规格组的id查询出组的对应信息以及规格项的集合
     * @param cid
     * @return
     */
    List<SpecGroupDTO> querySpecGroupOfParamsById(Long cid);

}
