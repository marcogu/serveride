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

app.controller('EditorController', function ($scope,$http, $location) {
    $scope.popupScodeFiles = [];
    $scope.selectedScode = null;
    $scope.editingContent = "";

    $scope.cmtype = "text/x-scala";

    $scope.loadDefaultCodeFile = function(){
        console.log("init");
        $http.get("/scode/all").success(function(data,state,header){
            $scope.popupScodeFiles = Object.entries(data);             
        }).error(function(err){
            console.log(err);
        });

        $scope.codemirroApply();
    };
    //test action -----
    $scope.codemirroApply = function() {
        var txaEle = document.getElementById("txaEditor");
        console.log(txaEle.dataset.mpath);

        if(txaEle.dataset.mpath != null && txaEle.dataset.mpath.length > 0){
            var editor = CodeMirror.fromTextArea(txaEle, {
                lineNumbers: true,
                matchBrackets: true,
                mode: txaEle.dataset.cmtype
            });
        }
    };
    $scope.reload = function(){
        if($scope.selectedScode == null) return;
        var encodedPath = encodeURIComponent($scope.selectedScode[1]);
        //$location.path("/editor/"+encodedPath);
        window.open("/editor/"+encodedPath);
    };
});