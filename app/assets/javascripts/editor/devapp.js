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
    $scope.selectedScode = {};
    $scope.editingContent = "";

    $scope.cmtype = "text/x-scala";

    $scope.loadDefaultCodeFile = function(){
        console.log("init");
        $http.get("/scode/all").success(function(data,state,header){
            $scope.popupScodeFiles = Object.entries(data);             
        }).error(function(err){
            console.log(err);
        });
    };
    //test action -----
    $scope.testAction = function() {
        var txaEle = document.getElementById("txaEditor");
        console.log(txaEle.dataset.cmtype);
    };
    // -----------------

    $scope.loadSouceCode = function(){
        var encodedPath = encodeURIComponent($scope.selectedScode[1]);
        var requrl = "/scode/load/" + encodedPath;
        $http.get(requrl).success(function(data, state, header){
            var ele = document.getElementById("txaEditor");
            ele.value = data
            var editor = CodeMirror.fromTextArea(ele, {
                lineNumbers: true,
                matchBrackets: true,
                mode: "text/x-scala"
            });
        });
    };
});