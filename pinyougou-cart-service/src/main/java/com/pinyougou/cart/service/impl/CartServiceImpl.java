package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

/**
 * 购物车实现类
 * @author lenovo
 *
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {	
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 添加商品到购物车
	 */
	@Override
	public List<Cart> addGoodsToCarList(List<Cart> cartList, Long itemId, Integer num) {
		//1.根据商品id获取商家名称，对应item表的seller字段
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if (item==null) {
			throw new RuntimeException("商品不存在");
		}
		if (!"1".equals(item.getStatus())) {//非上架状态
			throw new RuntimeException("商品处于非上架状态");
		}
		String sellerId = item.getSellerId();
		
		//2判断商家是否在购物车中存在
		Cart cart = searchCartBySellerId(cartList, sellerId);
		if (cart==null) {//2.1不存在，则创建该商家的购物车，添加商品
			
			cart = new Cart();
			TbOrderItem orderItem = saveOrderItem(item, num);
			List<TbOrderItem> orderItemList = new ArrayList<>();
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			cart.setSellerId(item.getSellerId());
			cart.setSellerName(item.getSeller());
			cartList.add(cart);
			return cartList;
		}else {//2.2存在
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
			//2.2.1判断当前添加的商品在该商家的购物车中是否存在
			if (orderItem==null) {
				//2.2.1.1不存在，添加商品
				cart.getOrderItemList().add(saveOrderItem(item, num));
				
			}else {
				//2.2.1.2存在，修改数量,更改价格
				orderItem.setNum(orderItem.getNum()+num);
				orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*item.getPrice().doubleValue()));
				if (orderItem.getNum()<=0) {
					cart.getOrderItemList().remove(orderItem);//如果该商品的数量<=0,从orderitemList中移除该商品
					
				}
				if (!(cart.getOrderItemList().size()>0)) {//如果cart中orderItemList没有商品，移除cart
					cartList.remove(cart);
				}
			}
			return cartList;
		}
	}

	/**
	 * 判断购物车列表中是否含有该商家的购物车
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	private Cart searchCartBySellerId(List<Cart> cartList,String sellerId) {
		for (Cart cart : cartList) {
			if (cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;
	}
	
	/**
	 * 根据商品id判断cart的orderItemList中
	 * 是否存在该商品
	 * @param orderItemList
	 * @param itemId
	 * @return
	 */
	private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
		for (TbOrderItem orderItem : orderItemList) {
			if (orderItem.getItemId().equals(itemId)) {
				return orderItem;
			}
		}
		return null;
	}
	
	/**
	 * 保存商品到商家购物车的OrderItemList中
	 * @param orderItem
	 * @param item
	 * @return
	 */
	private TbOrderItem saveOrderItem(TbItem item,Integer num) {
		if (num<0) {
			throw new RuntimeException("数量");
		}
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setItemId(item.getId());   //商品SKU的id
		orderItem.setGoodsId(item.getGoodsId()); //SPU的id
		orderItem.setTitle(item.getTitle()); //SKU的标题
		orderItem.setPrice(item.getPrice()); //商品单价
		orderItem.setNum(num); //商品数量
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
		orderItem.setSellerId(item.getSellerId());
		orderItem.setPicPath(item.getImage());
		return orderItem;
	}

	@Override
	public List<Cart> findCartListFromRedis(String username) {
		System.out.println(username+":购物车从redis中获取。。。");
		List<Cart> cartList=(List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		if (cartList==null) {
			cartList=new ArrayList<>();
		}
		return cartList;
	}

	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		redisTemplate.boundHashOps("cartList").put(username, cartList);
		System.out.println(username+":购物车保存到redis中获取。。。");
		
	}
	
	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		for(Cart cart : cartList2) {
			for(TbOrderItem orderItem : cart.getOrderItemList()) {
				cartList1 = addGoodsToCarList(cartList1, orderItem.getItemId(), orderItem.getNum());
			}
		}
		System.out.println("已合并购物车。。。");
		return cartList1;
	}
}
