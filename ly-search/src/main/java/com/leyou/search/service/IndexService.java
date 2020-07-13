package com.leyou.search.service;

import com.leyou.item.dto.SpuDTO;
import com.leyou.search.pojo.Goods;

public interface IndexService {

    /**
     * 使用测试类,将数据从数据库中查询到ElasticSearch中
     * @param spuDTO
     * @return
     */
    Goods buildGoods(SpuDTO spuDTO);

    /**
     * 商用rabbitMQ的监听器,进行商品上架的操作
     * @param id
     */
    void createIndex(Long id);

    /**
     * rabbitMQ的监听后触发的删除es商品信息的方法
     * @param id
     */
    void deleteIndex(Long id);
}
