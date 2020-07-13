package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.Brand;

import java.util.List;

public interface BrandService {

    /**
     * 根据条件进行分页查询
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @param key
     * @return
     */
    PageResult<BrandDTO> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key);

    /**
     * 保存一个品牌信息
     * @param brandDTO
     * @param ids
     */
    void saveBrand(BrandDTO brandDTO, List<Long> ids);

    /**
     * 修改品牌的基本信息
     * @param brandDTO
     * @param cids
     */
    void updateBrand(BrandDTO brandDTO, List<Long> cids);

    void deleteBrand(Long bid);

    /**
     * 根据id查询
     * @param brandId
     * @return
     */
    Brand queryById(Long brandId);

    /**
     * 根据category的id查询出来对应的brand集合
     * @param cid
     * @return
     */
    List<BrandDTO> queryBrandByCategoryId(Long cid);

    /**
     * 根据brandId进行查询
     * @param bid
     * @return
     */
    BrandDTO queryBrandById(Long bid);

    /**
     * 根据brand的id集合查询出来全部的信息
     * @param ids
     * @return
     */
    List<BrandDTO> queryBrandByIds(List<Long> ids);
}
