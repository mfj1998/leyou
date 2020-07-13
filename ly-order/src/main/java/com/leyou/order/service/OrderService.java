package com.leyou.order.service;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.vo.OrderVO;

public interface OrderService {

    /**
     * 根据前端发送来的参数产生订单，同时返回这个订单号码
     * @param orderDTO
     * @return
     */
    Long createOrder(OrderDTO orderDTO);

    /**
     * 使用微信支付的工具类生成url支付接口,返回到html页面
     * 因为订单要设置为两小时有效,所以需要存储到redis中
     * @param oid
     * @return
     */
    String createPayUrl(Long oid);

    /**
     * 根据订单id查询对应的状态
     * @param oid
     * @return
     */
    Integer getStatus(Integer oid);

    /**
     * 根据订单的id查询出其对应的orderVO对象
     * @param oid
     * @return
     */
    OrderVO getOrder(Long oid);
}
