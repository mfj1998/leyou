package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 对于Spu的分页条件查询
     *
     * @param page
     * @param rows
     * @param key      模糊查询的条件
     * @param saleable 上架或者下架
     * @return
     */
    @RequestMapping("/spu/page")
    public ResponseEntity<PageResult<SpuDTO>> queryBrandPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "key", required = false) String key,       //这个参数为根据什么模糊查询
            @RequestParam(value = "saleable", required = false) Boolean saleable) {
        PageResult<SpuDTO> pageResult = goodsService.queryBrandPage(page, rows, key, saleable);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 保存一个商品的信息,除了基本类型的对象外,其中还包含两个对象(1、集合对象   2、普通对象)
     *
     * @param spuDTO
     * @return
     */
    @PostMapping("/goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuDTO spuDTO) {
        goodsService.saveGoods(spuDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 将商品下架
     *
     * @param sid
     * @param saleable
     * @return
     */
    @PutMapping("/spu/saleable")
    public ResponseEntity<Void> updateGoodsSaleable(
            @RequestParam("id") Long sid,
            @RequestParam("saleable") Boolean saleable) {
        goodsService.updateGoodsSaleable(sid, saleable);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据spuDetail查询出来对应的信息
     *
     * @param id
     * @return
     */
    @GetMapping("/spu/detail")
    public ResponseEntity<SpuDetailDTO> querySpuDetailById(@RequestParam("id") Long id) {
        SpuDetailDTO spuDetailDTO = goodsService.querySpuDetailById(id);
        return ResponseEntity.ok(spuDetailDTO);
    }

    /**
     * 根据spu的id查询出来对应的List<SkuDTO>
     *
     * @param id
     * @return
     */
    @GetMapping("/sku/of/spu")
    public ResponseEntity<List<SkuDTO>> querySkuById(@RequestParam("id") Long id) {
        List<SkuDTO> skuDTOS = goodsService.querySkuById(id);
        return ResponseEntity.ok(skuDTOS);
    }

    /**
     * 修改商品的信息,其中有两个参数需要注意：SpuDetailDTO、List<SkuDTO>
     *
     * @param spuDTO
     * @return
     */
    @PutMapping("/goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuDTO spuDTO) {
        goodsService.updateGoods(spuDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据spu的id进行商品删除的操作
     *
     * @param sid
     * @return
     */
    @DeleteMapping("/goods/delete")
    public ResponseEntity<Void> deleteGoods(@RequestParam("id") Long sid) {
        goodsService.deleteGoods(sid);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * 根据在spu表中cid3的值,得到他的两个父标题(三个对象的集合)
     *
     * @param cid
     * @return
     */
    @GetMapping("/cid3")
    public ResponseEntity<List<CategoryDTO>> getClassification(@RequestParam("cid") Long cid) {
        List<CategoryDTO> categoryDTOS = goodsService.getClassification(cid);
        return ResponseEntity.ok(categoryDTOS);
    }

    /**
     * 根据spu的id去查询对应的SpuDTO对象,做好将spudetail和skus查询出来
     * @param id
     * @return
     */
    @GetMapping("/spu/{id}")
    public ResponseEntity<SpuDTO> querySpuById(@PathVariable("id") Long id){
        SpuDTO spuDTO = goodsService.querySpuById(id);
        return ResponseEntity.ok(spuDTO);
    }

    /**
     * 根据上架的状态查询出全部的Spu
     * @param saleable
     * @return
     */
    @GetMapping("/spu/saleable/all")
    public ResponseEntity<List<SpuDTO>> querySpuBySaleable(
            @RequestParam(name = "saleable",defaultValue = "true") Boolean saleable) {
        List<SpuDTO> spuDTOS = goodsService.querySpuBySaleable(saleable);
        return ResponseEntity.ok(spuDTOS);
    }

    /**
     * 根据sku的id集合查询出其对应的sku集合，这个是购物车的功能
     * 前端的localStorage可以实现,为什么还要用这个呢？因为有时候后端的存活、价格商品状态会修改，重新查询作对比
     * @param ids
     * @return
     */
    @GetMapping("/sku/list")
    public ResponseEntity<List<SkuDTO>> querySkuByIdList(@RequestParam("ids") List<Long> ids) {
        List<SkuDTO> skuDTOList = goodsService.querySkuByIdList(ids);
        return ResponseEntity.ok(skuDTOList);
    }

    /**
     * 根据skuid集合和对应的Num集合修改库存
     * @param cartMap
     * @return
     */
    @PutMapping("/sku/stock")
    public ResponseEntity<Void> updateSkuStock(@RequestBody Map<Long, Integer> cartMap) {
        goodsService.updateSkuStock(cartMap);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


}
