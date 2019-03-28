package com.pinyougou.manager.controller;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	@Autowired
	private Destination queueSolrDestination;//操作solr的消息目的地(点对点)
	@Autowired
	private Destination topicPageDestination;//操作静态页面的生成(搜索是个服务器集群，所有)发布/监听 
	@Autowired
	private JmsTemplate jmsTemplate;

	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods 
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){ //xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}
	
	@RequestMapping("/audit")
	public Result audit(Long[] ids,String auditStatus) {
		try {
			goodsService.update(ids, auditStatus);
			if ("2".equals(auditStatus) && auditStatus!=null) {//通过审核
				List<TbItem> list = goodsService.findItemListByGoodsIdAndStatus(ids, auditStatus);
				if (list.size()>0) {
					//itemSearchService.importList(list); //调用数据接口，实现数据导入solr操作
					
					//发送消息至目的地pinyougou_queue_solr
					//把list转为string 类型方便发送
					final String jsonList = JSON.toJSONString(list);
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							System.out.println("solr:发送消息。。。");
							return session.createTextMessage(jsonList);
						}
					});
				}else {
					System.out.println("list中没有数据导入solr中。。。。。。");
				}
				
				//审核通过后生成静态页面
				for(final Long id:ids) {
					//itemPageService.getItemHtml(id);
					jmsTemplate.send(topicPageDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(id+""); //发布消息
						}
					});
				}
			}
			
			
			return new Result(true, "操作成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "操作失败！");
		}
		
	}
	
}
