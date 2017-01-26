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

app.controller('EditorController', function ($scope,$http, $dropdown) {

    $scope.dropdown = [];

    $scope.loadDefaultCodeFile = function(){
        console.log("init");
    };

    $scope.getallsoucecodefiles = function() {
        //console.log($scope.dropdown);
        var t = angular.element(document.getElementById("dpFiles"));
        //var t = document.getElementById("dpFiles");
        var dropd = $dropdown(t, {title:"hello", content:"aaaa"});

        //$http.get("/scode/all").success(function(data,state,header){
        //    var ary = Object.entries(data);
        //    var dropdowndata = [];
        //
        //    for(item in ary) {
        //        dropdowndata.push({text:item[0], ctxdata:item[1]});
        //    }
        //
        //
        //});
    };
    //console.log(document.getElementById("dpFiles"));
    $scope.getallsoucecodefiles();
});