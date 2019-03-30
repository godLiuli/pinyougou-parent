package com.pinyougou.user.controller;
import java.util.List;
import java.util.Map;


import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbComment;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojogroup.OrderWithItems;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;

import entity.PageResult;
import entity.Result;
import util.IdWorker;
import util.PhoneFormatCheckUtils;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference(timeout = 10000)
	private UserService userService;
	@Reference(timeout = 100000)
	private OrderService orderService;

	/**
	 * 增加
	 * @param user
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbUser user,String smsCode){
		try {
			boolean checked = userService.checkCode(user.getPhone(),smsCode);
			if (!checked) {
				return new Result(false, "验证码错误！！！");
			}
			userService.add(user);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param user
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbUser user){
		try {
			userService.update(user);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	


	
	/**
	 * 点击获取验证码
	 * @param phone
	 * @return
	 */
	@RequestMapping("/sendCode")
	public Result sendCode(String phone) {
		if (!PhoneFormatCheckUtils.isPhoneLegal(phone)) { //校验手机号格式是否正确
			return new Result(false, "请输入正确的手机号！！！");
		}
		try {
			userService.sendCode(phone);
			return new Result(true, "");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "失败！！！");
		}
	}


	@RequestMapping("/findOrderList")
	public PageResult findOrderList(@RequestBody Map<String,String> searchMap){
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		if ("".equals(searchMap.get("pageSize"))||searchMap.get("pageSize")==null){
			searchMap.put("pageSize","10");
		}
		if ("".equals(searchMap.get("pageNum"))||searchMap.get("pageNum")==null){
			searchMap.put("pageNum","1");
		}


		searchMap.put("username",username);
		return  orderService.findOrderList(searchMap);

	}

	@RequestMapping("/findOneOrder")
	public OrderWithItems findOneOrder(Long id){
		return	orderService.findOneOrder(id);
	}


	/**
	 * 评论方法，根据订单ID添加评论数据，在此不细分sku，默认item集合中的第一个数据为sku，同时订单表绑定itemID，和orderID
	 * @param comment
	 * @return
	 */
	@RequestMapping("/makeComment")
	public Result makeComment(@RequestBody TbComment comment){
		try {
			orderService.makeComment(comment);
			return new Result(true,"添加评论成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(true,"添加评论失败！！！");
		}
	}

	@RequestMapping("/autoChangeStatus")
	public void autoChangeStatus(Long orderId, String status){
		orderService.autoChangeStatus(orderId,status);
	}

	
}
