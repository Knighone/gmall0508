package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;

    @RequestMapping("addToCart")
    @ResponseBody
    public String addToCart(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String,String > map){

        List<CartInfo> cartInfos = new ArrayList<>();
        String skuId = map.get("skuId");
        Integer num = Integer.parseInt(map.get("num"));

        SkuInfo skuInfo = skuService.getSkuById(skuId);

        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuNum(num);
        cartInfo.setSkuPrice(skuInfo.getPrice());
        cartInfo.setIsChecked("1");
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setCartPrice(new BigDecimal(num).multiply(skuInfo.getPrice()));

        String userId = (String) request.getAttribute("userId");

        if (StringUtils.isBlank(userId)) {
            cartInfo.setUserId("");
            String cookieValue = CookieUtil.getCookieValue(request, "listCartCookie", true);
            if (StringUtils.isBlank(cookieValue)) {
                cartInfos.add(cartInfo);
            } else {
                cartInfos = JSON.parseArray(cookieValue, CartInfo.class);

                boolean b = if_new_cart(cartInfos, cartInfo);

                if (b) {
                    cartInfos.add(cartInfo);
                } else {
                    for (CartInfo info : cartInfos) {
                        String cartSkuId = info.getSkuId();
                        if (cartSkuId.equals(skuId)) {
                            info.setSkuNum(info.getSkuNum() + num);
                            info.setCartPrice(new BigDecimal(info.getSkuNum()).multiply(info.getSkuPrice()));
                        }
                    }
                }
            }
            CookieUtil.setCookie(request, response, "listCartCookie", JSON.toJSONString(cartInfos), 1000*60*60*24, true);
        } else {
            cartInfo.setUserId(userId);
            CartInfo cartInfoDb = cartService.ifCartExist(cartInfo);
            if (null != cartInfoDb) {
                cartInfoDb.setSkuNum(num + cartInfoDb.getSkuNum());
                cartInfoDb.setCartPrice(new BigDecimal(cartInfoDb.getSkuNum()).multiply(cartInfoDb.getSkuPrice()));
                cartService.updateCart(cartInfoDb);
            } else {
                cartService.addCart(cartInfo);
            }
            cartService.flushCartCacheByUserId(userId);
        }

        return null;
    }

    private boolean if_new_cart(List<CartInfo> listCartCookie, CartInfo cartInfo) {

        boolean b = true;

        for (CartInfo info : listCartCookie) {
            if (info.getSkuId().equals(cartInfo.getSkuId())) {
                b = false;
            }
        }

        return b;
    }

    @RequestMapping("cartSuccess")
    public String cartSuccess(){
        return "redirect:/success";
    }
}
