package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    SkuService skuService;

    @RequestMapping("{skuId}.html")
    public String index(@PathVariable String skuId, ModelMap map) {
        SkuInfo skuInfo = skuService.getSkuById(skuId);
        if (null != skuInfo) {
            map.put("skuInfo", skuInfo);
            String spuId = skuInfo.getSpuId();
            List<SpuSaleAttr> spuSaleAttrs = skuService.getSpuSaleAttrListCheckBySku(spuId, skuId);
            map.put("spuSaleAttrListCheckBySku", spuSaleAttrs);

            Map<String,String> skuMap = new HashMap<String, String>();
            List<SkuInfo> skuInfos = skuService.getSkuSaleAttrValueListBySpu(spuId);
            for (SkuInfo info : skuInfos) {
                String v = info.getId();
                String k = "";
                List<SkuSaleAttrValue> skuSaleAttrValues = info.getSkuSaleAttrValueList();
                for (SkuSaleAttrValue skuSaleAttrValue: skuSaleAttrValues){
                    k = k + "|" + skuSaleAttrValue.getSaleAttrValueId();
                }
                skuMap.put(k, v);
            }
            String skuMapJson = JSON.toJSONString(skuMap);
            map.put("skuMapJson", skuMapJson);
            System.out.println(skuMapJson);
        }

        return "item";
    }
}
