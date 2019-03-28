package com.pinyougou.page.service.impl;

import java.io.File;
import java.io.FileWriter;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;
import freemarker.template.Configuration;
import freemarker.template.Template;

@Service
public class ItemPageServiceImpl implements ItemPageService{
	@Value("${pageDir}")
	private String pagedir;
	
	@Autowired
	private TbGoodsMapper goodsMapper;
	
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	
	@Autowired
	private FreeMarkerConfig freeMarkerConfig;
	
	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Override
	public boolean getItemHtml(long goodId) { //根据商品id获取goods 和goodsDesc
		//创建Configuration对象
		Configuration config = freeMarkerConfig.getConfiguration();
		try {
			Template template = config.getTemplate("item.ftl");
			Map map = new HashMap();
			TbGoods goods = goodsMapper.selectByPrimaryKey(goodId); //获取goods
			map.put("goods", goods);
			TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodId);
			map.put("goodsDesc", goodsDesc);
			
			//商品123级分类
			String category1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
			String category2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
			String category3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
			map.put("category1",category1 );
			map.put("category2", category2);
			map.put("category3", category3);
			
			//商品的SKU
			TbItemExample example = new TbItemExample();
			Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(goodId);
			criteria.andStatusEqualTo("1");
			example.setOrderByClause("is_default desc"); //根据 “默认排序”
			List<TbItem> itemList = itemMapper.selectByExample(example);
			map.put("itemList", itemList);
			Writer out = new FileWriter(pagedir+goodId+".html");//对应目录下 含名的html文件
			template.process(map, out);
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	@Override
	public boolean delItemHtml(Long[] ids) {
		try{
			for(long goodId:ids){
				new File(pagedir+goodId+".html").delete();
			} 
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

}
