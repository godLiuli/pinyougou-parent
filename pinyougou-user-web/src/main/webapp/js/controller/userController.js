 //控制层 
app.controller('userController' ,function($scope,$controller,$interval,userService,$location){
	
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


		//获取登陆名
		$scope.getLoginName = function() {
					userService.getLoginName().success(function(response) {
						$scope.loginUsername = response.username;
			})
		}


		// 封装searchMap
		$scope.searchMap = {'status':'','pageNum':'1','pageSize':'5'};

		$scope.findOrders = function(status) {
			$scope.searchMap['status'] = status;
			userService.findOrders($scope.searchMap).success(function(response) {
				$scope.ordersList = response.rows;
				$scope.paginationConf.totalItems = response.total;
			})
		}
		//分页切换页码
		$scope.search=function(pageNum,pageSize){

			$scope.searchMap["pageNum"]=pageNum;
			$scope.searchMap["pageSize"]=pageSize;
			// 查询我的订单
			$scope.findOrders($scope.searchMap['status'])

		}


		//待评价页面用于传递id到评价页面，获取订单详细的数据，数据为组合实体类
		$scope.getOrder=function () {
			var orderId=$location.search()['orderId']
			userService.findOneOrder(orderId).success(function (response) {
				$scope.entity=response;
				//那到数据的时候，把相关信息封存到comment对象里
				$scope.comment['orderID']=orderId;
				$scope.comment['itemId']=response.orderItemList[0].itemId;

			})}


		//定义评价对象给页面绑定
		$scope.comment={}
		//评价方法
		$scope.makeComment=function () {
			//富文本编辑器的内容，保存到comment的对象中
			$scope.comment['comments']=editor.html()
			userService.makeComment($scope.comment).success(function (response) {
				if (response.success){
					alert(response.message)
					location.href="home-order-evaluate.html"
				}else {
					alert(response.message)
				}
			})
		}

		//页面倒计时封装str，传入一个集合，将所有的str都动起来,list是集合，proper是字段
		$scope.getTimeStr=function(order,timeProper) {
				//所有状态在15天后都会系统自动进行处理
				var allsecond=  Math.floor( ((new Date(order[timeProper]).getTime()+15*3600*24*1000)- new Date().getTime())/1000 );
				if (timeProper=='paymentTime'){//代发货时间的话点击提醒按钮，出现倒计时，表示商家三天内发货
					alert("已提醒商家！")
					allsecond=3*24*3600;
				}
				if (allsecond<0){
					//待收货界面显示的发货时间
					if (timeProper=='consignTime'){
						//修改对应订单为待评价
						userService.autoChangeStatus(order.orderId,'7')
					}else{
						order.timeStr='00.00.00'
					}



			   }else {
				   time= $interval(function(){
					   allsecond=allsecond-1;
					   //为每一个item order添加这个玩意
					   order.timeStr=makeTime2Str(allsecond);

					   if(allsecond<=0){
						   $interval.cancel(time);
					   }

				   },1000 );
			   }
		}
		//确认收货
		$scope.receive=function (id) {
            userService.autoChangeStatus(id,'7').success(function () {
            	alert("感谢确认！")
                location.reload();
            })
        }

    //controller内部调用不需要$scope声明，方法和变量都是如此，$scope加上去是给外部页面调用或者使用的
    makeTime2Str=function (allsecond) {
        var days= Math.floor( allsecond/(60*60*24));//天数
        var hours= Math.floor( (allsecond-days*60*60*24)/(60*60) );//小数数
        var minutes= Math.floor(  (allsecond -days*60*60*24 - hours*60*60)/60    );//分钟数
        var seconds= allsecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
        var timeString="";
        if(days>0){
            timeString=days+"天 ";
        }
        return timeString+hours+":"+minutes+":"+seconds;

    }



});	
