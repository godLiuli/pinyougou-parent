 //控制层 
app.controller('userController' ,function($scope,$controller,$interval,userService){	
	
	$controller('baseController',{$scope:$scope});//继承
	$scope.entity={};
    //添加
	$scope.add=function(){
		if($scope.entity.password!=$scope.confirmPassword){
			alert("密码不一致！");
			return;
		}
		if($scope.smsCode==null || $scope.smsCode==""){
			alert("请输入验证码！！！");
			return;
		}
		userService.add($scope.entity,$scope.smsCode).success(
				
			function(response){
				alert(response.message);
				if(response.success){
					location.reload();
				}
			}	
		);
	}    
	
	//点击获取验证码
	/*$scope.getCode=function(){
		if($scope.entity.phone==null){
			alert("请输入手机号！！！");
			return;
		}
		userService.getCode($scope.entity.phone).success(
			function(response){
				if(!response.success){
					alert(response.message);
				}
			}	
		);
	}*/
		//倒计时180秒
		$scope.btnMsg = "获取验证码";
        //定义一个标识
        var active = true;
        //初始化时间
        var time = 180;
        //初始化定时器
        var secondInterval;
        $scope.getCode = function() {
        	if(active == false) {
                return;
            }
			if($scope.entity.phone==null){
				alert("请输入手机号！！！");
				return;
			}
			userService.getCode($scope.entity.phone).success(
					function(response){
						if(!response.success){//手机号格式不正确
							alert(response.message);
						}else{//手机号格式正确
							//显示倒计时
				            secondInterva = setInterval(function(){
				                if(time <= 0){
									//按钮可用
									$scope.isDisabled=false;
				                    //允许重新发送验证码
				                    $scope.btnMsg = "重发验证码";
				                    //强制更新视图
				                    $scope.$digest();
				                    active = true;
				                    time = 180;
				                    //关闭定时器
				                    clearInterval(secondInterva);
				                    secondInterva = undefined;
										
				                }else {
									//按钮不可用
									$scope.isDisabled=true;
				                    $scope.btnMsg = time + "秒可后重发";
				                    //强制跟新视图
				                    $scope.$digest();
				                    time--;
				                }
				            },1000);
						}
			});
		}
});	
