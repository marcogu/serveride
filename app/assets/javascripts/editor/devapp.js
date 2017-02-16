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

app.controller('EditorController', function ($scope,$http, $location, $modal) {
    $scope.popupScodeFiles = [];
    $scope.selectedScode = null;

    $scope.currentEditor = null;
    $scope.cmtype = "text/x-scala";

    $scope.treeContent = "";

    $scope.loadDefaultCodeFile = function(){
        console.log("init");
        $http.get("/scode/all").success(function(data,state,header){
            $scope.popupScodeFiles = Object.entries(data);             
        }).error(function(err){
            console.log(err);
        });

        $scope.codemirroApply();
    };
    
    $scope.codemirroApply = function() {
        var txaEle = document.getElementById("txaEditor");
        if(txaEle.dataset.mpath != null && txaEle.dataset.mpath.length > 0){
            var editor = CodeMirror.fromTextArea(txaEle, {
                lineNumbers: true,
                matchBrackets: true,
                theme: "ambiance",
                mode: txaEle.dataset.cmtype
            });
            $scope.currentEditor = editor;
        }
    };

    $scope.reload = function(){
        if($scope.selectedScode == null) return;
        console.log($scope.selectedScode);
        // var encodedPath = encodeURIComponent($scope.selectedScode[1]);
        //window.open("/editor/"+encodedPath);
        // window.location.href = "/editor/" + encodedPath;
    };

    $scope.save = function(){
        var txaEle = document.getElementById("txaEditor");
        var content = $scope.currentEditor.getValue();
        var postUrl = "/editor/save/" + encodeURIComponent(txaEle.dataset.mpath);
        $http.post(postUrl, content).success(function(data, state, header){
            //console.log("save got response:" + data);
        });
    };

    $scope.initProjStruct = function(){
        // "http://localhost:9527/proj/autotoolt6/scode/list"
        //$http.get("/tv").success(function(data){
            //$scope.treeContent = data;
            //console.log(data);
        //})
    };

    $scope.treeAdded = function(){
        console.log("tree added");
    };

    // var myOtherModal = $modal({scope: $scope, template: "view/siwpertestcase", show: false});
    $scope.showModel = function(){ //"/view/siwpertestcase"
        // myOtherModal.$promise.then(myOtherModal.show);
    };

    $scope.initProjStruct();
});