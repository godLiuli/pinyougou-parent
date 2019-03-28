package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;
import util.CookieUtil;

/**
 * 购物车控制层
 * @author lenovo
 *
 */
@RestController
@RequestMapping("/cart")
public class CartController {

	@Reference
	private CartService cartService;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	
	/**
	 * 从Cookie中获取购物车列表
	 * @return
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		String username=SecurityContextHolder.getContext().getAuthentication().getName();
		String cartListStr = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		if (cartListStr==null || "".equals(cartListStr)) {
			cartListStr="[]"; //当cartListStr没有值或者为空时候，JSON转化会出现异常
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListStr, Cart.class);
		
		if ("anonymousUser".equals(username)) {//用户未登录
			return cartList_cookie;
		}else {//已登陆
			List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
			
			if (cartList_cookie.size()>0) {
				//如果cookie中有值,合并购物车
				cartList_redis = cartService.mergeCartList(cartList_cookie, cartList_redis);
				//清空cookie中的购物车，
				CookieUtil.deleteCookie(request, response, "cartList");
				//合并后的购物车保存到redis中
				cartService.saveCartListToRedis(username, cartList_redis);
			}
			return cartList_redis;
		}	
	}
	
	/**
	 * 添加商品到购物车
	 * @param itemId
	 * @param num
	 * @return
	 */
	@RequestMapping("/addToCart")
	@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
	public Result addToCart(Long itemId,Integer num){

/*		response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105"); 
		response.setHeader("Access-Control-Allow-Credentials", "true"); 
*/		String username=SecurityContextHolder.getContext().getAuthentication().getName();
		try {
			//获取购物车
			List<Cart> cartList = findCartList();
			cartList = cartService.addGoodsToCarList(cartList, itemId, num);
			if ("anonymousUser".equals(username)) {//未登录,存入cookie
				CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
				System.out.println(username+":保存到cookie中。。。");
			}else {
				//2.已登陆,保存到redis中
				cartService.saveCartListToRedis(username, cartList);
				System.out.println(username+":保存到redis中。。。");
			}
			return new Result(true, "添加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}
}
