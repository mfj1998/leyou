package com.leyou.item.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.SpecParam;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.service.SpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SpecParamServiceImpl implements SpecParamService {

    @Autowired
    private SpecParamMapper specParamMapper;

    @Override
    public List<SpecParamDTO> querySpecParamBySpecGroupId(Long gid,Long cid,Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        List<SpecParam> specParams = specParamMapper.select(specParam);
        /*if (CollectionUtils.isEmpty(specParams)) {
            throw new LyException(ExceptionEnum.QUERY_NOT_FOUND);
        }*/

        return BeanHelper.copyWithCollection(specParams,SpecParamDTO.class);
    }

    @Override
    public void saveSpecParam(SpecParamDTO specParamDTO) {
        //1、基本的类型的转换以及对日期的赋值
        SpecParam specParam = BeanHelper.copyProperties(specParamDTO, SpecParam.class);
        specParam.setCreateTime(new Date());
        specParam.setUpdateTime(new Date());

        //2、直接添加
        int count = specParamMapper.insertSelective(specParam);
        if (count != 1) {
            throw new LyException(ExceptionEnum.DATA_SAVE_FAILURE);
        }

    }

    @Override
    public void updateSpecParam(SpecParamDTO specParamDTO) {
        //1、基本的类型的转换以及对日期的赋值
        SpecParam specParam = BeanHelper.copyProperties(specParamDTO, SpecParam.class);
        specParam.setUpdateTime(new Date());

        //2、修改
        int count = specParamMapper.updateByPrimaryKeySelective(specParam);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_DATA_FAILURE);
        }
    }

    @Override
    public void deleteSpecParam(Long pid) {
        SpecParam specParam = new SpecParam();
        specParam.setId(pid);
        int count = specParamMapper.deleteByPrimaryKey(specParam);
        if (count != 1) {
            throw new LyException(ExceptionEnum.DATA_DELETE_FAILURE);
        }
    }
}
