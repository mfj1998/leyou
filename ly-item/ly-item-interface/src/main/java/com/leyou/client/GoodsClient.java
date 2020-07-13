package com.leyou.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(value = "item-service")
public interface GoodsClient {

    /**
     * 利用这个分页查询,查询出来全部的Goods对象的数据
     *
     * @param page
     * @param rows
     * @param key
     * @param saleable
     * @return
     */
    @RequestMapping("/spu/page")
    PageResult<SpuDTO> queryBrandPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "key", required = false) String key,       //这个参数为根据什么模糊查询
            @RequestParam(value = "saleable", required = false) Boolean saleable);

    /**
     * 根据spu的id查询出来对应的sku的集合
     *
     * @param id
     * @return
     */
    @GetMapping("/sku/of/spu")
    List<SkuDTO> querySkuById(@RequestParam("id") Long id);

    /**
     * 根据brand的id查询
     *
     * @param bid
     * @return
     */
    @GetMapping("/brand/queryBrand")
    BrandDTO queryBrandById(@RequestParam("bid") Long bid);

    /**
     * 根据category的id进行查询
     *
     * @param ids
     * @return
     */
    @GetMapping("/category/of/categorys")
    List<CategoryDTO> queryCategoryById(@RequestParam("ids") List<Long> ids);

    /**
     * 获取到该条件下的所有的规格参数
     *
     * @param gid       groupId
     * @param cid       categoryId
     * @param searching 是否用于搜索
     * @return
     */
    @GetMapping("/spec/params")
    List<SpecParamDTO> querySpecParamBySpecGroupId(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching);

    /**
     * 根据spuDetail查询出来对应的信息
     *
     * @param id
     * @return
     */
    @GetMapping("/spu/detail")
    SpuDetailDTO querySpuDetailById(@RequestParam("id") Long id);

    /**
     * 根据brand的id集合查询出来其对应的对象
     *
     * @param ids
     * @return
     */
    @GetMapping("/brand/list")
    List<BrandDTO> queryBrandByIds(@RequestParam("ids") List<Long> ids);

    /**
     * 根据spu的id去查询对应的SpuDTO对象,做好将spudetail和skus查询出来
     *
     * @param id
     * @return
     */
    @GetMapping("/spu/{id}")
    SpuDTO querySpuById(@PathVariable("id") Long id);

    /**
     * 根据商品分类的id查询出其对应的组，然后组再查询出其对应的规格项
     * @param cid
     * @return
     */
    @GetMapping("/spec/group/of/param")
    List<SpecGroupDTO> querySpecGroupOfParamsById(@RequestParam("id") Long cid);


    /**
     * 根据商品的上架状态查询商品
     * @param saleable
     * @return
     */
    @GetMapping("/spu/saleable/all")
    List<SpuDTO> querySpuBySaleable(
            @RequestParam(name = "saleable",defaultValue = "true") Boolean saleable);


    /**
     * 根据sku的id集合查询出其对应的sku集合，这个是购物车的功能
     * 前端的localStorage可以实现,为什么还要用这个呢？因为有时候后端的存活、价格商品状态会修改，重新查询作对比
     * @param ids
     * @return
     */
    @GetMapping("/sku/list")
    List<SkuDTO> querySkuByIdList(@RequestParam("ids") List<Long> ids);

    /**
     * 根据skuid集合和对应的Num集合修改库存
     * @param cartMap
     * @return
     */
    @PutMapping("/sku/stock")
    void updateSkuStock(@RequestBody Map<Long, Integer> cartMap);
}
