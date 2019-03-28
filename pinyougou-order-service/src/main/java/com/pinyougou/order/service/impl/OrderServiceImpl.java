package com.pinyougou.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;

import util.IdWorker;
/**
 * 订单接口实现类
 * @author lenovo
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private IdWorker idWorker;
	
	@Autowired
	private TbOrderMapper orderMapper; //订单
	
	@Autowired
	private TbOrderItemMapper orderItemMapper; //订单详细
	
	@Autowired
	private TbPayLogMapper payLogMapper;//支付日志

	/**
	 * 添加订单
	 */
	@RequestMapping("/add")
	public void addOrder(TbOrder order) {
		//从redis中获取购物车
		List<Cart> cartList= (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		List<String> orderList = new ArrayList<>();
		double totalMoney=0; //总金额
		for(Cart cart:cartList){ 
		   long orderId = idWorker.nextId(); 
		   orderList.add(orderId+"");
		   System.out.println("sellerId:"+cart.getSellerId()); 
		   TbOrder tborder=new TbOrder();//新创建订单对象 
		   tborder.setOrderId(orderId);//订单 ID 
		   tborder.setUserId(order.getUserId());//用户名 
		   tborder.setPaymentType(order.getPaymentType());//支付类型 
		   tborder.setStatus("1");//状态：未付款 
		   tborder.setCreateTime(new Date());//订单创建日期 
		   tborder.setUpdateTime(new Date());//订单更新日期 
		   tborder.setReceiverAreaName(order.getReceiverAreaName());//地址 
		   tborder.setReceiverMobile(order.getReceiverMobile());//手机号 
		   tborder.setReceiver(order.getReceiver());//收货人 
		   tborder.setSourceType(order.getSourceType());//订单来源 
		   tborder.setSellerId(cart.getSellerId());//商家 ID 
		   
		   //循环购物车明细 
		   double money=0; 
		   for(TbOrderItem orderItem :cart.getOrderItemList()){   
		    orderItem.setId(idWorker.nextId()); 
		    orderItem.setOrderId( orderId  );//订单 ID 
		    orderItem.setSellerId(cart.getSellerId()); 
		    money+=orderItem.getTotalFee().doubleValue();//金额累加 
		    orderItemMapper.insert(orderItem);     //插入订单详细表，多个商品订单信息 
		   } 
		   tborder.setPayment(new BigDecimal(money));    
		   orderMapper.insert(tborder);  //插入订单表
		   totalMoney+=money;
		}
		if ("1".equals(order.getPaymentType())) { //微信支付
			TbPayLog payLog = new TbPayLog();
			String id = idWorker.nextId()+"";
			payLog.setOutTradeNo(id); //支付订单号
			payLog.setCreateTime(new Date()); //创建时间
			String orderIds = orderList.toString().replace("[", "").replace("]", "").replace(" ", ""); //每个订单号，订单号1,订单号2
			payLog.setOrderList(orderIds); //订单列表，每个cart对象编号,即每个订单
			payLog.setPayType("1"); //支付类型 1,微信支付
			payLog.setTotalFee( (long)(totalMoney*100 ) );//总金额(分) 
		    payLog.setTradeState("0");//支付状态 
		    payLog.setUserId(order.getUserId());//用户 ID   
		    System.out.println("============================");
		    int num = payLogMapper.insert(payLog);//插入到支付日志表   
		    System.out.println(num);
		    System.out.println("-----------------------------");
		    
		    redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);//放入缓存   
			 
		}
		
		
		//清除redis中的购物车
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}

	/**
	 * 从redis中获取支付日志
	 */
	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

	/**
	 * 跟新状态
	 */
	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		  //1.修改支付日志状态 
		  TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no); 
		  payLog.setPayTime(new Date()); 
		  payLog.setTradeState("1");//已支付 
		  payLog.setTransactionId(transaction_id);//交易号 
		  payLogMapper.updateByPrimaryKey(payLog);   
		  //2.修改订单状态 
		  String orderList = payLog.getOrderList();//获取订单号列表 
		  String[] orderIds = orderList.split(",");//获取订单号数组 
		   
		  for(String orderId:orderIds){ 
		   TbOrder order = 
		orderMapper.selectByPrimaryKey( Long.parseLong(orderId) ); 
		   if(order!=null){ 
		    order.setStatus("2");//已付款 
		    orderMapper.updateByPrimaryKey(order); 
		   }    
		  } 
		  //清除 redis 缓存数据   
		  redisTemplate.boundHashOps("payLog").delete(payLog.getUserId()); 
		 } 

}
