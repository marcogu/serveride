/**
 * Created by marco on 2017/1/24.
 * test application
 */


var app = angular.module('CodeEditor',
    ['ngAnimate', 'ngSanitize', 'mgcrea.ngStrap']);

app.filter('to_trusted', ['$sce', function ($sce) {
    return function (text) {
        return $sce.trustAsHtml(text);
    };
}]);

'use strict';

app.controller('EditorController', function ($scope,$http) {
    $scope.popupScodeFiles = [];
    $scope.loadDefaultCodeFile = function(){
        console.log("init");
        $http.get("/scode/all").success(function(data,state,header){
            $scope.popupScodeFiles = Object.entries(data);             
        }).error(function(err){
            console.log(err);
        });
    };
});