package com.leyou.item.service;

import com.leyou.item.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {

    /**
     * 根据父节点的id查询商品分类
     * @param pid
     * @return
     */
    List<CategoryDTO> findByParentId(Long pid);

    /**
     * 根据品牌的id信息查询出来商品的分类
     * @param bid
     * @return
     */
    List<CategoryDTO> queryCategoryByBrandId(Long bid);

    /**
     * 得到自己的多级菜单,获取到品牌的名字
     * @param categoryIds
     * @return
     */
    List<CategoryDTO> queryCategorys(List<Long> categoryIds);
}
