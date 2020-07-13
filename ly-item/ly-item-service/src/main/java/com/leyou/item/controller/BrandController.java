package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;
    /**
     * 分页查询,因为要查询的条件太多,所以要为其设置默认的值
     * @param page          当前页
     * @param rows          每页的数量
     * @param sortBy        排序的字段
     * @param desc          是否降序
     * @param search        查询的条件
     * @return
     */
    @GetMapping("/page")
    public ResponseEntity<PageResult<BrandDTO>> queryBrandByPage(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "rows", defaultValue = "5") Integer rows,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "desc", defaultValue = "false") Boolean desc,
            @RequestParam(name = "key", required = false) String search) {

        PageResult<BrandDTO> brandDTOList = brandService.queryBrandByPage(page,rows,sortBy,desc,search);
        return ResponseEntity.ok(brandDTOList);
    }


    /**
     * 保存一个品牌信息
     * @param brandDTO 品牌信息对象
     * @param ids      品牌的分类(因为是多层分类节点所以是一个集合),需要添加到sql中间表中
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(
            BrandDTO brandDTO,                      //在这里不能加@RequestBody注解,因为前端发送来的为数组,不是json数组
            @RequestParam("cids") List<Long> ids) { //@RequestBody的原理是为请求参数一个个配置@RequestParam注解,需要有setter、getter才行
        brandService.saveBrand(brandDTO,ids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据id进行商品品牌的修改
     * @param brandDTO
     * @param cids
     * @return[-
     */
    @PutMapping
    public ResponseEntity<Void> updateBrand(BrandDTO brandDTO,@RequestParam("cids")List<Long> cids) {
        brandService.updateBrand(brandDTO,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据品牌的id进行删除
     * @param bid
     * @return
     */
    @GetMapping("/deleteBrand")
    public ResponseEntity<Void> deleteBrand(@RequestParam("id") Long bid) {

        brandService.deleteBrand(bid);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据category的id进行查询
     * @param cid
     * @return
     */
    @GetMapping("/of/category")
    public ResponseEntity<List<BrandDTO>> queryBrandByCategoryId(@RequestParam("id") Long cid) {
        return ResponseEntity.ok(brandService.queryBrandByCategoryId(cid));
    }

    /**
     * 根据brand的id查询
     * @param bid
     * @return
     */
    @GetMapping("/queryBrand")
    public ResponseEntity<BrandDTO> queryBrandById(@RequestParam("bid") Long bid) {
        return ResponseEntity.ok(brandService.queryBrandById(bid));
    }

    /**
     * 根据brand的id集合查询出来其对应的对象
     * @param ids
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<BrandDTO>> queryBrandByIds(@RequestParam("ids")List<Long> ids) {
        return ResponseEntity.ok(brandService.queryBrandByIds(ids));
    }
}
