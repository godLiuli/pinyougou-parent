package com.pinyougou.order.service;

import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;

/**
 * 订单接口
 * @author lenovo
 *
 */
public interface OrderService {
	/**
	 * 添加订单
	 * @param order
	 */
	public void addOrder(TbOrder order);
	
	/**
	 * 从redis中获取支付日志信息
	 *  @param uIdAndPlId
	 * @return
	 */
	public TbPayLog searchPayLogFromRedis(String userId);
	
	/**
	 * 跟新状态
	 * @param out_trade_no 支付日志号
	 * @param transaction_id 微信支付订单号
	 */
	public void updateOrderStatus(String out_trade_no,String transaction_id);
}
