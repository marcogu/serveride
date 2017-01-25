/**
 * Created by marco on 2017/1/24.
 * test application
 */


var app = angular.module('CodeEditor', ['ngRoute','ngAnimate', 'ngSanitize', 'mgcrea.ngStrap']);
app.filter('to_trusted', ['$sce', function ($sce) {
    return function (text) {
        return $sce.trustAsHtml(text);
    };
}]);

app.controller('EditorController', ['$scope','$http', function ($scope,$http) {

    $scope.loadDefaultCodeFile = function(){
        console.log("init");
    };
}]);