package com.leyou.common.constants;

public abstract class MQConstants {

    public static final class Exchange {
        /**
         * 商品服务交换机名称
         */
        public static final String ITEM_EXCHANGE_NAME = "ly.item.exchange";

        /**
         * 消息服务交换机名称
         */
        public static final String SMS_EXCHANGE_NAME = "ly.sms.exchange";
        /**
         * 将购物车的商品删除的交换机
         */
        public static final String ORDER_EXCHANGE_NAME = "ly.order.exchange";
    }

    public static final class RoutingKey {
        /**
         * 商品上架的routing-key
         */
        public static final String ITEM_UP_KEY = "item.up";
        /**
         * 商品下架的routing-key
         */
        public static final String ITEM_DOWN_KEY = "item.down";

        /**
         * 短信发送的routing-key
         */
        public static final String VERIFY_CODE_KEY = "sms.verify.code";
        /**
         * 删除购物车商品的路由key
         */
        public static final String ORDER_DELETE_KEY = "order.delete";
    }

    public static final class Queue {
        /**
         * 搜索服务，商品上架的队列
         */
        public static final String SEARCH_ITEM_UP = "search.item.up.queue";
        /**
         * 搜索服务，商品下架的队列
         */

        public static final String SEARCH_ITEM_DOWN = "search.item.down.queue";

        /**
         * 搜索服务，商品上架的队列
         */
        public static final String PAGE_ITEM_UP = "page.item.up.queue";
        /**
         * 搜索服务，商品下架的队列
         */
        public static final String PAGE_ITEM_DOWN = "page.item.down.queue";

        /**
         * 短信的收发队列
         */
        public static final String SMS_VERIFY_CODE_QUEUE = "sms.verify.code.queue";

        public static final String ORDER_DELETE_QUEUE = "order.delete.queue";
    }
}
