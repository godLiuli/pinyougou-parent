package com.pinyougou.pay.service;
/**
 * 微信支付接口
 * @author lenovo
 *
 */

import java.util.Map;

public interface WeixinPayService {
	/**
	 * 生成微信支付二维码
	 * @param out_trade_no 商户订单号
	 * @param total_fee  标价金额
	 * @return
	 */
	public Map createNative(String out_trade_no,String total_fee);
	
	/**
	 * 查询支付是否成功
	 * @param out_trade_no
	 * @return
	 */
	public Map queryPayStatus(String out_trade_no);
}
