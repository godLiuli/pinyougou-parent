//服务层
app.service('userService',function($http){
	//添加
	this.add=function(entity,smsCode){
		return $http.post('../user/add.do?smsCode='+smsCode, entity);
	} 
	//获取验证码
	this.getCode=function(phone){
		return $http.get('../user/sendCode.do?phone='+phone);
	}
	//获取订单
	this.findOrders=function(searchMap){
		return $http.post('../user/findOrderList.do', searchMap);
	}
	//获取登录名
    this.getLoginName=function(){
        return $http.get('../login/name.do');
    }

    this.findOneOrder=function (orderId) {
		return $http.get('../user/findOneOrder.do?id='+orderId)
    }

    this.makeComment=function (comment) {
		return $http.post('../user/makeComment.do',comment)
    }

    this.autoChangeStatus=function (orderId,status) {
		return $http.post('../user/autoChangeStatus.do?orderId='+orderId+'&status='+status)
    }
});
