package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;

import util.HttpClient;
/**
 * 微信支付实现层
 * @author lenovo
 *
 */
@Service(timeout=6000)
public class WinxinPayServiceImpl implements WeixinPayService{
	
	@Value("${appid}")
	private String appid;
	
	@Value("${partner}")
	private String partner;
	
	@Value("${partnerkey}")
	private String partnerkey;
	
	@Value("${notifyurl}")
	private String notify_url;
	/**
	 * 生成支付二维码
	 */
	@Override
	public Map createNative(String out_trade_no, String total_fee) {
		//1.创建参数
		Map<String, String> param = new HashMap<>();
		param.put("appid", appid); //公众账号ID	
		param.put("mch_id", partner); //商户号		
		param.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串
		param.put("body", "品优购"); //商品描述	
		param.put("out_trade_no", out_trade_no); //商户订单号	
		param.put("total_fee", total_fee); //标价金额	
		param.put("spbill_create_ip", "127.0.0.1"); //终端IP
		param.put("notify_url", notify_url); //通知地址	
		param.put("trade_type", "NATIVE"); //交易类型	
		//2.生成要发送的xml
		try {
			String xmlParam=WXPayUtil.generateSignedXml(param, partnerkey); //把map转化为String类型的xml
			System.out.println(xmlParam);
			HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();	 //发送请求
			
			//3.获取结果
			String result = client.getContent();
			System.out.println(result);
			Map<String,String> resultMap = WXPayUtil.xmlToMap(result);//把返回的xml结果 转化为map 
			Map<String, String> map = new HashMap<>();
			map.put("code_url", resultMap.get("code_url"));//返回结果中获取支付地址url,根据url生成支付二维码 
		    map.put("total_fee", total_fee);//总金额 
		    map.put("out_trade_no",out_trade_no);//订单号 
		    return map;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}
		
	}
	
	/**
	 * 查询订单支付是否成功
	 */
	@Override
	public Map queryPayStatus(String out_trade_no) {
		//1.封装请求参数
		Map param = new HashMap<>();
		param.put("appid", appid);
		param.put("mch_id", partner);
		param.put("out_trade_no", out_trade_no);
		param.put("nonce_str", WXPayUtil.generateNonceStr());
		try {
			//2.发送请求
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
			
			//3.获取结果
			String result = client.getContent();
			System.out.println(result);
			Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
			return resultMap;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return null;
	}

}
