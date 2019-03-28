app.service('contentService',function($http){
	this.findByCategoryId=function(category){
		return $http.get('/content/findByCategoryId.do?categoryId='+category);
	}	
});