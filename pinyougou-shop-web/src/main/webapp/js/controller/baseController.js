/*基本控制层*/
	app.controller("baseController",function($scope){
		
		//重新加载
		$scope.reloadList = function(){
			//切换页码
			$scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
			$("#selall").removeAttr("checked");
		}
		//配置分页控件
		$scope.paginationConf = {
			currentPage: 1, 
			totalItems: 10,
			itemsPerPage: 10,
			perPageOptions: [10, 20, 30, 40, 50],
			onChange: function(){
				$scope.reloadList();
			}
		} 
		
		//获取选中的记录
	    $scope.selectIds = [];
		$scope.updateSelectIds = function($event,id) {
			if($event.target.checked){
				$scope.selectIds.push(id);
			}else{
				var idIndex = $scope.selectIds.indexOf(id);//拿到id在数组中的索引
				$scope.selectIds.splice(idIndex, 1); //从数组中把id删除，parm1: 位置  parm2: 移除的个数
			}
		}
		//全选或全不选
		$scope.selectAll = function($event, brandList){
			var cbs = $(".cb");
			for (var i = 0; i < cbs.length; i++) {
				cbs[i].checked = $event.target.checked;
			}
			if($event.target.checked){
				for (var i=0; i<brandList.length; i++) {
					$scope.selectIds.push(brandList[i].id);
				}
			}else{
				$scope.selectIds = [];
			}
		}
		
		//jsonToString 
		$scope.jsonToString = function(jsonString, key){
			var json = JSON.parse(jsonString);
			var value = "";
			for(var i=0; i<json.length;i++){
				if(i>0){
					value += ",";
				}
				value += json[i][key]
			}
			return value;
		}
		
		//specificationItems:[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5寸"]}]
		//判断勾选的类别在集合中存不存在，存在,返回在该对象， 
	    $scope.searchObjectBykey=function(list,key,keyValue){
	    	for(var i=0;i<list.length;i++){
	    		if(list[i][key]==keyValue){
	    			return list[i];
	    		}
	    	}
	    	return null;
	    }
	});
	