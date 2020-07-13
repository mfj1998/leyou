package com.leyou.cart.service;

import com.leyou.cart.entity.Cart;

import java.util.List;

public interface CartService {

    /**
     * 将选中的商品添加到对应的购物车当中
     * @param cart
     * @return
     */
    void addGoodsForCart(Cart cart);

    /**
     * 根据封闭线程工具类中的userId查询其购物车
     * @return
     */
    List<Cart> queryCart();

    /**
     * 修改购物车中某个商品的数量
     * @param skuId
     * @param num
     */
    void updateNum(Long skuId, Integer num);

    /**
     * 根据sku的id删除购物车中对应的商品
     * @param skuId
     * @return
     */
    void deleteCartGoods(Long skuId);

    /**
     * 将未登录状态localStorage中的购物车和登录状态的redis中的购物车进行合并的操作
     * @param cartList
     * @return
     */
    void addCartList(List<Cart> cartList);

    /**
     * 根据用户的id删除对应的购物车
     */
    void deleteCartByUserId(Long uid);
}
