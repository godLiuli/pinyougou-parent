package com.pinyougou.order.service;

import com.pinyougou.pojo.TbComment;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.OrderWithItems;
import entity.PageResult;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
	 *  @param
	 * @return
	 */
	public TbPayLog searchPayLogFromRedis(String userId);
	
	/**
	 * 跟新状态
	 * @param out_trade_no 支付日志号
	 * @param transaction_id 微信支付订单号
	 */
	public void updateOrderStatus(String out_trade_no,String transaction_id);



	public PageResult findOrderList(Map<String,String> searchMap);

	public OrderWithItems findOneOrder(Long id);


	public void makeComment(TbComment comment);
	//页面自动将超时十五天没确认收货的改变订单状态为待评价
	public void autoChangeStatus(Long orderId,String status);


	//将数据返回给运营商后台
	public Map findDataForManager();


	public List<Map>findDataByDate(Date starDate,Date endDate);

}
