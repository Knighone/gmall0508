package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.manager.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.manager.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseAttrInfo> getAttrListByCtg3(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> attrInfos = baseAttrInfoMapper.select(baseAttrInfo);
        if (attrInfos != null && attrInfos.size() > 0) {
            for (BaseAttrInfo attrInfo : attrInfos){
                String attrInfoId = attrInfo.getId();
                BaseAttrValue baseAttrValue = new BaseAttrValue();
                baseAttrValue.setAttrId(attrInfoId);
                List<BaseAttrValue> attrValues = baseAttrValueMapper.select(baseAttrValue);
                attrInfo.setAttrValueList(attrValues);
            }
        }return attrInfos;
    }

    @Override
    public void saveAttr(BaseAttrInfo baseAttrInfo) {
        String id = baseAttrInfo.getId();
        if(StringUtils.isBlank(id)){
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
            String attrId = baseAttrInfo.getId();
            for (BaseAttrValue baseAttrValue:baseAttrInfo.getAttrValueList()){
                baseAttrValue.setAttrId(attrId);
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }else {
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
            String attrId = id;
            Example example = new Example(BaseAttrValue.class);
            example.createCriteria().andEqualTo("attrId", attrId);
            baseAttrValueMapper.deleteByExample(example);
            for (BaseAttrValue baseAttrValue:baseAttrInfo.getAttrValueList()){
                baseAttrValue.setAttrId(attrId);
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }
    }

    @Override
    public List<BaseAttrInfo> getAttrListByValueId(String join) {
        List<BaseAttrInfo> attrInfos = baseAttrInfoMapper.selectAttrListByValueId(join);
        return attrInfos;
    }
}
