package com.leyou.item.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.SpecGroup;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.service.SpecGroupService;
import com.leyou.item.service.SpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpecGroupServiceImpl implements SpecGroupService {

    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamService specParamService;

    @Override
    public List<SpecGroupDTO> querySpecGroupByCategoryId(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> specGroups = specGroupMapper.select(specGroup);
        /*if (CollectionUtils.isEmpty(specGroups)) {
            throw new LyException(ExceptionEnum.QUERY_NOT_FOUND);
        }*/

        return BeanHelper.copyWithCollection(specGroups,SpecGroupDTO.class);
    }

    @Override
    public void saveSpecGGroup(SpecGroupDTO specGroupDTO) {
        SpecGroup specGroup = BeanHelper.copyProperties(specGroupDTO, SpecGroup.class);
        specGroup.setCreateTime(new Date());
        specGroup.setUpdateTime(new Date());

        //specGroupMapper.insert();                    该方法会插入所有的字段属性,没有值则插入null
        int count = specGroupMapper.insertSelective(specGroup);//该方法只插入你设置的属性
        if (count != 1) {
            throw new LyException(ExceptionEnum.DATA_SAVE_FAILURE);
        }
    }

    @Override
    public void updateSpecGroup(SpecGroupDTO group) {
        SpecGroup specGroup = BeanHelper.copyProperties(group, SpecGroup.class);
        specGroup.setUpdateTime(new Date());

        int count = specGroupMapper.updateByPrimaryKeySelective(specGroup);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_DATA_FAILURE);
        }
    }

    @Override
    public void deleteSpecGroup(Long gid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setId(gid);
        int count = specGroupMapper.deleteByPrimaryKey(specGroup);
        if (count != 1) {
            throw new LyException(ExceptionEnum.DATA_DELETE_FAILURE);
        }
    }

    @Override
    public List<SpecGroupDTO> querySpecGroupOfParamsById(Long cid) {
        List<SpecGroupDTO> specGroupDTOS = querySpecGroupByCategoryId(cid);
        List<SpecParamDTO> specParamDTOS = specParamService.querySpecParamBySpecGroupId(null,cid,null);

        //将查询出来的groupId进行分组
        Map<Long, List<SpecParamDTO>> paramMap = specParamDTOS.stream()
                .collect(Collectors.groupingBy(SpecParamDTO::getGroupId));

        //根据groupId进行取值赋值
        specGroupDTOS.forEach(specGroupDTO -> {
            specGroupDTO.setParams(paramMap.get(specGroupDTO.getId()));
        });
        return specGroupDTOS;
    }
}
