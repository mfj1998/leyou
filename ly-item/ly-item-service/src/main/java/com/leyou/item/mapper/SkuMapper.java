package com.leyou.item.mapper;

import com.leyou.item.entity.Sku;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

public interface SkuMapper extends Mapper<Sku>, InsertListMapper<Sku>, SelectByIdListMapper<Sku,Long> {

    /**
     * 根据逻辑外键的id对其进行修改
     * @param sku
     * @return
     */
    int updateSkuBySpuId(Sku sku);

    /**
     * 根据sku的sup_id进行删除的操作
     * @param sid
     * @return
     */
    int deleteBySpuId(Long sid);

    /**
     * 根据skuId修改库存
     * @param skuId
     * @param num
     * @return
     */
    int updateSkuStock(@Param("skuId") Long skuId, @Param("num") Integer num);
}
