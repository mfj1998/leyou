package com.leyou.search.indextest;

import com.leyou.client.GoodsClient;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SpuDTO;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.IndexService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IndexTest {

    @Autowired
    private ElasticsearchTemplate esTemplate;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private IndexService indexService;
    @Autowired
    private GoodsClient goodsClient;

    /**
     * 创建索引库
     */
    @Test
    public void createIndex() {
        //1、创建索引库
        esTemplate.createIndex(Goods.class);
        esTemplate.putMapping(Goods.class);
    }

    /**
     * 给索引库中添加数据
     */
    @Test
    public void insertDataForIndex() {
        int page = 1;
        while (true) {
            //循环查询出来SpuDTO对象，并且转换我Goods
            PageResult<SpuDTO> pageResult = goodsClient.queryBrandPage(page, 50, null, null);
            page++;
            if (null == pageResult || CollectionUtils.isEmpty(pageResult.getItems())) {
                break;
            }

            //转换
            List<Goods> goodsList = pageResult.getItems()
                    .stream()
                    .map(indexService::buildGoods)
                    .collect(Collectors.toList());

            //存储到索引库
            goodsRepository.saveAll(goodsList);
        }
    }

}
