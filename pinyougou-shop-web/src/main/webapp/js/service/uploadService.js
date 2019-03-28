app.service('uploadService',function($http){
	this.uploadFile=function(){
		var formData = new FormData();
		formData.append("file",file.files[0]);
		return $http({
			method:'post',
			url:'/upload.do',
			data:formData,
			headers:{'content-Type':undefined}, //设置表单不是普通文本表单
			transformRequest:angular.identity //序列号
		});
	}
});