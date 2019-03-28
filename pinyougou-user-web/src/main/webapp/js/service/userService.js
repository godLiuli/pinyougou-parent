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
});
