 //控制层 
app.controller('itemCatController' ,function($scope,$controller,itemCatService,typeTemplateService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){	
		$scope.entity.parentId=$scope.parentCat.id;
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			if(confirm("确认修改吗？")){
				serviceObject=itemCatService.update( $scope.entity ); //修改  
			}
			
		}else{
			serviceObject=itemCatService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					alert(response.message);
					//重新查询 
					$scope.findByParentId($scope.parentCat);//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){		
		if(!$scope.selectIds.length > 0) {
			alert("请选择记录！"); 
			return;
		}
		if(!confirm("确认删除所选记录吗？")){
			return;
		}
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.findByParentId($scope.parentCat);//重新加载
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	

	//商品分类
	$scope.findByParentId=function(entity){
		itemCatService.findByParentId(entity.id).success(
				function(response){
					$scope.list=response;
				}
		);
	}
	
	//导航条
	//level:1 顶级分类列表, 2  二级分类列表 ， 3  三级分类列表
	$scope.level=1;
	$scope.setLevel=function(level){  
		$scope.level=level;
	}
	//上级商品分类
	$scope.parentCat={id:0,name:''};
	
	//entity_1 表示  2级目录导航名; entity_2表示 3级目录导航名
	$scope.itemsCat=function(entity){
		$scope.parentCat.id=entity.id;
		if($scope.level==1){     //1级目录，没有父级目录
			$scope.entity_1=null;
			$scope.entity_2=null;
			$scope.parentCat.name='';
		}else if($scope.level==2){
			$scope.entity_1=entity; //点击了 顶级目录下的目录 -->到达2级目录
			$scope.entity_2=null;
			$scope.parentCat.name=entity.name;
		}else if($scope.level==3){
			$scope.entity_2=entity; //点击了 2级目录 -->到达3级目录
			$scope.parentCat.name +='>>'+entity.name;
		}
		$scope.findByParentId(entity);
	}
	
	//获取所有模板
	$scope.templateList={data:[]};
	$scope.findTypeTemplate=function(){
		typeTemplateService.selectOptionTemp().success(
			function(response){
				$scope.templateList={data:response};
			}
		);
	}
    
});	
