package com.pinyougou.shop.controller;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
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
	private JmsTemplate jmsTemplate;
	
	@Autowired
	private Destination topicPageDeleteDestination;//删除页面

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
	public Result add(@RequestBody Goods goods){
		try {
			String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
			goods.getGoods().setSellerId(sellerId); //商品所属的商家
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
		//获取当前登陆的商家id
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		//通过传过来的商品id，从数据库中获取goods信息
		Goods goods2 = goodsService.findOne(goods.getGoods().getId());//商品唯一
		//双重校验
		if (!goods.getGoods().getSellerId().equals(sellerId) || !goods2.getGoods().getSellerId().equals(sellerId)) {
			return new Result(false, "非法操作！");
		}
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
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);
			//成功删除商品后，删除对应的商品详情页面
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {					
					return session.createObjectMessage(ids);
				}
			});
			
			return new Result(true, "删除成功!"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败!");
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
		//获取商家id  sellerId 
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.setSellerId(sellerId);	
		return goodsService.findPage(goods, page, rows);		
	}
	
	/**
	 * 提交商品进行审核
	 */
	@RequestMapping("/checkCommit")
	public Result updateAudit(Long[] ids) {
		try {
			goodsService.update(ids);
			return new Result(true, "提交成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "提交失败！");
		}
	}
	
	@RequestMapping("/marketable")
	public Result isMarketable(Long[] ids,String marketable) {
		try {
			goodsService.isMarket(ids,marketable);
			return new Result(true, "操作成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "操作失败！");
		}
	}
	
}
