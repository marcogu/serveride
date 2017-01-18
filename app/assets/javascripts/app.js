/**
 * Created by marco on 16/2/15.
 *
 */

var app = angular.module('OemTool', ['ngRoute','ngAnimate', 'ngSanitize', 'mgcrea.ngStrap']);
app.config(['$routeProvider', function($routeProvider){
	$routeProvider.when('/oems', {templateUrl : '/view/swipertool'});
	$routeProvider.when('/schema', {templateUrl : '/view/dashboard/taskschema'});
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
	}
}]);