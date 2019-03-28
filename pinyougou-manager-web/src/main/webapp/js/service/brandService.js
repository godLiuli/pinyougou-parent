/* 品牌服务层 */
		app.service("brandService",function($http){
			//搜索
			this.search = function(page, rows, searchEntity){
				return $http.post("/brand/search.do?page="+page+"&rows="+rows, searchEntity);
			}
			//根据id查找
			this.findOne = function(id){
				return $http.get("/brand/findOne.do?id="+id);
			}
			//添加
			this.add = function(entity){
				return $http.post("/brand/add.do", entity);
			}
			//修改
			this.update = function(entity){
				return $http.post("/brand/update.do", entity);
			}
			//删除
			this.delete = function(ids){
				return $http.get("/brand/delete.do?ids="+ids);
			}
			//获取品牌列表(id和name)
			this.selectOptionList = function(){
				return $http.get("/brand/selectOptionList.do");
			}
		});
		