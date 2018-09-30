package com.atguigu.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {

    @Reference
    UserService userService;

    @RequestMapping("/userList")
    @ResponseBody
    public List<UserInfo> index(){
        List<UserInfo> userList = userService.getUserList();
        return userList;
    }

    @RequestMapping("/userAddress")
    @ResponseBody
    public UserAddress address(@RequestParam("uid")String uid){
        UserAddress address = userService.getAddress(uid);
        return address;
    }
}
