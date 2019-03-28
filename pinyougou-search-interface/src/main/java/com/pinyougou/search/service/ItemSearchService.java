package com.pinyougou.search.service;
/**
 * 搜索接口
 * @author lenovo
 *
 */

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbItem;

public interface ItemSearchService {
	/**
	 * 根据复制域item_keywords 来搜索，复制域包括(item_goodsid,item_category,item_brand,item_seller)
	 * @param serachMap
	 * @return
	 */
	public Map<String,Object> search(Map serachMap);
	
	/**
	 * 更新solr数据
	 * @param list
	 */
	public void importList(List list);
}
