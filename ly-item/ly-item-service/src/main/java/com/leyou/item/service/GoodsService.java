package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;

import java.util.List;
import java.util.Map;

public interface GoodsService {

    /**
     * 根据条件进行分页的查询
     * @param page
     * @param rows
     * @param key
     * @param saleable
     * @return
     */
    PageResult<SpuDTO> queryBrandPage(Integer page, Integer rows, String key, Boolean saleable);

    /**
     * 保存商品的操作
     * @param spuDTO
     */
    void saveGoods(SpuDTO spuDTO);

    /**
     * 根据sid对spu表的saleable状态吗进行修改
     * @param sid
     * @param saleable
     */
    void updateGoodsSaleable(Long sid, Boolean saleable);

    /**
     * 根据id对SpuDetailDTO进行查询
     * @param id
     * @return
     */
    SpuDetailDTO querySpuDetailById(Long id);

    /**
     * 根据spu的id查询出来对应的List<SkuDTO>
     * @param id
     * @return
     */
    List<SkuDTO> querySkuById(Long id);

    /**
     * 修改spuDTO的属性信息
     * @param spuDTO
     */
    void updateGoods(SpuDTO spuDTO);

    /**
     * 根据sid进行删除的操作
     * @param sid
     */
    void deleteGoods(Long sid);

    /**
     * 根据一个cid3的id得到其两个父的cid
     * @param cid
     * @return
     */
    List<CategoryDTO> getClassification(Long cid);

    /**
     * 根据spu的id查询三个表
     * @param id
     * @return
     */
    SpuDTO querySpuById(Long id);

    /**
     * 根据上架的状态查询出全部的Spu
     * @param saleable
     * @return
     */
    List<SpuDTO> querySpuBySaleable(Boolean saleable);

    /**
     * 根据sku的id集合查询出其对应的sku集合
     * @param ids
     * @return
     */
    List<SkuDTO> querySkuByIdList(List<Long> ids);

    /**
     * 根据skuid集合和对应的Num集合修改库存
     * @param cartMap
     * @return
     */
    void updateSkuStock(Map<Long, Integer> cartMap);

}
