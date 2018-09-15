package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParam;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;

    @Override
    public List<SkuLsInfo> serch(SkuLsParam skuLsParam) {

        List<SkuLsInfo> skuLsInfos = new ArrayList<>();

        Search search = new Search.Builder(getMyDsl(skuLsParam)).addIndex("gmall0508").addType("SkuLsInfo").build();

        SearchResult execute = null;

        try {
            execute = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);

        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo source = hit.source;
            skuLsInfos.add(source);
        }

        return skuLsInfos;
    }

    public String getMyDsl(SkuLsParam skuLsParam){

        String catalog3Id = skuLsParam.getCatalog3Id();
        String keyword = skuLsParam.getKeyword();
        String[] valueId = skuLsParam.getValueId();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        if (null!= catalog3Id) {

            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        if (null!=valueId&&valueId.length>0) {
            //加载分类属性的条件；
        }

        if (StringUtils.isNotBlank(keyword)) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("keyword", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(100);

        return  searchSourceBuilder.toString();
    }

}
