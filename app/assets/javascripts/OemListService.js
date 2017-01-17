/**
 * Created by marco on 16/3/22.
 */
app.factory('OemListService', ['$http', function($http){
    return {
        ctx:{pageNo:1, pageSize:10, query:{}, totalCount:0, totalPage:0, queryTxt:""}, //, count:20
        setTotalCount:function(count){
            if(count >= 0){
                this.ctx.totalCount = count;
                this.ctx.totalPage = parseInt(this.ctx.totalCount / this.ctx.pageSize + (this.ctx.totalCount % this.ctx.pageSize > 0 ? 1 : 0));
                console.log("total page count= " + this.ctx.totalPage );
            }
        },
        nextPage:function(){
            this.ctx.pageNo++;
            return this.postQuery(null);
        },
        previouPage:function(){
            this.ctx.pageNo--;
            if (this.ctx.pageNo < 1) {
                this.ctx.pageNo = 1;
            }
            return this.postQuery(null);
        },
        postQuery: function (requestBody) {
            if(requestBody){
                this.ctx.pageNo = 1;
                this.ctx.query = requestBody;
            }
            return $http.post('/oems/' + this.ctx.pageNo + '/' + this.ctx.pageSize + '/targetName/desc', this.ctx.query);
        },
        updateOem: function (oem, iconZipFile, resourcesZipFile, successHandler, errorHandler ) {
            var fd = new FormData();
            if(iconZipFile)
                fd.append('iconZip', iconZipFile);
            if (resourcesZipFile)
                fd.append('resourcesZip', resourcesZipFile);

            var jsonString = JSON.stringify(oem);
            fd.append('oem', jsonString);

            $http.post('/oem/update/mockId', fd, {headers: {'Content-Type': undefined}}).success(successHandler).error(errorHandler);
        },
        repoInfo:function (containerType,url){
            var reqUrl = '/repo/' + url +'/' + containerType; //git%40github.com%3aAFNetworking%2fAFNetworking.git
            return $http.get(reqUrl);
        },
        simpleHttpPostWithJson:function(url, data) { return $http.post(url, data); },
        updateOemIcon:function(oemObj) {
            var oemJson = JSON.stringify(oemObj);
            return $http.post('/oem/upicon/', oemJson);
        },
        importOemFromCfgPlist: function (oemPlist) {
            var fd = new FormData();
            fd.append('plist', oemPlist);
            return $http.post('/import', fd, {headers: {'Content-Type': undefined}});
        },
        schemaProjects:function() {
            return $http.get('/schema')
        }
    };
}]);
app.factory('PackMornitService', [function(){
    return {
        isConnected:false,
        websock:null,
        connect:function(callBack){
            if(this.isConnected == false){
                this.isConnected = true;
                this.websock = new WebSocket("ws://" + window.location.host + "/mornitscoket/");
                if(callBack != null){
                    this.websock.onmessage = callBack;
                }
                this.websock.onclose = function(event){
                    console.log("websocket did disconnect!");
                    console.log(event);
                    this.isConnected = false; // error
                }.bind(this)
            }
        },
        disconnect:function() {
            this.websock.close();
            this.isConnected = false;
        },
        sendMornitCmd:function(cmd) {
            this.websock.send(cmd);
        }
    };
}]);