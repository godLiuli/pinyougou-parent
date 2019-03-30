package com.pinyougou.order.service.impl;

import java.math.BigDecimal;
import java.util.*;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.OrderWithItems;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.order.service.OrderService;
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
	@Autowired
	private TbSellerMapper sellerMapper;

	@Autowired
	private TbCommentMapper commentMapper;
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
			order.setUpdateTime(new Date());
		    orderMapper.updateByPrimaryKey(order); 
		   }    
		  } 
		  //清除 redis 缓存数据   
		  redisTemplate.boundHashOps("payLog").delete(payLog.getUserId()); 
		 }

	@Override
	public PageResult findOrderList(Map<String,String> searchMap) {
		List list=new ArrayList<OrderWithItems>();
		//状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价 status
		TbOrderExample example=new TbOrderExample();
			//查找未付款的订单，Status一次只能是一个值，根据值的不同进行判断筛选
		if ("1".equals(searchMap.get("status"))){
			example.createCriteria().andUserIdEqualTo((String) searchMap.get("username")).andStatusEqualTo("1");
		}
		//查找待发货订单，即已付款订单
		if ("3".equals(searchMap.get("status"))){
			example.createCriteria().andUserIdEqualTo((String) searchMap.get("username")).andStatusEqualTo("3");
		}
		//查找已发货订单待收货
		if ("4".equals(searchMap.get("status"))){
			example.createCriteria().andUserIdEqualTo((String) searchMap.get("username")).andStatusEqualTo("4");
		}
		//查找待评价订单
		if ("7".equals(searchMap.get("status"))){
			example.createCriteria().andUserIdEqualTo((String) searchMap.get("username")).andStatusEqualTo("7");
		}

		//根据以上筛选得到tborder的订单集合，遍历封装到组合实体类
		PageHelper.startPage(Integer.parseInt(searchMap.get("pageNum")),Integer.parseInt(searchMap.get("pageSize")));
		Page<TbOrder> page= (Page<TbOrder>) orderMapper.selectByExample(example);


		for (TbOrder tbOrder : page.getResult()) {
			OrderWithItems orderWithItems=new OrderWithItems();
			orderWithItems.setTbOrder(tbOrder);
			//根据tbOrder的订单id，去数据库中查询tbOrderItem
			Long orderId = tbOrder.getOrderId();

			TbOrderItemExample orderItemExample=new TbOrderItemExample();
			orderItemExample.createCriteria().andOrderIdEqualTo(orderId);
			List<TbOrderItem> tbOrderItems = orderItemMapper.selectByExample(orderItemExample);
			orderWithItems.setOrderItemList(tbOrderItems);


			//获取商户的昵称
			String sellerId = tbOrder.getSellerId();
			//数据库中有垃圾数据，sellerID为null，为了防止报错
			if (sellerId==null||sellerId==""){
				//为空做个说明，该商户撤离平台了~
				orderWithItems.setNickName("商户倒闭了~~~");
			}else {
				String nickName = sellerMapper.selectByPrimaryKey(sellerId).getNickName();
				orderWithItems.setNickName(nickName);
			}


				list.add(orderWithItems);
			}



		return new PageResult(page.getTotal(),list);
	}

	@Override
	public OrderWithItems findOneOrder(Long id) {
		OrderWithItems orderWithItems=new OrderWithItems();
		//获取Tborder对象
		TbOrder tbOrder = orderMapper.selectByPrimaryKey(id);
		orderWithItems.setTbOrder(tbOrder);
		//获取商家ID，为了获取NickName
		String sellerId = tbOrder.getSellerId();
		String nickName = sellerMapper.selectByPrimaryKey(sellerId).getNickName();
		orderWithItems.setNickName(nickName);

		//获取对应的sku集合，用作图片集合展示
		//根据tbOrder的订单id，去数据库中查询tbOrderItem
		Long orderId = tbOrder.getOrderId();
		TbOrderItemExample orderItemExample=new TbOrderItemExample();
		orderItemExample.createCriteria().andOrderIdEqualTo(orderId);
		List<TbOrderItem> tbOrderItems = orderItemMapper.selectByExample(orderItemExample);
		orderWithItems.setOrderItemList(tbOrderItems);

		return orderWithItems;
	}

	@Override
	public void makeComment(TbComment comment) {
		//生成评论的ID
		comment.setId(idWorker.nextId()+"");
		String orderid = comment.getOrderid();
		TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.valueOf(orderid));
		//状态码8表示已评价~
		tbOrder.setStatus("8");
		tbOrder.setUpdateTime(new Date());
		orderMapper.updateByPrimaryKey(tbOrder);
		//插入评论
		commentMapper.insert(comment);

	}

	@Override
	public void autoChangeStatus(Long orderId, String status) {
		System.out.println(orderId+"哈哈哈哈哈啊哈哈哈"+status);
		try {
			TbOrder tbOrder = orderMapper.selectByPrimaryKey(orderId);
			tbOrder.setStatus(status);
			tbOrder.setUpdateTime(new Date());
			orderMapper.updateByPrimaryKey(tbOrder);
		} catch (Exception e) {
			new RuntimeException("状态自动修改失败！");
			e.printStackTrace();
		}
	}


	/**
	 * 将各个数据封装给运营商后台
	 * {value:100, name:'待付款订单'},status 1
	 {value:200, name:'待发货订单'},status 3
	 {value:300, name:'已发货订单'},status 4
	 {value:400, name:'已完成订单'},status 8
	 {value:500, name:'已关闭订单'}status 6
	 * @return
	 */
	@Override//获取此时此刻前后24小时的数据
	public Map findDataForManager() {
		Calendar calendar =Calendar.getInstance();
		Map map=new HashMap();
		TbOrderExample example1=new TbOrderExample();
		example1.createCriteria().andStatusEqualTo("1").andUpdateTimeBetween(new Date(new Date().getTime()-24*3600*1000),new Date(new Date().getTime()));
		int num1 = orderMapper.selectByExample(example1).size();//待付款

		TbOrderExample example2=new TbOrderExample();
		example2.createCriteria().andStatusEqualTo("3").andUpdateTimeBetween(new Date(new Date().getTime()-24*3600*1000),new Date(new Date().getTime()));
		int num2 = orderMapper.selectByExample(example2).size();//待发货

		TbOrderExample example3=new TbOrderExample();
		example3.createCriteria().andStatusEqualTo("4").andUpdateTimeBetween(new Date(new Date().getTime()-24*3600*1000),new Date(new Date().getTime()));
		int num3 = orderMapper.selectByExample(example3).size();//已发货订单数

		TbOrderExample example4=new TbOrderExample();
		example4.createCriteria().andStatusEqualTo("8").andUpdateTimeBetween(new Date(new Date().getTime()-24*3600*1000),new Date(new Date().getTime()));
		int num4 = orderMapper.selectByExample(example4).size();//已完成货订单数

		TbOrderExample example5=new TbOrderExample();
		example5.createCriteria().andStatusEqualTo("6").andUpdateTimeBetween(new Date(new Date().getTime()-24*3600*1000),new Date(new Date().getTime()));
		int num5 = orderMapper.selectByExample(example5).size();//交易完成订单数

		map.put("num1",num1);
		map.put("num2",num2);
		map.put("num3",num3);
		map.put("num4",num4);
		map.put("num5",num5);
		return map;
	}

	@Override
	public List<Map> findDataByDate(Date startDate, Date endDate) {
		List<Map>list=new ArrayList<>();
		//前后天数
		int days= (int) ((endDate.getTime()-startDate.getTime())/(1000*3600*24))+1;
		Map map1=new HashMap();
		map1.put("name","待付款订单");
		map1.put("type","bar");
		map1.put("stack","总量");
		List list1=new ArrayList();
		list1 = getListSizeByDate(list1, startDate, days, "1");
		map1.put("data",list1);
		list.add(map1);

		Map map2=new HashMap();
		map2.put("name","待发货订单");
		map2.put("type","bar");
		map2.put("stack","总量");
		List list2=new ArrayList();
		list2 = getListSizeByDate(list2, startDate, days, "3");
		map2.put("data",list2);
		list.add(map2);

		Map map3=new HashMap();
		map3.put("name","已发货订单");
		map3.put("type","bar");
		map3.put("stack","总量");
		List list3=new ArrayList();
		list3 = getListSizeByDate(list3, startDate, days, "4");
		map3.put("data",list3);
		list.add(map3);


		Map map4=new HashMap();
		map4.put("name","已完成订单");
		map4.put("type","bar");
		map4.put("stack","总量");
		List list4=new ArrayList();
		list4 = getListSizeByDate(list4, startDate, days, "8");
		map4.put("data",list4);
		list.add(map4);

		Map map5=new HashMap();
		map5.put("name","已发货订单");
		map5.put("type","bar");
		map5.put("stack","总量");
		List list5=new ArrayList();
		list5 = getListSizeByDate(list5, startDate, days, "6");
		map5.put("data",list5);
		list.add(map5);

		System.out.println(list);
		return list;
	}

	//集合，起始日，循环次数，状态码
	public List getListSizeByDate(List list,Date start,int days,String status){
		Long second=(long)3600*24*1000;//一天的秒数

		for (int i = 0; i <days; i++) {
			Date st=new Date(start.getTime()+second*i);
			Date end=new Date(start.getTime()+second*(i+1)-1);
			TbOrderExample example=new TbOrderExample();
			example.createCriteria().andStatusEqualTo(status).andUpdateTimeBetween(st,end);
			System.out.println(st+"----------------"+end);
			int no=orderMapper.selectByExample(example).size();
			list.add(no);
		}

			return list;
	}


}
