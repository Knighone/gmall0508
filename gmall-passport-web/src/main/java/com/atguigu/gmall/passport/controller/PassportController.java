package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userservice;

    @Reference
    CartService cartService;

    @RequestMapping("index")
    public String index(String returnUrl, ModelMap map){
        map.put("originUrl", returnUrl);
        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request, HttpServletResponse response) {
        UserInfo user = userservice.login(userInfo);
        if (null == user) {
            return "err";
        }else {
            Map<String,String> userMap = new HashMap<>();
            userMap.put("userId", user.getId());
            userMap.put("nickName", user.getNickName());
            String ip = getMyIpFromRequest(request);
            String token = JwtUtil.encode("atguigugmall0508",userMap,ip);

            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            cartService.combine(user.getId(), JSON.parseArray(listCartCookie, CartInfo.class));
            CookieUtil.deleteCookie(request, response, "listCartCookie");
            return token;
        }
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request,String currentIp,ModelMap map,String token){
        try {
            Map atguigugmall0508 = JwtUtil.decode("atguigugmall0508", token, currentIp);
            if (null != atguigugmall0508) {
                return "success";
            } else {
                return "fail";
            }
        } catch (Exception e) {
            return "fail";
        }
    }

    private String getMyIpFromRequest(HttpServletRequest request) {
        String ip = "";
        ip = request.getRemoteAddr();
        if (StringUtils.isBlank(ip)) {
            ip = request.getHeader("x-forwarder-for");
            if (StringUtils.isBlank(ip)) {
                ip = "192.168.0.16";
            }
        }
        return ip;
    }
}
