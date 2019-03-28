/* 品牌控制 */
		app.controller("brandController",function($scope,$controller,brandService){
			$controller("baseController",{$scope:$scope}); //继承
			//保存
			$scope.save = function(){
				if(!confirm("确认保存吗？")){
					$scope.entity={};
					return;
				}
				var serviceObject = brandService.add($scope.entity);//默认 添加操作
				if($scope.entity.id != null){
					//说明是修改保存操作
					serviceObject=brandService.update($scope.entity);
				}
				serviceObject.success(
					function(response){
						$scope.entity={};
						if(response.success){
							$scope.reloadList();
							alert(response.message);
						}else{
							alert(response.message);
						}
					}		
				);
			}
			
			//findOne根据id查找
			$scope.findOne = function(id){
				brandService.findOne(id).success(
					function(response){
						$scope.entity = response;
					}		
				);
			}
			
			//删除
			$scope.delete = function(){
				if(!$scope.selectIds.length > 0) {
					alert("请选择记录！"); 
					return;
				}
				if(!confirm("确认删除所选记录吗？")){
					return;
				}
				brandService.delete($scope.selectIds).success(
					function(response){
						if(!response.success){
							alert(response.message);
							return;
						}
						alert(response.message);
						$scope.reloadList();
						
					}
				);
			}
			
			//查询条件对象
			$scope.searchEntity = {};
			//搜索
			$scope.search = function(page, rows){
				brandService.search(page, rows, $scope.searchEntity).success(
					function(response){
						$scope.brandList = response.rows;
						$scope.paginationConf.totalItems = response.total;
					}		
				);
			}
			
		});