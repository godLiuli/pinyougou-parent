 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){	
	
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
		var id=$location.search()['id'];
		if(id==null){ //进入新建界面
			return;
		}
		goodsService.findOne(id).success(//进入修改界面
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){	
		var object;
		$scope.entity.goodsDesc.introduction=editor.html(); 
		if($scope.entity.goods.id != null){
			object=goodsService.update($scope.entity);
		}else{
			object=goodsService.add($scope.entity);
		}
		object.success(
			function(response){
				alert(response.message);
				if(response.success){
					$scope.entity={};
					editor.html('');//清空富文本编辑器 
					location.href="./goods.html";
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
	//文件上传
	$scope.uploadFile=function(){
		uploadService.uploadFile().success(
			function(response){
				if(response.success){	
					$scope.image_entity.url=response.message;
				}else{
					alert("上传失败！");
				}
			}	
		);
	} 
	
	//添加图片列表 
	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}}; //商品entity
    $scope.add_image_entity=function(){     
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity); 
    } 
    
    //从列表中移除图片
    $scope.remove_image_entity=function(index){     
        $scope.entity.goodsDesc.itemImages.splice(index,1); 
    } 
    
    //商品分类下拉框 category1_id 一级目录
    $scope.findCategory1=function(id){
    	itemCatService.findByParentId(id).success(
    		function(response){
    			$scope.itemCat1List=response;
    		}
    	);
    }
    //商品分类下拉框 category2_id 二级级目录  监听一级下拉框的变化
    $scope.$watch('entity.goods.category1Id',function(newValue,oldValue){
    	$scope.itemCat3List=null;
    	$scope.itemCatTypeId=null;
    	$scope.brandList=null;
    	//111$scope.entity.goodsDesc.customAttributeItems=null;
    	$scope.entity.goods.typeTemplateId=null;
    	itemCatService.findByParentId(newValue).success(
    		function(response){
    			$scope.itemCat2List=response;
    		
    		}
    	);
    });
    //商品分类下拉框 category3_id 三级级目录  监听二级下拉框的变化
    $scope.$watch('entity.goods.category2Id',function(newValue,oldValue){
    	$scope.entity.goods.typeTemplateId=null;
    	$scope.brandList=null;
    	//111$scope.entity.goodsDesc.customAttributeItems=null;
    	itemCatService.findByParentId(newValue).success(
    		function(response){
    			$scope.typeId=response
    			$scope.itemCat3List=response;
    		}
    	);
    });
    //获取模板ID
    $scope.$watch('entity.goods.category3Id',function(newValue,oldValue){
    	itemCatService.findOne(newValue).success(
    		function(response){
    			$scope.entity.goods.typeTemplateId=response.typeId;
    		}
    	);
    });
    
    //监听根据3级目录的typeId
    $scope.$watch('entity.goods.typeTemplateId',function(newValue,oldValue){
    	//获取品牌列表
    	typeTemplateService.findOne(newValue).success(
    		function(response){
    			$scope.brandList=JSON.parse(response.brandIds);
    			if($location.search()['id']==null){ 
    			     $scope.entity.goodsDesc.customAttributeItems = 
    			    	 JSON.parse(response.customAttributeItems);//扩展属性  
    			    }   
    		}
    	);
    	if($scope.entity.goods.typeTemplateId!=null){
    		//获取规格项  [{"id":27,"text":"网络","options":[ {},{}]},..]
        	typeTemplateService.findSpecList(newValue).success(
        		function(response){
        			$scope.specList=response;
        		}
        	);
    	}else{
    		$scope.specList=null;
    	}
    });
   
    //$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}}; //商品entity
    //添加、移除所选规格        
    $scope.addSpecificationItems=function(keyValue,optionName,$event){
    	var object=$scope.searchObjectBykey($scope.entity.goodsDesc.specificationItems,"attributeName",keyValue);
    	if(object!=null){
    		if($event.target.checked){
    			object.attributeValue.push(optionName);
    		}else{
    			var index=object.attributeValue.indexOf(optionName);
    			object.attributeValue.splice(index,1);
    			if(object.attributeValue.length<=0){
    				var index=$scope.entity.goodsDesc.specificationItems.indexOf(object);
    				$scope.entity.goodsDesc.specificationItems.splice(index,1);
    			}
    		}
    	}else{
    		var item={"attributeName":keyValue,"attributeValue":[optionName]}
    		$scope.entity.goodsDesc.specificationItems.push(item);
    	}
    	
    }
    
    
    //$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}}; //商品entity
   
    
  //创建SKU列表
	$scope.createItemList=function(){	
		$scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0'} ];//列表初始化
		var items= $scope.entity.goodsDesc.specificationItems;
		
		for(var i=0;i<items.length;i++){
			$scope.entity.itemList= addColumn( $scope.entity.itemList, items[i].attributeName,items[i].attributeValue );			
		}	
		
	}
	
	addColumn=function(list,columnName,columnValues){
		
		var newList=[];		
		for(var i=0;i< list.length;i++){
			var oldRow=  list[i];			
			for(var j=0;j<columnValues.length;j++){
				var newRow=  JSON.parse( JSON.stringify(oldRow)  );//深克隆
				newRow.spec[columnName]=columnValues[j];
				newList.push(newRow);
			}			
		}		
		return newList;
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
	
	//商品状态0-下架，1-上架
	$scope.marketble=['已下架','已上架'];
	
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
	
	$scope.checkAttributeValue=function(specName,optionName){ 
		 var items= $scope.entity.goodsDesc.specificationItems; 
		 var object=$scope.searchObjectBykey(items,'attributeName',specName); 
		 if(object==null){ 
			 return false; 
		 }else{ 
			 if(object.attributeValue.indexOf(optionName)>=0){ 
				 return true; 
			 }else{ 
				 return false; 
			 } 
		 }    
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
	//提交审核
	$scope.checkCommit=function(){
		if($scope.selectIds.length==0){
			alert("请选择！");
			return;
		}
		if(!confirm("确认提交审核吗？")){
			return;
		}
		goodsService.checkCommit($scope.selectIds).success(
			function(response){
				alert(response.message);
				if(response.success){
					$scope.reloadList();//重新加载
					$scope.selectIds=[];
				}
			}
		);
	}
	//上下架
	$scope.isMarketable=function(marketable){
		if($scope.selectIds.length==0){
			alert("请选择！");
			return;
		}
		if(!confirm("确认操作吗？")){
			return;
		}
		goodsService.marketable($scope.selectIds,marketable).success(
			function(response){
				alert(response.message);
				if(response.success){
					$scope.reloadList();//重新加载
					$scope.selectIds=[];
				}
			}
		);
	}
});	
