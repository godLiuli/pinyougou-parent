package com.pinyougou.solr.listener;

import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
@Component
public class MyMessageListener implements MessageListener {
	@Autowired
	private ItemSearchService itemSearchService;
	
	@Override
	public void onMessage(Message message) {
		TextMessage textMessage = (TextMessage)message; //发送的是json类型的字符串
		try {
			String listText = textMessage.getText(); //获取内容
			List<TbItem> list = JSON.parseArray(listText, TbItem.class);//转换传递过来的消息为list集合，即SKU集合
			//保存至索引库
			for(TbItem item:list) {
				Map specMap = JSON.parseObject(item.getSpec()); //item中字段specMap赋值
				item.setSpecMap(specMap);
			}
			itemSearchService.importList(list);
			System.out.println("添加到solr成功。。。");
		} catch (JMSException e) {
			e.printStackTrace();
			System.out.println("添加到solr失败。。。");
		}

	}

}
