app.controller('searchController',function($scope,$sce,$location,searchService){
	$scope.list={"brandList":[]};
	$scope.search=function(){
		searchService.search($scope.searchMap).success(
			function(response){
				$scope.list=response;
				//构建分页标签函数
				pageLabel();
			}
		);
	}
	
	//搜索条件
	$scope.searchMap={"keywords":'',"category":'',"brand":'',"price":'',"spec":{},"pageNum":1,"pageSize":40,"sort":'',"sortType":''};
	//添加搜索条件
	$scope.addSearchItem=function(key,value){
		if(key=='category' || key=='brand' || key=='price'){
			$scope.searchMap[key]=value;
		}else{
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();//添加搜索条件之后需要重新查
	}
	//移除搜索条件
	$scope.removeSearchItem=function(key){
		if(key=='category' || key=='brand' || key=='price'){
			$scope.searchMap[key]='';
		}else{
			delete $scope.searchMap.spec[key];
		}
		$scope.search();
	}
	
	//构建分页标签函数(显示5页)
	pageLabel=function(){
		$scope.pages=[];
		var startPage=1;
		var endPage=$scope.searchMap.totalPage;
		//总页数大于5页
		if($scope.list.totalPage>5){ 
			if($scope.searchMap.pageNum<3){ //当前页小于3,1  2  345          96 97 98 99 100
				endPage=5;
			}else if($scope.searchMap.pageNum>=$scope.list.totalPage-1){
				startPage=$scope.list.totalPage-4;
				endPage=$scope.list.totalPage;
			}else{
				startPage=$scope.searchMap.pageNum-2;
				endPage=$scope.searchMap.pageNum+2;
			}
		}else{  //总页数小于5页
			startPage=1;
			endPage=$scope.list.totalPage;
		}
		for(var i=startPage;i<=endPage;i++){
			$scope.pages.push(i);
		}
	}
	//点击页码标签
	$scope.queryByPage=function(pageNum){
		$scope.searchMap.pageNum=pageNum;
		$scope.search();
	}
	//清空gotoPage值
	$scope.clearPage=function(){
		$scope.gotoPage=1;
	}
	
	//根据字段排序
	$scope.sort=function(sortName,sortType){
		$scope.searchMap.sort=sortName;
		$scope.searchMap.sortType=sortType;
		$scope.search();
	}
	
	//隐藏品牌列表
	$scope.hideBrandList=function(){
		var keywords=$scope.searchMap.keywords;
		for(var i=0;i<$scope.list.brandList.length;i++){
			if($scope.searchMap.keywords.indexOf($scope.list.brandList[i].text)>=0){
				return true;
			}
		}
		return false;
	}
	//搜索页面跳转接受
	$scope.loadkeywords=function(){
		var keywords=$location.search()['keywords'];
		$scope.searchMap.keywords=keywords;
		$scope.search();
	}
});