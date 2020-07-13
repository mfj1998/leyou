package com.leyou.item.controller;

import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父id查询出商品分类
     * @return
     */
    @RequestMapping("/of/parent")
    public ResponseEntity<List<CategoryDTO>> findByParentId(
            @RequestParam(name = "pid",defaultValue = "0") Long pid) {
        List<CategoryDTO> categoryDTOS = categoryService.findByParentId(pid);
        return ResponseEntity.ok(categoryDTOS);
    }

    //根据品牌信息查询商品分类，一对多的关系
    @GetMapping("/of/brand")
    public ResponseEntity<List<CategoryDTO>> queryCategoryByBrandId(@RequestParam("id") Long bid) {
        List<CategoryDTO> categoryDTOS = categoryService.queryCategoryByBrandId(bid);
        return ResponseEntity.ok(categoryDTOS);
    }

    /**
     * 根据category的id集合查询出来对应的集合结果集
     * @param ids
     * @return
     */
    @GetMapping("/of/categorys")
    public ResponseEntity<List<CategoryDTO>> queryCategoryById(@RequestParam("ids")List<Long> ids) {
        return ResponseEntity.ok(categoryService.queryCategorys(ids));
    }



}
