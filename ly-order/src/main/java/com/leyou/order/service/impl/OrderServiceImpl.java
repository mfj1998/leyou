package com.leyou.order.service.impl;

import com.leyou.client.GoodsClient;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.threadlocals.UserHolder;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.dto.SkuDTO;
import com.leyou.order.dto.CartDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.entity.Order;
import com.leyou.order.entity.OrderDetail;
import com.leyou.order.entity.OrderLogistics;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderLogisticsMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.service.OrderService;
import com.leyou.order.utils.PayHelper;
import com.leyou.order.vo.OrderDetailVO;
import com.leyou.order.vo.OrderLogisticsVO;
import com.leyou.order.vo.OrderVO;
import com.leyou.pojo.UserAddress;
import com.leyou.user.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.leyou.common.constants.MQConstants.Exchange.ORDER_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.RoutingKey.ORDER_DELETE_KEY;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final String payUrl = "ly:order:pay:";

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderLogisticsMapper orderLogisticsMapper;
    @Autowired
    private UserClient userClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PayHelper payHelper;


    @Override
    @Transactional
    public Long createOrder(OrderDTO orderDTO) {
        //雪花算法生成唯一订单id
        long orderId = idWorker.nextId();
        Order order = new Order();
        order.setOrderId(orderId);

        UserInfo userInfo = UserHolder.getUserInfo();
        order.setUserId(userInfo.getId());

        //获取到订单的sku、detail、地址信息、并将sku表的库存减去num参数
        //查询出skuDTOList
        List<Long> skuIds = orderDTO.getCarts().stream()
                .map(CartDTO::getSkuId)
                .collect(Collectors.toList());
        List<SkuDTO> skuDTOList = goodsClient.querySkuByIdList(skuIds);
        //将CartDTO的属性设置为map
        Map<Long, Integer> skuDTOMap = orderDTO.getCarts().stream()
                .collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));

        //计算金额,并未orderDetail填充数据
        long money = 0;
        Date orderTime = new Date();
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (SkuDTO skuDTO : skuDTOList) {

            money += skuDTO.getPrice() * skuDTOMap.get(skuDTO.getId());
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setId(null);
            orderDetail.setSkuId(skuDTO.getId());
            orderDetail.setOrderId(orderId);
            orderDetail.setNum(skuDTOMap.get(skuDTO.getId()));
            orderDetail.setTitle(skuDTO.getTitle());
            orderDetail.setPrice(skuDTO.getPrice());
            orderDetail.setOwnSpec(skuDTO.getOwnSpec());
            orderDetail.setImage(StringUtils.substringBefore(skuDTO.getImages(), ","));
            orderDetail.setUpdateTime(orderTime);
            orderDetail.setCreateTime(orderTime);
            orderDetails.add(orderDetail);
        }

        //为order填充数据
        order.setTotalFee(money);                           //总金额
        order.setPostFee(0L);                               //邮费
        order.setActualFee(money + order.getPostFee());     //实际金额,邮费+总金额-活动打折
        order.setPaymentType(orderDTO.getPaymentType());    //支付类型
        order.setStatus(OrderStatusEnum.INIT.getValue());   //初实话支付状态
        int count = orderMapper.insertSelective(order);
        if (count == 0) {
            throw new LyException(ExceptionEnum.ORDER_PAY_ERROR);
        }

        count = orderDetailMapper.insertList(orderDetails);
        if (count != orderDetails.size()) {
            throw new LyException(ExceptionEnum.ORDER_PAY_ERROR);
        }

        //为orderLogistics物流 信息填充数据
        List<UserAddress> addressList = userClient.getAddress();
        if (CollectionUtils.isEmpty(addressList)) {
            throw new LyException(ExceptionEnum.SERVER_ERROR);
        }
        UserAddress userAddress = addressList.get(0);
        OrderLogistics orderLogistics = BeanHelper.copyProperties(userAddress, OrderLogistics.class);
        orderLogistics.setOrderId(orderId);
        orderLogistics.setUpdateTime(orderTime);
        orderLogistics.setCreateTime(orderTime);
        count = orderLogisticsMapper.insertSelective(orderLogistics);
        if (count != 1) {
            throw new LyException(ExceptionEnum.ORDER_PAY_ERROR);
        }

        //减少sku库存
        goodsClient.updateSkuStock(skuDTOMap);

        Set<Long> skuSet = skuDTOMap.keySet();
        //这一切都完成后RabbitMQ远程调用cart，删除redis中的缓存的信息
        Long uid = userInfo.getId();
        amqpTemplate.convertAndSend(ORDER_EXCHANGE_NAME, ORDER_DELETE_KEY, uid);

        return orderId;
    }

    //支付的订单，会返回一个二维码的链接
    @Override
    public String createPayUrl(Long oid) {
        //先判断是否生成url到redis中
        String key = payUrl + String.valueOf(oid);
        String url = redisTemplate.opsForValue().get(key);
        if (!StringUtils.isBlank(url)) {
            return url;
        }


        //查询订单
        Order order = orderMapper.selectByPrimaryKey(oid);
        if (order == null) {
            throw new LyException(ExceptionEnum.QUERY_NOT_FOUND);
        }

        //判断订单状态,1为未支付,2支付,支付了抛出异常
        Integer status = order.getStatus();
        if (status.equals(OrderStatusEnum.INIT)) {
            throw new LyException(ExceptionEnum.ORDER_PAY_ERROR);
        }

        //设置支付金额和描述
        Long payMoney = 1L;
        String desc = "不可描述的支付";
        String orderUrl = payHelper.createOrder(oid, payMoney, desc);
        log.info("【二维码连接生成成功】：" + orderUrl);
        return orderUrl;
    }

    @Override
    public Integer getStatus(Integer oid) {
        Order order = orderMapper.selectByPrimaryKey(oid);
        if (order == null) {
            throw new LyException(ExceptionEnum.SERVER_ERROR);
        }

        return order.getStatus();
    }

    @Override
    public OrderVO getOrder(Long oid) {
        //根据orderId查询出对应的Order
        Order order = orderMapper.selectByPrimaryKey(oid);
        OrderVO orderVO = BeanHelper.copyProperties(order, OrderVO.class);

        //填充List<OrderDetailVO>集合对象
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(oid);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);
        if (CollectionUtils.isEmpty(orderDetails)) {
            throw new LyException(ExceptionEnum.SERVER_ERROR);
        }
        List<OrderDetailVO> orderDetailVOS = BeanHelper.copyWithCollection(orderDetails, OrderDetailVO.class);
        orderVO.setDetailList(orderDetailVOS);

        //填充OrderLogisticsVO对象,根据orderId查询
        OrderLogistics orderLogistics = orderLogisticsMapper.selectByPrimaryKey(oid);
        if (orderLogistics == null) {
            throw new LyException(ExceptionEnum.SERVER_ERROR);
        }
        orderVO.setLogistics(BeanHelper.copyProperties(orderLogistics, OrderLogisticsVO.class));

        return orderVO;
    }
}
