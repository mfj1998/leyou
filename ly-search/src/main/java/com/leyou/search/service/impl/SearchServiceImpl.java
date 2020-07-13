package com.leyou.search.service.impl;

import com.leyou.client.GoodsClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Override
    public PageResult<GoodsDTO> queryPage(SearchRequest searchRequest) {

        String key = searchRequest.getKey();        //搜索的条件
        if (StringUtils.isBlank(key)) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //注意这里,因为后期要添加过滤条件的查询,这个方法是设置已有的过滤条件的
        queryBuilder.withQuery(getQueryBuilder(searchRequest));

        queryBuilder
                .withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));

        //分页查询
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage(), searchRequest.getSize()));

        //查询
        AggregatedPage<Goods> goodsAggregatedPage = esTemplate.queryForPage(queryBuilder.build(), Goods.class);


        //解析结果,得到查询的总记录数,总的页数,以及当前页的集合数据
        long totalCount = goodsAggregatedPage.getTotalElements();
        int totalPages = goodsAggregatedPage.getTotalPages();
        List<Goods> goodsContent = goodsAggregatedPage.getContent();
        List<GoodsDTO> goodsDTOS = BeanHelper.copyWithCollection(goodsContent, GoodsDTO.class);
        return new PageResult<>(totalCount, totalPages, goodsDTOS);
    }


    @Override
    public Map<String, List<?>> queryFilter(SearchRequest searchRequest) {

        //用来存储查询出来的categoryIds 和 brandIds
        Map<String, List<?>> filterMap = new LinkedHashMap<>();


        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        QueryBuilder queryBuilder = getQueryBuilder(searchRequest);
        nativeSearchQueryBuilder.withQuery(queryBuilder);

        //主要获得聚合分桶的信息,数据查询随便操作就行,谨记第二个参数不能为0
        nativeSearchQueryBuilder.withPageable(PageRequest.of(0, 1));

        //设置分桶的名字和进行分桶的字段
        String categoryAgg = "categoryAgg";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(categoryAgg).field("categoryId"));
        String brandAgg = "brandAgg";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(brandAgg).field("brandId"));
        AggregatedPage<Goods> result = esTemplate.queryForPage(nativeSearchQueryBuilder.build(), Goods.class);

        //获取到所有的分类聚合，每个聚合下有多个桶
        Aggregations aggregations = result.getAggregations();
        LongTerms categoryTerms = aggregations.get(categoryAgg);


        /*
            这里进行修改一下，应该获得的为它的id集合，
                    若结果集合的数量为1,则进行规格参数的聚合，因为只有为一个品牌分类的时候才能聚合规格参数,
                    多个分类无法确定它的规格参数,无法进行聚合过滤
         */
        List<Long> categoryIds = categoryResult(categoryTerms, filterMap);
        LongTerms brandTerms = aggregations.get(brandAgg);
        brandResult(brandTerms, filterMap);     //将该聚合下的所有桶的数据放入到map集合中


        //当分类的id集合数量为1时，查询其对应的规格参数
        if (!CollectionUtils.isEmpty(categoryIds) && categoryIds.size() == 1) {
            specResult(categoryIds.get(0),queryBuilder, filterMap);
        }

        return filterMap;
    }

    /**
     * 若查询到的goods分类只有一个的时候,显示这个商品的规格参数,需要聚合查询
     *
     * @param cid       //分类的id,这里用来查询对应的规格参数
     * @param queryBuilder       //查询条件
     * @param filterMap //
     */
    private void specResult(Long cid,QueryBuilder queryBuilder, Map<String, List<?>> filterMap) {
        //1、根据分类id查询对应的规格参数
        List<SpecParamDTO> specParamDTOS = goodsClient.querySpecParamBySpecGroupId(null, cid, true);

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withQuery(queryBuilder);
        nativeSearchQueryBuilder.withPageable(PageRequest.of(0, 1));

        //2、根据你选择的过滤项进行聚合;因为是主动类型推断,且字段不能进行分词,所以字段为：specs.name.keyword
        specParamDTOS.forEach(specParamDTO -> {
            //根据不同的name字段名进行聚合,聚合的名字就是字段的名字
            String name = specParamDTO.getName();
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name + ".keyword"));
        });

        //执行查询
        AggregatedPage<Goods> result = esTemplate.queryForPage(nativeSearchQueryBuilder.build(), Goods.class);
        Aggregations aggregations = result.getAggregations();


        //解析查询的结果
        specParamDTOS.forEach(specParamDTO -> {
            String name = specParamDTO.getName();
            StringTerms stringTerms = aggregations.get(name);

            //stringTerms是一个字段的多个桶,需要继续遍历
            List<String> list = stringTerms.getBuckets()
                    .stream()
                    .map(StringTerms.Bucket::getKeyAsString)
                    .collect(Collectors.toList());
            filterMap.put(name,list);
        });
    }


    /**
     * 将categoryId从桶中拿出来并查询出对象
     *
     * @param terms
     * @param map
     * @return
     */
    public List<Long> categoryResult(LongTerms terms, Map<String, List<?>> map) {

        List<Long> categoryIds = terms.getBuckets()
                .stream()
                .map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());

        //数据库中查询出分类信息
        List<CategoryDTO> categoryDTOList = goodsClient.queryCategoryById(categoryIds);
        map.put("分类", categoryDTOList);
        return categoryIds;
    }

    /**
     * 将brandId从桶中拿出来并查询出对象
     *
     * @param terms
     * @param map
     */
    public void brandResult(LongTerms terms, Map<String, List<?>> map) {

        List<Long> brandIds = terms.getBuckets()
                .stream()
                .map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());

        //数据库中查询出来
        List<BrandDTO> brandDTOList = goodsClient.queryBrandByIds(brandIds);
        map.put("品牌", brandDTOList);
    }

    /**
     * 抽取出来的,用来生成构建器
     * @param searchRequest
     * @return
     */
    public QueryBuilder getQueryBuilder(SearchRequest searchRequest) {
        String key = searchRequest.getKey();
        if (StringUtils.isEmpty(key)) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        //这个map集合装的为过滤的条件：分类条件、品牌条件、其他条件
        Map<String, String> filter = searchRequest.getFilter();
        //构建器
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.matchQuery("all",key).operator(Operator.AND));

        //过滤的条件为空说明正常的查询,不进行任何的过滤
        if (filter == null || filter.size() == 0) {
            return queryBuilder;
        }

        //分类条件(需要主键id查询)、品牌条件(需要主键id查询)、其他条件;有可能是多个条件,所以需要遍历添加进条件中
        filter.entrySet().forEach(objectEntrySet -> {
            //获取到其对应的键
            String entrySetKey = objectEntrySet.getKey();
            String value = objectEntrySet.getValue();
            if (entrySetKey.equals("品牌")) {
                entrySetKey = "brandId";
            } else if (entrySetKey.equals("分类")) {
                entrySetKey = "categoryId";
            } else {
                entrySetKey = "specs."+entrySetKey+".keyword";
            }
            //添加键值对的过滤，如：  分类  ->  手机(应是id值)
            queryBuilder.filter(QueryBuilders.termQuery(entrySetKey,value));
        });
        return queryBuilder;
    }
}
