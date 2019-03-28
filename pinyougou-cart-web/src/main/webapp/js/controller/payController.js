app.controller('payController',function(payService,$scope,$location){
	$scope.createNative=function(){
		payService.createNative().success(
			function(response){
				$scope.money=(response.total_fee/100).toFixed(2);
				$scope.out_trade_no=response.out_trade_no;
				//创建支付二维码
				var qr = new QRious(
					{
						element:document.getElementById("payCode"),
						size:250,
						level:"H",
						value:response.code_url
					}
				);
				queryPayStatus($scope.out_trade_no);
			}
		);
	}
	
	//询问订单支付状态
	queryPayStatus=function(out_trade_no){
		payService.queryPayStatus(out_trade_no).success(
			function(response){
				if(response.success){
					location.href="paysuccess.html#?money="+$scope.money;
				}else if("二维码超时"==response.message){
					$scope.error_code="二维码已过期，刷新页面重新获取二维码";
				}else{
					location.href="payfail.html";
				}
			}
		);
	}

	//支付成功后获取订单金额
	$scope.getMoney=function(){
		return $location.search()['money'];
	}
});