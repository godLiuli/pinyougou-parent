package com.pinyougou.search.service.impl;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.impl.StreamingBinaryResponseParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService {
	@Autowired
	private SolrTemplate solrTemplate;
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 根据复制域item_keywords 来搜索，复制域包括(item_goodsid,item_category,item_brand,item_seller)
	 * @param serachMap
	 * @return
	 */
	public Map<String, Object> search(Map searchMap) {
		System.out.println("search执行1。。。");
		Map<String, Object> map =  new HashMap<>();
		//1.根据关键字查询，高亮显示
		System.out.println("search执行2.。。");
		Map<String, Object> map2 = searchList(searchMap);
		map.putAll(map2);
		System.out.println("search执行3.。。");
		//2.根据关键字查询商品分类itemCategory
		List<String> categoryList = searchCatgoryList(searchMap);
		map.put("categoryList", categoryList);
		
		//3.查询品牌和规格列表(多个品牌则默认显示第一个品牌的规格列表)
		String category = (String) searchMap.get("category");//根据查询条件
		if(category!=null && !"".equals(category)) { //查询条件searchMap的category有值，说明根据关键字查询之后点击了某个商品分类
			map.putAll(searchBrandAndSpecList(category));
		}else {
			if(categoryList!=null && categoryList.size()>0){ //查询条件searchMap的category没有值，每点击商品分类，为第一次根据关键字keywords查询，如果第一次根据关键字查询得到的分类列表不为空
				map.putAll(searchBrandAndSpecList(categoryList.get(0))); 
			} 
		}
		return map;
	}
	
	/**
	 * 条件查询内容
	 * @param searchMap
	 * @return
	 */
	private Map searchList(Map searchMap){
		Map map = new HashMap<>();
		//设置高亮显示
		HighlightQuery query =  new SimpleHighlightQuery();//高亮显示查询
		HighlightOptions options = new HighlightOptions();
		options.addField("item_title");//高亮的域 前端显现的搜索关键字
		options.setSimplePrefix("<em style='color:red'>");//高亮设置
		options.setSimplePostfix("</em>");
		query.setHighlightOptions(options);
		
		
		//---1.根据关键字查询---
		String keywords = (searchMap.get("keywords")+"").replace(" ", "");//去除关键字中的空格
		Criteria criteria = new Criteria("item_keywords").is(keywords);
		query.addCriteria(criteria);
		System.out.println("searchList1...");
		//---2.过滤---
		//2.1 分类过滤（商品的分类，category）
		if(searchMap.get("category")!=null && !"".equals(searchMap.get("category"))) {
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			FilterQuery filterQuery = new SimpleFilterQuery();
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		//2.2 品牌过滤(brand)
		if(searchMap.get("brand")!=null && !"".equals(searchMap.get("brand"))) {
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			FilterQuery filterQuery = new SimpleFilterQuery();
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		System.out.println("searchList2...");
		//2.3 规格过滤
		if (searchMap.get("spec")!=null && !"".equals(searchMap.get("spec"))) {
			Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
			for(String key:specMap.keySet()) {
				Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
				FilterQuery filterQuery = new SimpleFilterQuery();
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		System.out.println("searchList3...");
		//2.4 价格过滤
		if (searchMap.get("price") !=null && !"".equals(searchMap.get("price"))) {
			 String[] priceStr = ((String) searchMap.get("price")).split("-");
			 //最低价格
			 Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(priceStr[0]);
			 FilterQuery filterQuery = new SimpleFilterQuery();
			 filterQuery.addCriteria(filterCriteria);
			 query.addFilterQuery(filterQuery);
			 //最高价格
			 if (!"*".equals(priceStr[1]) && priceStr[1]!= null) {
				 Criteria filterCriteria2 = new Criteria("item_price").lessThanEqual(priceStr[1]);
				 FilterQuery filterQuery2 = new SimpleFilterQuery();
				 filterQuery.addCriteria(filterCriteria2);
				 query.addFilterQuery(filterQuery2);
			}
		}
		//2.5 分页
		Integer pageNum = 1;//默认当前页为第一页
		Integer pageSize = 20;
		if (searchMap.get("pageNum")!=null && !"".equals(searchMap.get("pageNum"))) {//当前页
			pageNum = Integer.valueOf(searchMap.get("pageNum")+"");
		}
		if (searchMap.get("pageSize")!=null && !"".equals(searchMap.get("pageSize"))) { //每页大小
			 pageSize = Integer.valueOf(searchMap.get("pageSize")+"");
		}
		query.setOffset((pageNum-1)*pageSize);
		query.setRows(pageSize);
		System.out.println("searchList5......");
		//2.5 根据价格排序过滤
		String sortName = searchMap.get("sort")+"";//排序字段
		String sortType = searchMap.get("sortType")+"";//排序的方式
		if (sortName!=null && !"".equals(sortName)) {
			if (sortType!=null && "ASC".equals(sortType)) {
				Sort sort = new Sort(Direction.ASC,"item_"+sortName);//升序排序
				query.addSort(sort);
			}else if (sortType!=null && "DESC".equals(sortType)) {
				Sort sort = new Sort(Direction.DESC,"item_"+sortName);//降序排序
				query.addSort(sort);
			}
		}
		
		
			
		//3.高亮显示处理
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		System.out.println("000000");
		List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();//循环高亮入口集合
		for (HighlightEntry<TbItem> h: highlighted) {
			//获取原实体类
			TbItem item = h.getEntity(); 
			if (h.getHighlights().size()>0 && h.getHighlights().get(0).getSnipplets().size()>0) {
				//页面标题关键字高亮显示
				item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));//把高亮结果设置item中
			}
		}
		System.out.println("searchList6...");
		map.put("totalPage", page.getTotalPages());//总页数
		map.put("total", page.getTotalElements());//总记录数
		map.put("rows", page.getContent());//当前页的内容
		System.out.println("searchList7...");
		System.out.println(JSON.toJSONString(map));
		return map;		
	}
	
	/**
	 * 查询分类列表
	 * @param searchMap
	 * @return
	 */
	private List searchCatgoryList(Map searchMap) {
		List<String> list = new ArrayList<>();
		Query query = new SimpleQuery();
		//关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//设置分组选项
		GroupOptions groupOptions = new GroupOptions();
		groupOptions.addGroupByField("item_category"); //根据category分类
		query.setGroupOptions(groupOptions);
		//查询得到分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		//根据分组列获得分组集
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//得到分组结果入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//得到分组入口集合
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		//把分组结果封装到list中
		for (GroupEntry<TbItem> groupEntry : content) {
			list.add(groupEntry.getGroupValue());
		}
		return list;
	}
	
	/**
	 * 根据商品分类 从redis中获取品牌和规格列表
	 * @param category
	 * @return
	 */
	private Map searchBrandAndSpecList(String category) {
		Map map = new HashMap<>();
		System.out.println("方法执行。。。");
		//获取模板id
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		
		if (typeId!=null && !"".equals(typeId)) {
			//1.从redis中获取品牌
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
			map.put("brandList", brandList);
			System.out.println("存入缓存brandList...");
			//2.从redis中获取规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
			map.put("specList", specList);
			System.out.println("存入缓存specList");
			
		}
		return map;
	}

	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
		
	}

}
