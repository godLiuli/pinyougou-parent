/*登陆服务*/
app.service("loginService",function($http){
	//获取登陆名
	this.getName=function(){
		return $http.get("/login/getName.do");
	}
});