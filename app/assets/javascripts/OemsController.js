/**
 * Created by marco on 16/3/22.
 */
app.controller('OemsController', ['$scope','$alert', 'OemListService', function($scope, $alert, OemListService){
    $scope.editType = "编辑";
    $scope.queryCtx = OemListService.ctx;
    $scope.selectPageNo = null;
    $scope.addingOem = null;

    $scope.pageRangeModel = {
        pageRange:[],
        set:function(){
            var size = 6;
            var s = $scope.queryCtx.pageNo - size / 2;
            var e = $scope.queryCtx.pageNo + size / 2;
            s = s > 0 ? s : 1;
            e = e <= $scope.queryCtx.totalPage ? e : $scope.queryCtx.totalPage;

            var tooLess = ((e - s) < size) && $scope.queryCtx.totalPage >= size;
            if (tooLess) {
                if( s < size / 2 ) {
                    var extende = e + size - (e - s);
                    e = extende > $scope.queryCtx.totalPage ? $scope.queryCtx.totalPage : extende;
                } else if( $scope.queryCtx.totalPage - e < size / 2 ){
                    var extendes = s - (size - (e - s));
                    s = extendes > 0 ? extendes : 1;
                }
            }

            var tempRange = [];
            for(var i=s; i<=e; i++){
                tempRange.push(i);
            }
            $scope.pageRangeModel.pageRange = tempRange;
        }
    };

    $scope.searchClick = function(){
        OemListService.postQuery().success($scope.queryResponseHandler)
    };

    $scope.previouRange = function() { // TODO: change logic previou to previou page range
        OemListService.previouPage().success($scope.queryResponseHandler);
    };

    $scope.nextRange = function() { // TODO: change logic next to next page range
        OemListService.nextPage().success($scope.queryResponseHandler);
    };
    
    $scope.oemItemClick = function(oem){
        $scope.editType = "编辑";
        $scope.selectedOem = oem;
        $scope.isEditPanelIsVisable = true;
    };
    $scope.onPageNo = function(pageNo, event){
        if($scope.selectPageNo){
            $scope.selectPageNo.removeClass("active");
        }
        $scope.selectPageNo = angular.element(event.target.parentElement);
        $scope.selectPageNo.addClass("active");
        $scope.queryCtx.pageNo = pageNo;
        OemListService.postQuery().success($scope.queryResponseHandler)
    };
    $scope.save = function(selectedOem){
        /** augular will invoke oemEditFormSubmit later, do not request server here.*/
        console.log(selectedOem.targetName);
    };
    $scope.cancelEdit = function(selectedOem, event){
        $scope.addingOem = null;
        $scope.isEditPanelIsVisable = false;
    };
    $scope.oemEditFormSubmit = function() {
        var iconsZip = $scope.oemIcons;
        var resourcesZip = $scope.oemResources;
        // validate 
        var isNewOem = !$scope.selectedOem._id;
        if (isNewOem) { // create new record
            $scope.selectedOem.iconImageAssetsPath = ""; // for test
            $scope.selectedOem.oemPath = "";
            $scope.selectedOem.resourcePath = "";
            console.log($scope.selectedOem);    
            // TODO: request add new oem 
            // after respoonse success:
            $scope.addingOem = null;
        } else {
            OemListService.updateOem($scope.selectedOem, iconsZip, resourcesZip, function(data, status, headers){
                var alertInfo = "oem:" + $scope.selectedOem.bundleDisplayName + ", id:" + data.$oid + " 已经保存.";
                $alert({title: '操作:', content: alertInfo,
                    placement: 'top', type: 'info', duration:"3", show: true,
                    element:angular.element(document.querySelector('#oemSave'))});
            }, function(err){});    
        }
    };
    $scope.add = function(newOem) {
        console.log(newOem);
    };
    $scope.searchInputChange = function(){
        var rgx = {$regex:".*?"+ $scope.queryCtx.queryTxt +".*", $options: "i"};
        var obj = {targetName:rgx};
        OemListService.postQuery(obj).success($scope.queryResponseHandler);
    };

    $scope.queryResponseHandler = function(data, status, headers) {
        OemListService.setTotalCount(data.count);
        $scope.pageRangeModel.set();
        $scope.dataset = data.data;
    };

    $scope.$on('eNvOemClickBroadcast', function(e, data){
        console.log("get broad cast event:" + data);
    });
    $scope.newOemClick = function(){
        console.log($scope.selectedOem);
        $scope.editType = "添加";
        $scope.isEditPanelIsVisable = true;
        if ($scope.addingOem){
            $scope.selectedOem = $scope.addingOem;  
        } else {
            $scope.selectedOem = {};
            $scope.addingOem = $scope.selectedOem;
        }
    };
    $scope.updateIconTouch = function (oem) {
        if(!oem || !oem._id) {
            console.log("can not get oem by arg");
            return;
        }
        OemListService.updateOemIcon(oem).success(function(data, status, headers){
            if ($scope.selectedOem.bundleId == data.bundleId) {
                $scope.selectedOem.displayIcon = data.displayIcon;
            }
            console.log(data);
        }).error(function(err){
            console.log(err);
        })
    };
    $scope.searchClick();
}]);
app.controller('AppController', ['$scope','$http','PackMornitService', function($scope,$http,PackMornitService){
    String.prototype.replaceAll = function(reallyDo, replaceWith, ignoreCase) {
        if (!RegExp.prototype.isPrototypeOf(reallyDo)) {
            return this.replace(new RegExp(reallyDo, (ignoreCase ? "gi": "g")), replaceWith);
        } else {
            return this.replace(reallyDo, replaceWith);
        }
    };
    $scope.$on("eNvOemClick", function (event, data) {
        $scope.$broadcast("eNvOemClickBroadcast", data);
    });
    $scope.consoleCloseBtnClick = function(){
        $scope.consoleIsOpen = false;
    };
    $scope.showConsoleClick = function() {
        $scope.consoleIsOpen = true;
        PackMornitService.connect($scope.consoleListener);
    };
    $scope.consoleListener = function(msg) {
        var obj = JSON.parse(msg.data);
        console.log(obj);
        if(obj.body) $scope.wsBuildMsgHandler(obj);
        else if(obj.cmd) {
            switch (obj.cmd.cmd) {
                case 'Checkout':
                    console.log("get checkout event!");
                    $scope.$broadcast('Checkout', obj);
                    break;
            }
        }
    };
    $scope.wsBuildMsgHandler = function(msgObj){
        var current = "not console result event";
        if(msgObj.body.stype == "Console")
            current = msgObj.body.out.replaceAll("\n","<br/>") + "<br/>";// msg.data;
        else if(msgObj.body.stype == "Finish")
            current = "<br/>oem:" + msgObj.body.sid + " is finish";

        if (($scope.consoleContent.length + current.length) < 33535)
            $scope.consoleContent += current;
        else
            $scope.consoleContent = current;
        $scope.$apply();
    };

    $scope.sendTestAction = function() {
        $http.get('/start/0001');
    };
    $scope.sendStopAction = function() {
        console.log("stop did click");
        $http.get('/stop/0001');
    };
    $scope.consoleClearBtnClick = function() {
        console.log("--------");
        $scope.consoleContent = "";
    };

    PackMornitService.connect($scope.consoleListener);
}]);
app.controller('NavController', ['$scope', function ($scope) {
    $scope.selNavIdx = 1;
    $scope.showOems = function(){
        $scope.selNavIdx = 1;
        // test dispatch an event to other controller
        $scope.$emit("eNvOemClick", "info-marco");
    };
}]);
app.controller('SchemaController', ['$scope', '$modal','$alert', 'OemListService', function($scope, $modal, $alert, service){
    $scope.addProjState = "dfProjRepo";
    $scope.schemaState = "listSchema";
    $scope.stepLabel = "提交仓库信息";
    $scope.repoContainer = "tags";
    $scope.taskTargets = [];

    $scope.selectedProj = {
        vcontainer:[],
        selectedTags:"",
        version:"",
        target:"",
        certName:"",
        repo:{
            type:"git",
            url:"",
            tags:[],
            heads:[]
        },
        projEntity:function(selectItemModel, isTag){ /* isTag = $scope.repoContainer == 'tags' */
            var ver = isTag ? selectItemModel.selectedTags : selectItemModel.version;
            return {
                compileTarget:selectItemModel.target,
                certName:selectItemModel.certName,
                repoDef:{
                    url:selectItemModel.repo.url
                },
                specVer:{
                    version: ver,
                    verCtxName: selectItemModel.selectedTags,
                    verDesc:"",
                    isTag:isTag
                }
            };
        },
        toSchemaEntity:function(proj, configInfo) {
            var sc = { project:proj };
            if (configInfo) sc.config = { output:"allOem" };
            return sc;
        }
    };
    $scope.stateStepCommit = function(){ //changeSchemaState value is in enum ['dfProjRepo', 'checkRepo']
        switch ($scope.addProjState){
            case "dfProjRepo":
                $scope.commitHandlerForDefineRepoState(function(){$scope.stepLabel = "提交版本信息";});
                break;
            case "repoDetail":$scope.commitHandlerForCheckoutRepo(function(){$scope.stepLabel = "提交工程配置";});
                break;
            case "projDetail":$scope.commitSchema();
            default:;
        }
    };
    $scope.toListStateTouchHandler = function() {
        $scope.schemaState = "listSchema";
    };
    $scope.commitHandlerForDefineRepoState = function(afterHandler){
        if(!$scope.selectedProj.repo.tags.length){ // local memory is empty, send for request server
            $scope.selectedProj.vcontainer = [];
            $scope.stepCmtBtnDisable = true;

            var httpget = service.repoInfo($scope.repoContainer, encodeURIComponent($scope.selectedProj.repo.url));
            httpget.success(function(data, status, header){
                $scope.selectedProj.repo.tags = data; // bing data
                $scope.selectedProj.vcontainer = data;
                $scope.addProjState = "repoDetail"; // change ng-switch state
                if(afterHandler)
                    afterHandler();
                $scope.stepCmtBtnDisable = false;
                console.log(data);
            });
            httpget.error(function(error){
                console.log(error);
                if(afterHandler)
                    afterHandler();
                $scope.stepCmtBtnDisable = false;
            });
        } else {
            $scope.selectedProj.vcontainer = $scope.selectedProj.repo.tags;
            if(afterHandler)
                afterHandler();
        }
    };

    $scope.repoContainerSelectChange = function(selContainer){
        if(selContainer == 'tags'){
            $scope.commitHandlerForDefineRepoState();
        } else {
            if(!$scope.selectedProj.repo.heads.length){
                $scope.selectedProj.vcontainer = [];
                $scope.stepCmtBtnDisable = true;
                var httpget = service.repoInfo(selContainer, encodeURIComponent($scope.selectedProj.repo.url));
                httpget.success(function(data, status, header){
                    $scope.selectedProj.repo.heads = data; // bing data
                    $scope.selectedProj.vcontainer = data;
                    $scope.stepCmtBtnDisable = false;
                    console.log(data);
                });
                httpget.error(function(error){
                    $scope.stepCmtBtnDisable = false;
                    console.log(error);
                });
            } else {
                $scope.selectedProj.vcontainer = $scope.selectedProj.repo.heads;
            }
        }
    };

    $scope.showEventHandler = function(){
        console.log("---------");
    };

    $scope.commitHandlerForCheckoutRepo = function(){
        var theProj = $scope.selectedProj.projEntity($scope.selectedProj, $scope.repoContainer == 'tags');
        $scope.stepCmtBtnDisable = true;
        var encodeRepoUrl = encodeURIComponent($scope.selectedProj.repo.url);
        service.simpleHttpPostWithJson('/repo/' + encodeRepoUrl + '/checkout', theProj).success(function(data, status, header){
            if(data && data.msg == "loading" && data.succ == true){ // server response success
                if(data.finish == true){
                    $scope.stepCmtBtnDisable = false;
                } else {
                    var agAltele = angular.element(document.querySelector('#divSchemaMain')) ;
                    $alert({title: '操作',
                        content: '正在检出源代码...', placement: 'top', type: 'info', duration:"3",
                        show: true, element:agAltele});
                }
            } else { // reponse reponse error

            }
        }).error(function(errInfo){
            //$scope.stepCmtBtnDisable = false;
            console.log(errInfo);
        });

    };

    $scope.commitSchema = function() {
        $scope.stepCmtBtnDisable = false;
        var theProj = $scope.selectedProj.projEntity($scope.selectedProj, $scope.repoContainer == 'tags');
        var schema = $scope.selectedProj.toSchemaEntity(theProj, "allOem");
        service.simpleHttpPostWithJson('/schema/update', schema).success(function(data, status, header){
            $scope.stepCmtBtnDisable = true;
            $scope.schemaState = 'listSchema';
        }).error(function(errInfo){
            $scope.stepCmtBtnDisable = true;
            alert(errInfo);
        });    
    };

    $scope.$on('Checkout',  function (event, data) {
        $scope.stepLabel = '新建项目';
        $scope.stepCmtBtnDisable = false;
        $scope.addProjState = "projDetail";
        //var agAltele = angular.element(document.querySelector('#divSchemaMain')) ;
        //$alert({title: '提示',
        //    content: '源码检出完成,已经切换到版本:' + data.cmd.args[1], placement: 'top', type: 'info', duration:"3",
        //    show: true, element:agAltele});
    });

    $scope.requestTargets = function() {
        service.schemaProjects().success(function(data, status, headers){
            for(var i=0; i < data.length; i++) {
                var result = data[i].project.repoDef.url.split("/");
                data[i].sname = result.length > 0 ? result[result.length-1] : data[i].project.repoDef.url;
            }
            $scope.taskTargets = data;
        }).error(function(info){
            console.log(info);
        });
    };

    $scope.excuteSchema = function(selectedItem) {
        alert("将要执行" + selectedItem.sname + "的打包工作，oem数量：" + selectedItem.omes.length);
    };

    $scope.requestTargets();
}]);
app.controller('SettingController',['$scope', 'OemListService', function($scope, OemListService){ //'$http'
    $scope.importDesc = "选择文件";
    $scope.importUploadClick = function(){
        document.querySelector("#importUploadInput").disabled = false;
        document.querySelector("#importUploadInput").click();
    };
    $scope.importUploadChange = function(event) {
        $scope.theFile = event.value;
        if(event.value){
            $scope.importDesc = "上传并导入";
            var uploaderEle = document.querySelector("#importUploadInput");
            if(uploaderEle.disabled == false){
                $scope.$apply();
                uploaderEle.disabled = true;
            }
        }
    };
    $scope.uploadBtnClick = function(event, target){
        if(event.target != target) return;

        if($scope.theFile){
            var uploaderEle = document.querySelector("#importUploadInput");
            //alert("执行上传:" + $scope.theFile);
            OemListService.importOemFromCfgPlist(uploaderEle.files[0]).success(function(data, status, headers){
                console.log("import response is" + data);
            }).error(function(err){
                console.log("import error:" + err);
            });
        } else {
            document.querySelector("#importUploadInput").disabled = false;
            document.querySelector("#importUploadInput").click();
        }
    };
}]);
app.controller('HistoryController', ['$scope', function($scope){

}]);
app.controller('ViewCmpTestCtrl', ['$scope', '$modal','$alert', function($scope, $modal, $alert){
    $scope.selectedIcon = "";
    $scope.selectedIcons = ["Globe","Heart"];
    $scope.icons = [{"value":"Gear","label":"<i class=\"fa fa-gear\"></i> Gear"},{"value":"Globe","label":"<i class=\"fa fa-globe\"></i> Globe"},{"value":"Heart","label":"<i class=\"fa fa-heart\"></i> Heart"},{"value":"Camera","label":"<i class=\"fa fa-camera\"></i> Camera"}];
    $scope.alert = {title: 'Holy guacamole!', content: 'Best check yo self, you\'re not looking too good.', type: 'info'};

    $scope.showModalTest = function(event) {
        var myModal = $modal({title: 'Title', content: 'Hello Modal<br />This is a multiline message!',placement:"center",
            backdrop:"static",
            show: true, html:true, element:angular.element([event.target.parentElement])}); //
    };

    $scope.showAlert = function(event){
        //$alert({title: 'Holy guacamole!',
        //    content: 'Best check yo self, you\'re not looking too good.', placement: 'top', type: 'info', duration:"3",
        //    show: true, element:angular.element([event.target.parentElement.parentElement.parentElement])});
        $alert({title: 'Holy guacamole!',
            content: 'Best check yo self, you\'re not looking too good.', placement: 'top', type: 'info', duration:"3",
            show: true, element:angular.element([event.target.parentElement])});
    }
} ]);