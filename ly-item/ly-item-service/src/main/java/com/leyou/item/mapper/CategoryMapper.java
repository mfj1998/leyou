package com.leyou.item.mapper;

import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.Category;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CategoryMapper extends Mapper<Category>, IdListMapper<Category,Long> {

    /**
     * 根据品牌的id查询出来商品的分类
     * @param bid
     * @return
     */
    List<CategoryDTO> queryCategoryByBrandId(@Param("bid") Long bid);

    /**
     * 删除中间表关联
     * @param bid
     */
    int deleteBrandAndCategory(@Param("bid") Long bid);

    /**
     * 计算中间关系表的数量
     * @param bid
     * @return
     */
    int queryCount(@Param("bid") Long bid);

}
