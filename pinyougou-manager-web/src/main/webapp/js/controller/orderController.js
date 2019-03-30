/* 品牌控制 */
		app.controller("orderController",function($scope,$http) {


            $scope.getNums=function () {
                alert("heloo")
                $http.get('/order/findDataForManager.do').success(function (response) {
                    option.series[0]={
                        name:'访问来源',
                        type:'pie',
                        radius : '55%',
                        center: ['50%', '60%'],
                        data:[
                            {value:response.num1, name:'待付款订单'},
                            {value:response.num2, name:'待发货订单'},
                            {value:response.num3, name:'已发货订单'},
                            {value:response.num4, name:'已完成订单'},
                            {value:response.num5, name:'已关闭订单'}
                        ]
                    }

                    myChart.setOption(option);


                });
            }



            $scope.getTime=function () {
                $scope.startTime=$("#dateId1").val()
                $scope.endTime=$("#dateId2").val()
                if ($scope.startTime==null||$scope.startTime==''|| $scope.endTime==null|| $scope.endTime==''){
                    $scope.endTime=new Date().toLocaleDateString().replace("/","-").replace("/","-");
                    $scope.startTime=new Date(new Date().getTime()-7*24*3600*1000).toLocaleDateString().replace("/","-").replace("/","-");
                }

                $http.get('/order/packegDataByTime.do?startTime='+ $scope.startTime+'&endTime='+$scope.endTime).success(function (response) {
                    option2.series=response;
                    option2.xAxis[0]= {
                        type : 'category',
                        data: eachTime($scope.startTime,$scope.endTime)
                    }
                    myChart2 = echarts.init(document.getElementById('main2'));
                    myChart2.setOption(option2);

                    myChart.connect(myChart2);
                    myChart2.connect(myChart);

                    setTimeout(function (){
                        window.onresize = function () {
                            myChart.resize();
                            myChart2.resize();
                        }
                    },200);


                })
            }

        })
