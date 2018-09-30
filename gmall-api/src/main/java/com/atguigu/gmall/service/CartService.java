package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {

    void combine(String id, List<CartInfo> parseArray);

    CartInfo ifCartExist(CartInfo cartInfo);

    void updateCart(CartInfo cartInfoDb);

    void addCart(CartInfo cartInfo);

    void flushCartCacheByUserId(String userId);

}
