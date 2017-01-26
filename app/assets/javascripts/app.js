/**
 * Created by marco on 16/2/15.
 *
 */

var app = angular.module('OemTool', ['ngRoute','ngAnimate', 'ngSanitize', 'mgcrea.ngStrap']);
app.config(['$routeProvider', function($routeProvider){
	$routeProvider.when('/oems', {templateUrl : '/view/swipertool'});
	$routeProvider.when('/schema', {templateUrl : '/view/sqltool'});
	$routeProvider.when('/strapt', {templateUrl : '/view/dashboard/viewtest'});
	$routeProvider.when('/setting', {templateUrl : '/view/dashboard/setting'});
	$routeProvider.when('/history', {templateUrl : '/view/dashboard/packhistory'});
	$routeProvider.when('/swiper', {templateUrl : '/view/dashboard/swipertool' });

	$routeProvider.otherwise({redirectTo:'/oems'})
}]);
app.filter('to_trusted', ['$sce', function ($sce) {
	return function (text) {
		return $sce.trustAsHtml(text);
	};
}]);
app.directive('fileModel', ['$parse', function($parse){
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			var model = $parse(attrs.fileModel);
			var modelSetter = model.assign;
			element.bind('change', function(){
				scope.$apply(function(){
					modelSetter(scope, element[0].files[0]);
				});
			});
		}
	};
}]);

app.controller('AppController', ['$scope','$http', function($scope,$http){
}]);

app.controller('NavController', ['$scope', function ($scope) {
	$scope.selNavIdx = 1;

	$scope.showOems = function(){
		console.log("0-0-------");
		$scope.selNavIdx = 1;
		// test dispatch an event to other controller
		//$scope.$emit("eNvOemClick", "info-marco");
	};
}]);

app.controller('SwiperToolController', ['$scope', '$element', '$http', '$modal', function($scope, $element, $http, $modal){
	$scope.deal = {};

	$scope.showAlert = function(content){
		$modal({title: 'Title', content: content, placement:"center",
			backdrop:"static",
			show: true, html:true, element:angular.element([event.target.parentElement])});
	};

	$scope.alertWith = function(e, content) {
		$modal({title: 'Title', content: content, placement:"center",
			backdrop:"static",
			show: true, html:false, element:angular.element([e])});
		//element:angular.element([e])
	};

	//$scope.urlformat = function(action, data) {
	//	switch(action) {
	//		case "wk": return "/wkey/" + data[0] + "/" + data[1] + "/" + data[2] + "/" + data[3];
	//			break;
	//		case "mc": return "/mac/" + data[0] + "/" + data [1];
	//			break;
	//		case "3d":
	//			break;
	//		case "d3d":
	//			break;
	//		case "ci":
	//			break;
	//		case "tk":
	//			break;
	//	}
	//};

	$scope.commitWk = function(deal) {
		let url = `/wkey/${deal.random}/${deal.translog}/${deal.ksn}/${deal.mk}`;
		var alertTarget = event.target.parentElement;

		$http.get(url).success(function(data, state, header){
			$modal({title: 'Working Key Caculation result',
				content: `<html>${JSON.stringify(data, null, '\t')}</html>`, placement:"center",
				backdrop:"static",
				show: true, html:true, element:angular.element([alertTarget])});
		}).error(function(error, status, header){
			$modal({title: 'Title', content: error, placement:"center",
				backdrop:"static",
				show: true, html:true, element:angular.element([alertTarget])});
		})
	};

	$scope.commitMac = function(deal){
		let url = `/mac/${deal.wk}/${deal.data}`;
		var alertTarget = event.target.parentElement;
		$http.get(url).success(function(data, state, header){
			$modal({title: 'MAC Caculation result',
				content: `<html>${JSON.stringify(data, null, '\t')}</html>`, placement:"center",
					backdrop:"static",
					show: true, html:true, element:angular.element([alertTarget])});
		}).error(function(error, status, header){
			$modal({title: 'Title', content: error, placement:"center",
				backdrop:"static",
				show: true, html:true, element:angular.element([alertTarget])});
		})
	};

	$scope.commit3des = function(deal){
		let url = `/3des/${deal.key}/${deal.data}`;
		var alertTarget = event.target.parentElement;
		$http.get(url).success(function(data, state, header){
			$modal({title: '3DES Caculation result', content: `<html>${JSON.stringify(data, null, '\t')}</html>`, placement:"center",
					backdrop:"static",
					show: true, html:true, element:angular.element([alertTarget])});
		}).error(function(error, status, header){
			$modal({title: 'Title', content: error, placement:"center",
				backdrop:"static",
				show: true, html:true, element:angular.element([alertTarget])});
		})
	};

	$scope.commitdecrypt = function(deal){
		let url = `/derypt3des/${deal.key}/${deal.data}`;
		var alertTarget = event.target.parentElement;
		$http.get(url).success(function(data, state, header){
			$modal({title: '3DES decrypt result', content: `<html>${JSON.stringify(data, null, '\t')}</html>`, placement:"center",
					backdrop:"static",
					show: true, html:true, element:angular.element([alertTarget])});
		}).error(function(error, status, header){
			$modal({title: 'Title', content: error, placement:"center",
				backdrop:"static",
				show: true, html:true, element:angular.element([alertTarget])});
		})
	};

	$scope.cmtParserCinfo = function(info){
		let url = `/card/${info}`;
		let alertTarget = event.target.parentElement;
		$http.get(url).success(function(data, state, header){
			$modal({title: '3DES decrypt result',
				content: `<html>${JSON.stringify(data, null, '\t')}</html>`, placement:"center",
					backdrop:"static",
					show: true, html:true, element:angular.element([alertTarget])});
		}).error(function(error, status, header){
			$modal({title: 'Title', content: error, placement:"center",
				backdrop:"static",
				show: true, html:true, element:angular.element([alertTarget])});
		})
	};

	$scope.showTestCase = function(){
		let url = "/view/siwpertestcase";
		let alertTarget = event.target.parentElement;
		$http.get(url).success(function(data, state, header){
			$modal({title: '测试用例：',
				content:data, placement:"center",
					backdrop:"static",
					show: true, html:true, element:angular.element([alertTarget])});
		}).error(function(error, status, header){
			$modal({title: 'Title', content: error, placement:"center",
				backdrop:"static",
				show: true, html:true, element:angular.element([alertTarget])});
		})
	}
}]);

