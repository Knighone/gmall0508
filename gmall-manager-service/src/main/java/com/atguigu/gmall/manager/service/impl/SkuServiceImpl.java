package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manager.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.manager.mapper.SkuImageMapper;
import com.atguigu.gmall.manager.mapper.SkuInfoMapper;
import com.atguigu.gmall.manager.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public void saveSku(SkuInfo skuInfo) {
        skuInfoMapper.insertSelective(skuInfo);
        String skuId = skuInfo.getId();

        // 保存平台属性关联信息
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insert(skuAttrValue);
        }

        // 保存销售属性关联信息
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        }

        // 保存图片相关信息
        List<SkuImage> skuImages = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImages) {
            skuImage.setSkuId(skuId);
            skuImageMapper.insertSelective(skuImage);
        }
    }

    @Override
    public List<SkuInfo> getSkuInfoList(String spuId) {
        SkuInfo skuInfoParam = new SkuInfo();
        skuInfoParam.setSpuId(spuId);
        List<SkuInfo> skuInfos = skuInfoMapper.select(skuInfoParam);
        return skuInfos;
    }

    public SkuInfo getSkuByIdFromDB(String skuId) {
        SkuInfo skuInfoParam = new SkuInfo();
        skuInfoParam.setId(skuId);
        SkuInfo skuInfo = skuInfoMapper.selectOne(skuInfoParam);

        SkuImage skuImageParam = new SkuImage();
        skuImageParam.setSkuId(skuId);
        List<SkuImage> images = skuImageMapper.select(skuImageParam);

        skuInfo.setSkuImageList(images);
        return skuInfo;
    }

    @Override
    public SkuInfo getSkuById(String skuId) {

        String custom = Thread.currentThread().getName();

        System.err.println(custom + "线程进入sku查询方法");

        SkuInfo skuInfo = null;
        String skuKey = "sku:" + skuId + ":info";

        Jedis jedis = redisUtil.getJedis();
        String s = jedis.get(skuKey);

        if (StringUtils.isNotBlank(s) && "empty".equals(s)) {
            System.err.println( custom + "线程进入db发现没有数据，返回");
            return  null;
        }

        if (StringUtils.isNotBlank(s) && !"empty".equals(s)){
            System.err.println(custom + "线程能够从redis中获得数据");
            skuInfo = JSON.parseObject(s,SkuInfo.class);
        }
        else {
            System.err.println(custom + "线程没有从redis中取出数据，申请访问数据库" );
            String OK = jedis.set("sku:" + skuId + ":lock", "1","nx","px",10000);
            if (StringUtils.isNotBlank(OK)) {
                System.err.println(custom + "线程得到分布式锁，开始访问数据库");
                skuInfo = getSkuByIdFromDB(skuId);
                if (null != skuInfo) {
                    System.err.println("线程访问数据库成功，删除分布式锁");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    jedis.del("sku" + skuId + ":lock");
                }else {
                    System.err.println(custom + "线程需要访问数据库，但没有得到分布式锁，开始自旋");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //自旋
                    jedis.close();
                    getSkuById(skuId);
                }
            }
            if (null==skuInfo) {
                System.err.println(custom + "线程从数据库得到的数据为空，同步到redis");
                jedis.set(skuKey, "empty");
            }
            System.err.println(custom + "线程将数据同步到redis");
            if (null!= skuInfo&&!"empty".equals(s)){
                jedis.set(skuKey, JSON.toJSONString(skuInfo));
            }
        }
        System.err.println(custom + "线程访问结束返回");

        jedis.close();
        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(String spuId, String skuId) {
        List<SpuSaleAttr> spuSaleAttrList = skuSaleAttrValueMapper.selectSpuSaleAttrListCheckBySku(Integer.parseInt(spuId), Integer.parseInt(skuId));
        return spuSaleAttrList;
    }

    @Override
    public List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuInfo> skuInfos = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(Integer.parseInt(spuId));
        return skuInfos;
    }

    @Override
    public List<SkuInfo> getSkuByCatalog3Id(int catalog3Id) {
        SkuInfo skuInfoParam = new SkuInfo();
        skuInfoParam.setCatalog3Id(String.valueOf(catalog3Id));
        List<SkuInfo> skuInfos = skuInfoMapper.select(skuInfoParam);
        if (null != skuInfos) {
            for (SkuInfo skuInfo : skuInfos) {
                String skuId = skuInfo.getId();

                SkuImage skuImageParam = new SkuImage();
                skuImageParam.setSkuId(skuId);
                List<SkuImage> skuImages = skuImageMapper.select(skuImageParam);
                skuInfo.setSkuImageList(skuImages);

                SkuAttrValue skuAttrValueParam = new SkuAttrValue();
                skuAttrValueParam.setSkuId(skuId);
                List<SkuAttrValue> skuAttrValues = skuAttrValueMapper.select(skuAttrValueParam);
                skuInfo.setSkuAttrValueList(skuAttrValues);

            }
        }

        return skuInfos;
    }
}
