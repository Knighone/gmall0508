package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Override
    public void combine(String userId, List<CartInfo> parseArray) {

        CartInfo cartInfoParam = new CartInfo();
        cartInfoParam.setUserId(userId);
        List<CartInfo> listCartDb = cartInfoMapper.select(cartInfoParam);

        if (null!=parseArray&&parseArray.size()>0) {
            for (CartInfo cartCookie : parseArray) {
                String skuIdCookie = cartCookie.getSkuId();
                boolean b = true ;
                if (null!=listCartDb&&listCartDb.size()>0) {
                    b = if_new_cart(cartCookie, listCartDb);
                }
                if (b) {
                    cartCookie.setUserId(userId);
                    cartInfoMapper.insertSelective(cartCookie);
                } else {
                    CartInfo cartDb = new CartInfo();
                    for (CartInfo info : listCartDb) {
                        if (info.getSkuId().equals(cartCookie.getSkuId())) {
                            cartDb = info;
                        }
                        cartDb.setIsChecked(cartCookie.getIsChecked());
                        cartDb.setSkuNum(cartCookie.getSkuNum());
                        cartDb.setCartPrice(cartDb.getSkuPrice().multiply(new BigDecimal(cartDb.getSkuNum())));
                        cartInfoMapper.updateByPrimaryKeySelective(cartDb);
                    }
                }

        }

        }
        flushCartCacheByUserId(userId);

    }

    @Override
    public CartInfo ifCartExist(CartInfo cartInfo) {

        Example e = new Example(CartInfo.class);
        e.createCriteria().andEqualTo("userId", cartInfo.getUserId());
        CartInfo cartInfoResult = cartInfoMapper.selectOneByExample(e);
        return cartInfoResult;
    }

    @Override
    public void updateCart(CartInfo cartInfoDb) {

        cartInfoMapper.updateByPrimaryKeySelective(cartInfoDb);
        flushCartCacheByUserId(cartInfoDb.getUserId());
    }

    @Override
    public void addCart(CartInfo cartInfo) {
        cartInfoMapper.insertSelective(cartInfo);
        flushCartCacheByUserId(cartInfo.getUserId());
    }

    @Override
    public void flushCartCacheByUserId(String userId) {

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfos = cartInfoMapper.select(cartInfo);

        if (null != cartInfos && cartInfos.size() > 0) {
            Map<String, String> map = new HashMap<>();
            for (CartInfo info : cartInfos) {
                map.put(info.getId(), JSON.toJSONString(info));

                Jedis jedis = redisUtil.getJedis();
                jedis.hmset("cart:" + userId + ":list", map);
                jedis.close();
            }
        } else {
            Jedis jedis = redisUtil.getJedis();
            jedis.hmset("cart:" + userId + ":list", null);
            jedis.close();
        }
    }

    private boolean if_new_cart(CartInfo cartCookie, List<CartInfo> listCartDb) {

        boolean b = true;

        for (CartInfo cartInfo : listCartDb) {
            if (cartCookie.getSkuId().equals(cartInfo.getSkuId())) {
                b = false;
            }
        }
            return b;
    }
}
