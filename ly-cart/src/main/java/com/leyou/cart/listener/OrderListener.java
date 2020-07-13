package com.leyou.cart.listener;

import com.leyou.cart.service.CartService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.leyou.common.constants.MQConstants.Exchange.ORDER_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.Queue.ORDER_DELETE_QUEUE;
import static com.leyou.common.constants.MQConstants.RoutingKey.ORDER_DELETE_KEY;

@Component
public class OrderListener {

    @Autowired
    private CartService cartService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = ORDER_DELETE_QUEUE, durable = "true"),
            exchange = @Exchange(name = ORDER_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = ORDER_DELETE_KEY
    ))
    public void listenerCartDelete(Long uid) {
        cartService.deleteCartByUserId(uid);
    }
}
