package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;

import java.util.List;

public interface SkuService {
    void saveSku(SkuInfo skuInfo);

    List<SkuInfo> getSkuInfoList(String spuId);

    SkuInfo getSkuById(String skuId);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(String spuId, String skuId);

    List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId);

    List<SkuInfo> getSkuByCatalog3Id(int i);
}
