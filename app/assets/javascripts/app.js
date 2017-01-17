/**
 * Created by marco on 16/2/15.
 */

var app = angular.module('OemTool', ['ngRoute','ngAnimate', 'ngSanitize', 'mgcrea.ngStrap']);
app.config(['$routeProvider', function($routeProvider){
	$routeProvider.when('/oems', {templateUrl : '/view/dashboard/oemlistview'});
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