app.controller('SqltoolController', ['$scope', '$element', '$http', '$modal',
	function($scope, $element, $http, $modal) {

	$scope.sql = "SELECT A.AGTORG,A.TMERCID,A.TTERMID,D.NOD_ID,D.AGTORGCPUS,D.TPDU,D.TTMKKEY FROM STPNODMERKEYINF A" +
			" LEFT JOIN OMNG_NODPARA D ON TRIM(A.AGTORG)=TRIM(D.AGTORG) AND A.ORG_ID = D.NOD_ID WHERE ROWNUM < 2";

	$scope.divJsviewerQueryResultInit = function() {};

	$scope.submitSQL = function(asg) {
		var container = document.getElementById('divJsviewerQueryResult');
		while(container.hasChildNodes())
			container.removeChild(container.lastChild);

		$http.post("/sql", {sql:$scope.sql}).success(function(data,state, header){
			var ele = renderjson(data);
			container.appendChild(ele);
		})
	};

	$scope.querySpec = function(action, sql) {
		var postBody = {sql:sql};
		return $http.post(action, postBody).then(function(res){
			console.log(res.data);
			return res.data;
		});
	};

	$scope.testQueryOnOracle = function(sql){
		var secorcurl = encodeURIComponent("jdbc:oracle:thin:@172.16.16.13:1521:ORCL");
		var orc = "/sql/" + secorcurl + "/BPMPOS/nK17kLnd/oracle.jdbc.driver.OracleDriver";
		var result = $scope.querySpec(orc, sql);
	};

	$scope.testQueryOnPostgrate = function(sql){
		var secpgurl = encodeURIComponent("jdbc:postgresql://127.0.0.1:5432/mydb");
		var pg = "/sql/" + secpgurl + "/marco/emptypwd/org.postgresql.Driver";
		var result = $scope.querySpec(pg, sql);
	}
}]);