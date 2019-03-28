var app = angular.module("pinyougou",[]);

//angularjs过滤器  --让html标签正常显示
app.filter('trustHtml',['$sce',function($sce){
	return function(data){
		return $sce.trustAsHtml(data);
	}
	
}]);
