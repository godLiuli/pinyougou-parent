/*登陆控制*/
app.controller("loginController",function($scope,$controller ,loginService){
	$scope.getName=function(){
		loginService.getName().success(
				function(response){
					$scope.username=response.username;
				}
				
		);
	}	
});