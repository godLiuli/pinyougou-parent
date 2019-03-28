app.controller('loginController' ,function($scope,loginService){
	$scope.getLoginName=function(){
		loginService.getLoginName().success(
			function(response){
				$scope.loginUsername=response.username;
			}	
		);
	}
})