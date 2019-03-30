package com.pinyougou.cart.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;

import entity.Result;

/**
 * 订单控制层
 * @author lenovo
 *
 */
@RestController
@RequestMapping("/order")
public class OrderController {
	
	@Reference
	private OrderService orderService;
	
	@RequestMapping("/add")
	public Result add(@RequestBody TbOrder order) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		order.setUserId(username); //订单所属用户
		order.setSourceType("2");//订单来源pc
		try {
			orderService.addOrder(order);
			return new Result(true, "添加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}
}
