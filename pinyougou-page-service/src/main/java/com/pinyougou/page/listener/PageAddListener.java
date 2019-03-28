package com.pinyougou.page.listener;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.page.service.ItemPageService;


@Component
public class PageAddListener implements MessageListener{
	@Autowired
	private ItemPageService itemPageService;
		
	@Override
	public void onMessage(Message message) {
		TextMessage textMessage = (TextMessage)message;
		try {
			String goodIdStr = textMessage.getText();
			Long goodId = Long.parseLong(goodIdStr);
			boolean b = itemPageService.getItemHtml(goodId); //执行页面生成
			if (b) {
				System.out.println("静态页面<pagedir"+goodId+".html>生成成功。。。");
			}else {
				System.out.println("静态页面生成失败。。。");
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
