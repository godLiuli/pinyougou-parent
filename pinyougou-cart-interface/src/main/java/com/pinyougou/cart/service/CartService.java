package com.pinyougou.cart.service;
/**
 * 购物车业务接口
 * @author lenovo
 *
 */

import java.util.List;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Cart;

public interface CartService {
	/**
	 * 获取原来的购物车，再把加入购物车的商品item和数量添加到原来的购物车，返回新的购物车集合
	 * @param cartList
	 * @param item
	 * @param num
	 * @return
	 */
	public List<Cart> addGoodsToCarList(List<Cart> cartList, Long itemId,Integer num);
	
	/**
	 * 从redis中获取购物车
	 * @param username
	 * @return
	 */
	public List<Cart> findCartListFromRedis(String username);
	
	/**
	 * 购物车保存到redis中
	 * @param username
	 * @param cartList
	 */
	public void saveCartListToRedis(String username,List<Cart> cartList);
	
	/**
	 * 合并购物车
	 * @param cartList1
	 * @param cartList2
	 * @return
	 */
	public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
