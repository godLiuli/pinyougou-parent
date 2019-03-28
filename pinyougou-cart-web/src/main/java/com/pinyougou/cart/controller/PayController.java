package com.pinyougou.cart.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;

import entity.Result;
import util.IdWorker;

/**
 * 支付控制层
 * @author lenovo
 *
 */
@RestController
@RequestMapping("/pay")
public class PayController {
	
	@Reference
	private WeixinPayService weixinPayService;
	@Reference
	private OrderService orderService;
	
	/**
	 * 生成二维码
	 * @param out_trade_no
	 * @param total_fee
	 * @return
	 */
	@RequestMapping("/createNative")
	public Map createNative() {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		//从redis中获取支付日志
		TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
		Map map=null;
		if (payLog!=null) {
			map = weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
		}
		return map;
		
	}
	
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		Result result=null;
		int time=0;
		while (true) {
			Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
			if (map==null) {
				result=new Result(false, "支付失败");
				break;
			}
			if ("SUCCESS".equals(map.get("trade_state"))) {
				System.out.println("支付成功");
				result=new Result(true, "支付成功");
				//修改订单状态 
			    orderService.updateOrderStatus(out_trade_no,map.get("transaction_id"));
				break;
			}
			
			try {
				Thread.sleep(3000);//每三秒执行一次询问
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
			time++;
			if (time>100) {
				result=new Result(false, "二维码超时");
				break;
			}
		}
		return result;
	}
}
