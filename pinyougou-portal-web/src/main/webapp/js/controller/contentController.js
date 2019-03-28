app.controller('contentController',function($scope,$location,contentService){
	//根据广告分类id获取某类广告集
	$scope.content=[]; //每类广告集
	$scope.findByCategoryId=function(categoryId){
		contentService.findByCategoryId(categoryId).success(
			function(response){
				$scope.content[categoryId]=response;
			}
		);
	}
	
	//跳转搜索页面
	$scope.search=function(){
		location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords; 
	}
});