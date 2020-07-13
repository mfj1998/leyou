package com.leyou.order.controller;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.service.OrderService;
import com.leyou.order.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 根据前端发送来的参数产生订单，同时返回这个订单号码
     * @param orderDTO
     * @return
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO) {
        Long orderId = orderService.createOrder(orderDTO);
        return ResponseEntity.ok(orderId);
    }

    /**
     * 根据订单的id查询出来对应的orderVo对象
     * @param oid
     * @return
     */
    @GetMapping("{oid}")
    public ResponseEntity<OrderVO> getOrder(@PathVariable("oid") Long oid) {
        OrderVO orderVO = orderService.getOrder(oid);
        return ResponseEntity.ok(orderVO);
    }

    /**
     * 使用微信支付的工具类生成url支付接口,返回到html页面
     * 因为订单要设置为两小时有效,所以需要存储到redis中
     * @param oid
     * @return
     */
    @GetMapping("url/{oid}")
    public ResponseEntity<String> getPayUrl(@PathVariable("oid") Long oid) {
        String url = orderService.createPayUrl(oid);
        return ResponseEntity.ok(url);
    }

    /**
     * 根据订单id查询出对应的订单状态
     * @param oid
     * @return
     */
    @GetMapping("state/{oid}")
    public ResponseEntity<Integer> getStatus(@PathVariable("oid")Integer oid) {
        Integer status = orderService.getStatus(oid);
        return ResponseEntity.ok(status);
    }
}
