package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    ListService listService;

    @Reference
    AttrService attrService;

    @RequestMapping("list")
    public String list(SkuLsParam skuLsParam, ModelMap map){

        List<SkuLsInfo> skuLsInfoList = listService.serch(skuLsParam);

        HashSet<String> strings = new HashSet<>();
        for (SkuLsInfo skuLsInfo : skuLsInfoList) {
            List<SkuLsAttrValue> skuLsAttrValues = skuLsInfo.getSkuAttrValueList();
            for (SkuLsAttrValue skuLsAttrValue : skuLsAttrValues) {
                String valueId = skuLsAttrValue.getValueId();
                strings.add(valueId);
            }
        }
        String join = StringUtils.join(strings,",");
        List<BaseAttrInfo> baseAttrInfos = attrService.getAttrListByValueId(join);

        List<Crumb> crumbs = new ArrayList<>();
        String[] valueId = skuLsParam.getValueId();

        if (null!=valueId&&valueId.length>0) {
            for (String sid : valueId) {
                Crumb crumb = new Crumb();
                Iterator<BaseAttrInfo> iterator = baseAttrInfos.iterator();
                while (iterator.hasNext()) {
                    BaseAttrInfo baseAttrInfo = iterator.next();
                    List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                    for (BaseAttrValue baseAttrValue : attrValueList) {
                        String id = baseAttrValue.getId();
                        if (id.equals(sid)) {
                            crumb.setValueName(baseAttrValue.getValueName());
                            iterator.remove();
                        }
                    }
                }
                String crumbsUrlParam = getCrumbsUrlParam(skuLsParam, sid);
                crumb.setUrlParam(crumbsUrlParam);
                crumbs.add(crumb);
            }
        }

        String urlParam = getUrlParam(skuLsParam);
        map.put("urlParam", urlParam);
        map.put("attrValueSelectedList", crumbs);
        map.put("attrList", baseAttrInfos);
        map.put("skuLsInfoList", skuLsInfoList);
        return "list";
    }

    private String getUrlParam(SkuLsParam skuLsParam) {
        String[] valueId = skuLsParam.getValueId();
        String catalog3Id = skuLsParam.getCatalog3Id();
        String keyword = skuLsParam.getKeyword();

        String urlParam = "";

        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam +"&" ;
            }
            urlParam = urlParam  + "catalog3Id="+catalog3Id;
        }

        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam +"&" ;
            }
            urlParam = urlParam + "keyword="+keyword;
        }

        if(null!=valueId&&valueId.length>0){
            for (String id : valueId) {
                urlParam = urlParam +"&" + "valueId="+id;
            }
        }


        return urlParam;
    }

    private String getCrumbsUrlParam(SkuLsParam skuLsParam, String sid) {
        String[] valueId = skuLsParam.getValueId();
        String keyword = skuLsParam.getKeyword();
        String catalog3Id = skuLsParam.getCatalog3Id();

        String urlParam = "";
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }

        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }

        if (null!=valueId&&valueId.length>0) {
            for (String id: valueId){
                if (!id.equals(sid)) {
                    if (StringUtils.isNotBlank(urlParam)) {
                        urlParam = urlParam + "&valueId=" + id;
                    }
                }
            }

        }
        return urlParam;
    }
}
