 //控制层 
app.controller('goodsController' ,function($scope,$controller,goodsService,itemCatService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//商品状态0-未申请，1-申请中，2-审核通过，3-已驳回，4-关闭
	$scope.status=['未申请','申请中','审核通过','已驳回','关闭'];
	//商品分类 123级类别
	$scope.categorys=[];
	$scope.getCategorys=function(){
		itemCatService.findAll().success(
				function(response){
					$scope.itemCatList=response;
					for(var i=0;i<$scope.itemCatList.length;i++){
						var index=$scope.itemCatList[i].id;
						var value=$scope.itemCatList[i].name;
						$scope.categorys[index]=value;
					}
				}	
		);
	}
	
	//审核
	$scope.updateStatus=function(status){
		if($scope.selectIds.length==0){
			alert("请选择！");
			return;
		}
		if(!confirm("确认当前操作吗？")){
			return;
		}
		goodsService.audit($scope.selectIds,status).success(
			function(response){
				alert(response.message);
				if(response.success){
					$scope.reloadList();//重新加载
					$scope.selectIds=[];
				}
			}
		);
		
	}
	
	 //删除
	$scope.dele=function(){
		if($scope.selectIds.length==0){
			alert("请选择！");
			return;
		}
		if(!confirm("确认删除吗？")){
			return;
		}
		goodsService.dele($scope.selectIds).success(
			function(response){
				alert(response.message);
				if(response.success){
					$scope.reloadList();//重新加载
					$scope.selectIds=[];
				}
			}
		);
	}
	
	//从当前页面地址栏获取中的参数
	$scope.findOne=function(){
		var id=$location.search()['id'];
		goodsService.findOne(id).success(
			function(response){
				$scope.entity=response;
				editor.html(response.goodsDesc.introduction);//富文本编辑
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);//解析图片字符串为json
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse(response.goodsDesc.customAttributeItems);//扩展属性
				$scope.entity.goodsDesc.specificationItems=JSON.parse(response.goodsDesc.specificationItems);//规格列表
				//SKU
				for(var i=0;i<$scope.entity.itemList.length;i++){
					$scope.entity.itemList[i].spec =  
						JSON.parse( $scope.entity.itemList[i].spec); 
				}
			}
		);
	}
   
});	
