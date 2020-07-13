package com.leyou.cart.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.cart.entity.Cart;
import com.leyou.cart.service.CartService;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.threadlocals.UserHolder;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service
@SuppressWarnings("all")
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "ly:cart:uid:";

    @Override
    public void addGoodsForCart(Cart cart) {
        UserInfo userInfo = UserHolder.getUserInfo();
        String key = KEY_PREFIX + userInfo.getId().toString();

        //构建该用户的购物车的hash,大键为user的id
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        //构建hash的内部map，键为sku的id
        String skuIdStr = cart.getSkuId().toString();
        int num = cart.getNum();

        //判断该商品是否已经在redis的username键中,存在则数量+1
        Boolean keyBool = hashOps.hasKey(skuIdStr);
        if (keyBool != null && keyBool) {
            String str = hashOps.get(skuIdStr);
            Cart cartTwo = JsonUtils.toBean(str, Cart.class);
            cart.setNum(cartTwo.getNum() + num);
        }

        hashOps.put(skuIdStr, JsonUtils.toString(cart));
    }

    @Override
    public List<Cart> queryCart() {
        String key = getKey();

        //判断该用户的购物车是否存在或为空
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        Long size = hashOps.size();
        if (size == null || size == 0) {
            throw new LyException(ExceptionEnum.CART_IS_NULL);
        }

        //将json的购物车数据转换为java对象
        List<String> cartListStr = hashOps.values();
        List<Cart> carts = cartListStr.stream()
                .map(cartStr -> JsonUtils.toBean(cartStr, Cart.class))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(carts)) {
            throw new LyException(ExceptionEnum.CART_IS_NULL);
        }
        return carts;
    }

    @Override
    public void updateNum(Long skuId, Integer num) {
        String key = getKey();

        String skuIdStr = skuId.toString();
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        Boolean hasKeyBool = hashOps.hasKey(skuIdStr);
        if (hasKeyBool == null || !hasKeyBool) {
            throw new LyException(ExceptionEnum.CART_IS_NULL);
        }

        Cart cart = JsonUtils.toBean(hashOps.get(skuIdStr), Cart.class);
        cart.setNum(num);

        //重新存入缓存
        hashOps.put(skuIdStr, JsonUtils.toString(cart));
    }

    @Override
    public void deleteCartGoods(Long skuId) {
        String key = getKey();
        String skuIdStr = skuId.toString();

        Long num = redisTemplate.opsForHash().delete(key, skuIdStr);
        if (num == null || num == 0) {
            throw new LyException(ExceptionEnum.SERVER_ERROR);
        }
    }

    @Override
    public void addCartList(List<Cart> cartList) {
        String key = getKey();
        if (CollectionUtils.isEmpty(cartList)) {
            throw new LyException(ExceptionEnum.SERVER_ERROR);
        }

        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        for (Cart cart : cartList) {
            String skuIdStr = cart.getSkuId().toString();

            //存在则将两个数量相加在添加到redis
            Boolean skuBool = hashOps.hasKey(skuIdStr);
            if (skuBool) {
                Integer num = cart.getNum();
                Cart redisCart = JsonUtils.nativeRead(hashOps.get(skuIdStr), new TypeReference<Cart>() {
                });
                redisCart.setNum(redisCart.getNum() + num);
            }

            //不存在)则添加进去
            hashOps.put(skuIdStr, JsonUtils.toString(cart));
        }
    }

    @Override
    public void deleteCartByUserId(Long uid) {
        String key = KEY_PREFIX + String.valueOf(uid);
        Boolean aBoolean = redisTemplate.hasKey(key);
        if (!aBoolean) {
            return;
        }

        Boolean delete = redisTemplate.delete(key);
        if (!delete) {
            throw new LyException(ExceptionEnum.SERVER_ERROR);
        }
    }

    private String getKey() {
        UserInfo userInfo = UserHolder.getUserInfo();
        String key = KEY_PREFIX + userInfo.getId().toString();
        //判断key是否存在
        Boolean keyBool = redisTemplate.hasKey(key);
        if (keyBool == null || !keyBool) {
            throw new LyException(ExceptionEnum.CART_IS_NULL);
        }
        return key;
    }
}
