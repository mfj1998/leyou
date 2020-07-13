package com.leyou.search.service;

import com.leyou.common.vo.PageResult;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;

import java.util.List;
import java.util.Map;

public interface SearchService {



    /**
     * 使用elasticsearch进行分页的查询
     * @param searchRequest
     * @return
     */
    PageResult<GoodsDTO> queryPage(SearchRequest searchRequest);

    /**
     * 聚合分桶的分页查询
     * @param searchRequest
     * @return
     */
    Map<String, List<?>> queryFilter(SearchRequest searchRequest);
}
