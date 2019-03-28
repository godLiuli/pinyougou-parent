package com.pinyougou.solrutil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;
@Component
public class SolrUtil {
	
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private SolrTemplate solrTemplate;
	//从数据库中获取item数据，并导入solr
	public void importItemData() {
		TbItemExample example = new TbItemExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");
		List<TbItem> itemList = itemMapper.selectByExample(example);
		System.out.println("============开始============");
		for (TbItem item : itemList) {
			Map map = JSON.parseObject(item.getSpec());
			item.setSpecMap(map);
		}
		solrTemplate.saveBeans(itemList);
		solrTemplate.commit();
		System.out.println("==============成功=============");
	}
	
	public static void main(String[] args) {
		//获取IOC容器
		ApplicationContext ac = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
		//获取bean对象
		SolrUtil solrUtil = (SolrUtil) ac.getBean("solrUtil");
		solrUtil.importItemData();
	}

}
