package com.pinyougou.search.controller;
/**
 * 搜索
 * @author lenovo
 *
 */

import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;
@RestController
@RequestMapping("/search")
public class ItemSearchController {
	@Reference
	private ItemSearchService ItemSearchService;
	/**
	 * 搜索
	 * @param searchMap
	 * @return
	 */
	@RequestMapping("/itemSearch")
	public Map<String,Object> itemSearch(@RequestBody Map searchMap) {
		return ItemSearchService.search(searchMap);
	}
}
