package com.leyou.search.service.impl;

import com.google.common.collect.Lists;
import com.leyou.client.GoodsClient;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SpuDTO;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class RedisServiceImpl {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private IndexService indexService;

    /**
     * 监听域对象的创建,进行初始化的方法
     */
    //@PostConstruct
    public void contextInitialized() {
        int page = 1;
        while (true) {
            //循环查询出来SpuDTO对象，并且转换我Goods
            PageResult<SpuDTO> pageResult = goodsClient.queryBrandPage(page, 50, null, null);
            page++;
            if (null == pageResult || CollectionUtils.isEmpty(pageResult.getItems())) {
                break;
            }

            List<SpuDTO> items = pageResult.getItems();
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            List<List<SpuDTO>> partition = Lists.partition(items, 70);

            partition.forEach(spuDTOS -> {
                executorService.execute(new Thread(()->{
                    spuDTOS.forEach(spuDTO -> {
                        //添加到redis中
                        Goods goods = indexService.buildGoods(spuDTO);
                        BoundHashOperations operations = redisTemplate.boundHashOps("goodsList");
                        operations.put(goods.getId(),goods);
                    });
                }));
            });

            //释放线程资源
            executorService.shutdown();
        }
    }
}
