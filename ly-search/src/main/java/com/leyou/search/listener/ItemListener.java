package com.leyou.search.listener;

import com.leyou.search.service.IndexService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.leyou.common.constants.MQConstants.Exchange.ITEM_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.Queue.SEARCH_ITEM_DOWN;
import static com.leyou.common.constants.MQConstants.Queue.SEARCH_ITEM_UP;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_DOWN_KEY;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_UP_KEY;

/**
 * 这是rabbitMQ的消费者监听器,经停成功则调用对应的方法
 */
@Component
public class ItemListener {

    @Autowired
    private IndexService indexService;

    /**
     * 若Item-service(生产者)中的商品进行上架,rabbitMQ发送下架商品的id信息;
     * 该微服务监听rabbitMQ中的该队列,有信息上架信息则进行上架的操作
     * @param id
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = SEARCH_ITEM_UP,durable = "true"),
            exchange = @Exchange(name = ITEM_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),key = ITEM_UP_KEY
    ))
    public void listenerUp(Long id) {
        if (id != null) {
            indexService.createIndex(id);
        }
    }

    /**
     * 商品下架,mysql数据库商品下架,发送信息到rabbitMQ;
     * ElasticSearch同样对商品进行下架
     * @param id
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = SEARCH_ITEM_DOWN, durable = "true"),
            exchange = @Exchange(name = ITEM_EXCHANGE_NAME, type = ExchangeTypes.TOPIC), key = ITEM_DOWN_KEY
    ))
    public void listenerDown(Long id) {
        indexService.deleteIndex(id);
    }
}
