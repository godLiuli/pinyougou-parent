app.controller('itemController',function($scope,$http){
	$scope.addNum=function(i){
		$scope.num+=i;
		if($scope.num<=1){
			$scope.num=1;
		}
	}
	//选中规格
	$scope.spec={};
	$scope.addSpec=function(name,value){
		$scope.spec[name]=value;
		searchSKU($scope.spec);
	}
	//判断当前是否选中
	$scope.isChecked=function(name,value){
		if($scope.spec[name]==value){
			return true;
		}
		return false;
	}
	//加载默认SKU(查询的时候已经排序了，是否默认)
	$scope.loadSKU=function(){
		$scope.sku=itemList[0];
		$scope.spec=JSON.parse(JSON.stringify(itemList[0].spec)) //选中默认对应规格选项
	}
	
	//匹配对象方法
	matchObject=function(map1,map2){
		for(var key in map1){        //可能map1是map2的子集
			if(map1[key]!=map2[key]){
				return false;
			}
		}
		for(var key in map2){
			if(map2[key]!=map1[key]){
				return false;
			}
		}
		return true;
	}
	//根据选中的规格，显示对应的SKU
	searchSKU=function(spec){
		for(var i=0;i<itemList.length;i++){
			if(matchObject(spec,itemList[i].spec)){
				$scope.sku=itemList[i];
				return;
			}
		}
		$scope.sku={"id":0,"title":"没有当前组合的商品！！！"}
	}
	//添加SKU到购物车
	$scope.addToCat=function(){
		alert("id"+$scope.sku.id);
		$http.get('http://localhost:9107/cart/addToCart.do?itemId='+$scope.sku.id+'&num='+$scope.num,{'withCredentials':true}).success(
			function(response){
				if(response.success){//添加成功跳转到cart.html      
					location.href="http://localhost:9107";
				}else{
					alert(response.message);
				}
			}	
		);
	}
});