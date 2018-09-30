package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    UserAddressMapper userAddressMapper;

    @Override
    public List<UserInfo> getUserList() {
        List<UserInfo> userList = userInfoMapper.selectAll();
        return userList;
    }

    @Override
    public UserAddress getAddress(String uid) {
        Example example = new Example(UserAddress.class);
        Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId", uid);
        UserAddress userAddress =  userAddressMapper.selectOneByExample(example);
        return userAddress;
    }

    @Override
    public UserInfo login(UserInfo userInfo) {

        UserInfo info = userInfoMapper.selectOne(userInfo);
        return info;
    }


}
