app.controller('cartController',function(cartService,$scope){
	$scope.findCartList=function(){
		cartService.findCartList().success(
			function(response){
				$scope.cartList=response;
				$scope.totalValue=cartService.sum($scope.cartList);
			}
		);
	}
	
	
	//加减商品数量
	$scope.addNum=function(itemId,num){
		cartService.addToCart(itemId,num).success(
			function(response){
				if(response.success){
					$scope.findCartList();
				}else{
					alert(response.message);
				}
			}
		);
	}
	
	//获取登陆用户所有地址
	$scope.findAddressList=function(){
		cartService.findAddressList().success(
			function(response){
				$scope.addressList=response;
				for(var i=0;i<$scope.addressList.length;i++){
					if($scope.addressList[i].isDefault=="1"){
						$scope.address=$scope.addressList[i];
					}
				}
			}
		);
	}
	
	//选择地址 
	 $scope.selectAddress=function(address){ 
		 $scope.address=address;   
	 }
	
	//是否是当前选中
	$scope.isSeletedAddress=function(address){
		if(address==$scope.address){
			return true;
		}else{
			return false;
		}
	}
	
	$scope.order={};
	//付款方式
	$scope.order.paymentType="1";
	$scope.payType=function(type){
		$scope.order.paymentType=type;
	}
	
	
	//提交订单
	$scope.submitOrder=function(){
		$scope.order.receiverAreaName=$scope.address.address;//地址 
		$scope.order.receiverMobile=$scope.address.mobile;//手机 
		$scope.order.receiver=$scope.address.contact;//联系人 
		  cartService.submitOrder( $scope.order ).success( 
				  function(response){ 
					  if(response.success){ 
						  //页面跳转 
						  if($scope.order.paymentType=='1'){//如果是微信支付，跳转到支付页面 
							  location.href="pay.html"; 
						  }else{//如果货到付款，跳转到提示页面 
							  location.href="paysuccess.html"; 
						  }      
					  }else{ 
						  alert(response.message); //也可以跳转到提示页面     
					  }     
				  });   
	}
});