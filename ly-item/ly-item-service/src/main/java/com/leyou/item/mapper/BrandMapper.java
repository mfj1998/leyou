package com.leyou.item.mapper;

import com.leyou.item.entity.Brand;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand>, SelectByIdListMapper<Brand,Long> {

    /**
     * 添加品牌id和对应的多个category的id
     * @param id
     * @param cids
     * @return
     */
    int insertBrandIdAndCategoryId(@Param("id") Long id, @Param("cids") List<Long> cids);

    /**
     * 根据id进行修改
     * @param brand
     * @return
     */
    int updateBrand(Brand brand);

    /**
     * 根据category的id到中间表查询出来对应的brand
     * @param cid
     * @return
     */
    List<Brand> queryBrandByCategoryId(@Param("cid") Long cid);
}
