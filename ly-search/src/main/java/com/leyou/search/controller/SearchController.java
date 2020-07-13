package com.leyou.search.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 使用elasticsearch进行分页的查询
     * @param searchRequest
     * @return
     */
    @PostMapping("/page")
    public ResponseEntity<PageResult<GoodsDTO>> queryPage(@RequestBody SearchRequest searchRequest) {
        PageResult<GoodsDTO> pageResult = searchService.queryPage(searchRequest);
        return ResponseEntity.ok(pageResult);
    }

    /**
     *
     * @param searchRequest
     * @return
     */
    @PostMapping("/filter")
    public ResponseEntity<Map<String, List<?>>> queryFilter(@RequestBody SearchRequest searchRequest) {
        /*
            接受前端发送来的分页查询条件,然后根据这个条件进行聚合分桶,
            注意，是拥有该条件的分类、品牌进行聚合分桶;只需要得到桶信息,不需要spu信息
         */
        Map<String, List<?>> map = searchService.queryFilter(searchRequest);
        return ResponseEntity.ok(map);
    }
}
