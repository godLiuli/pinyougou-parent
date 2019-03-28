package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.lf5.viewer.LogFactor5LoadingDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criterion;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbSellerMapper sellerMapper;
	@Autowired 
	private TbItemCatMapper itemCatMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0"); //0:未申请状态
		goods.getGoods().setIsMarketable(null);
		goodsMapper.insert(goods.getGoods());
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//tb_goods_desc 表的id
		goodsDescMapper.insert(goods.getGoodsDesc());
		//SKU保存到item表
		saveItemList(goods);
				
}			  
		 private void setItemValus(Goods goods,TbItem item) { 
			  item.setGoodsId(goods.getGoods().getId());//商品 SPU 编号 
			  item.setSellerId(goods.getGoods().getSellerId());//商家编号 
			  item.setCategoryid(goods.getGoods().getCategory3Id());//商品分类编号（3 级） 
			  item.setCreateTime(new Date());//创建日期 
			  item.setUpdateTime(new Date());//修改日期  
			  //sku表获取品牌名称
			  TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
			  item.setBrand(brand.getName());
			  //sku表店铺名称 nickName
			  TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
			  item.setSeller(seller.getNickName());
			  //sku表获取图片地址（取 spu 的第一个图片） 
			  String itemImages = goods.getGoodsDesc().getItemImages();
			  List<Map> imageList = JSON.parseArray(itemImages, Map.class);
			  if (imageList.size()>0) {
				  item.setImage ( (String)imageList.get(0).get("url")); 
			  }
			  //sku表三级分类
			  TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
			  item.setCategory(itemCat.getName());
			  
		 }


				
			



	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		goods.getGoods().setIsMarketable(null);
		goods.getGoods().setAuditStatus("0"); //0:未申请状态
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		//先删除item表中原有的记，然后在添加
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);
		//SKU保存到item表
		saveItemList(goods);
		
	}	
	
	//保存itemList的私有方法
	private void saveItemList(Goods goods) {
		if ("1".equals(goods.getGoods().getIsEnableSpec())){
			for (TbItem item : goods.getItemList()) {
				//标题
				String title=goods.getGoods().getGoodsName();
				Map<String, Object> specMap = JSON.parseObject(item.getSpec());
				for(String key:specMap.keySet()){ 
				     title+=" "+ specMap.get(key); 
				} 
				item.setStatus("1"); //默认启用  即上架
			    item.setTitle(title); 
			    setItemValus(goods,item); 
			    itemMapper.insert(item); 
			}    
			  
		 }else {
			 TbItem item=new TbItem(); 
			   item.setTitle(goods.getGoods().getGoodsName());//商品 KPU+规格描述串作为SKU 名称 
			   item.setPrice( goods.getGoods().getPrice() );//价格    
			   item.setStatus("1");//状态 
			   item.setIsDefault("1");//是否默认    
			   item.setNum(99999);//库存数量 
			   item.setSpec("{}");    
			   setItemValus(goods,item);      
			   itemMapper.insert(item); 
		}  
	}
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		//设置goods
		goods.setGoods(goodsMapper.selectByPrimaryKey(id));
		//设置goodsDesc
		goods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(id));
		//设置itemList
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemList(itemList);
		return goods;
	}

	/**
	 * 批量删除 逻辑删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods goods = new TbGoods();
			goods.setId(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKeySelective(goods);
		}		
	}
	
	/**
	 * 分页+查询
	 */
	@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusEqualTo(goods.getAuditStatus());
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
			criteria.andIsDeleteIsNull();
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
	/**
	 * 跟新TbGoods表的status
	 */
	@Override
	public void update(Long[] ids,String auditStatus) {
		for(Long id:ids) {
			TbGoods goods = new TbGoods();
			goods.setId(id);
			goods.setAuditStatus(auditStatus);
			goodsMapper.updateByPrimaryKeySelective(goods);
		}
	}

	/**
	 * 提交审核
	 */
	@Override
	public void update(Long[] ids) {
		for(Long id:ids) {
			TbGoods goods = new TbGoods();
			goods.setId(id);
			goods.setAuditStatus("1");
			goodsMapper.updateByPrimaryKeySelective(goods);
		}
	}

	@Override
	public void isMarket(Long[] ids,String marketable) {
		for(Long id:ids) {
			TbGoods goods = new TbGoods();
			goods.setId(id);
			goods.setIsMarketable(marketable);
			goodsMapper.updateByPrimaryKeySelective(goods);
		}
		
	}

	/**
	 * 根据商品id和状态查询sku列表
	 */
	@Override
	public List<TbItem> findItemListByGoodsIdAndStatus(Long[] ids, String status) {
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");
		criteria.andGoodsIdIn(Arrays.asList(ids));
		List<TbItem> list = itemMapper.selectByExample(example);
		return list;
	}
	
	
	
}
