package com.pinyougou.page.listener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.page.service.ItemPageService;

@Component
public class PageDeleteListener implements MessageListener{
	@Autowired
	private ItemPageService itemPageService;
		
	@Override
	public void onMessage(Message message) {
		ObjectMessage idsStr = (ObjectMessage) message;//获取
		try {
			Long[] ids = (Long[]) idsStr.getObject();
			boolean b = itemPageService.delItemHtml(ids);
			if (b) {
				System.out.println("删除商品详情页面成功。。。");
			}else {
				System.out.println("删除商品详情页面失败。。。");
			}
			
		} catch (JMSException e) {
			e.printStackTrace();
			System.out.println("删除商品详情页面失败。。。");
		}
	}

}
