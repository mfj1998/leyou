package com.leyou.cart.controller;

import com.leyou.cart.entity.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 将选中的商品添加到对应的购物车当中
     * @param cart
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addGoodsForCart(@RequestBody Cart cart) {
        cartService.addGoodsForCart(cart);
        return ResponseEntity.ok().build();
    }

    /**
     * 查看用户的购物车,不需要请求参数,封闭线程获取即可
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<Cart>> queryCart() {
        List<Cart> carts = cartService.queryCart();
        return ResponseEntity.ok(carts);
    }

    /**
     * 添加商品的数量,修改redis中的数量
     * @param skuId
     * @param num
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateNum(@RequestParam("id")Long skuId,
                                          @RequestParam("num")Integer num) {
        cartService.updateNum(skuId,num);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据sku的id删除购物车中对应的商品
     * @param skuId
     * @return
     */
    @DeleteMapping("/{skuId}")
    public ResponseEntity<Void> deleteCartGoods(@PathVariable("skuId") Long skuId) {
        cartService.deleteCartGoods(skuId);
        return ResponseEntity.ok().build();
    }

    /**
     * 将未登录状态localStorage中的购物车和登录状态的redis中的购物车进行合并的操作
     * @param cartList
     * @return
     */
    @PostMapping("/list")
    public ResponseEntity<Void> addCartList(@RequestBody List<Cart> cartList) {
        cartService.addCartList(cartList);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
