app.service('cartService',function($http){
	this.findCartList=function(){
		return $http.get('/cart/findCartList.do');
	}
	
	//添加商品至购物车
	this.addToCart=function(itemId,num){
		return $http.get('/cart/addToCart.do?itemId='+itemId+'&num='+num);
	}
	
	//总商品数和总价格
	this.sum=function(cartList){   
	  var totalValue={totalNum:0, totalMoney:0.00 };//合计实体 
	  for(var i=0;i<cartList.length;i++){ 
		  var cart=cartList[i]; 
	   for(var j=0;j<cart.orderItemList.length;j++){ 
	   		var orderItem=cart.orderItemList[j];//购物车明细 
	   		totalValue.totalNum+=orderItem.num; 
	   		totalValue.totalMoney+=orderItem.totalFee;
	   }
	  
	 }
	  return totalValue;
	}
	
	//获取当前登陆用户的所有收获地址
	this.findAddressList=function(){
		return $http.get('/address/findAddressList.do');

	}
	
	//保存订单 
	 this.submitOrder=function(order){ 
	  return $http.post('order/add.do',order);   
	 } 
});