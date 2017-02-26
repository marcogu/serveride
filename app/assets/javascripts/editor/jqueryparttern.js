/**
 * Created by marco on 2017/2/16.
 */

history.pushState(null, null, location.href);
window.onpopstate = function(event) {
    history.go(1);
};

$(function(){
    "use strict";

    var testSepcProjectName = $('#divFileStruct').attr("data-proj");
    var editingItem = {
        rpath:"",
        renderEle:null
    };

    var rightClickTreeItem = {
        rpath:"",
        ele:null
    };

    var contextMenuClickHandler = function(key, options){
        rightClickTreeItem.ele = options.$trigger;
        rightClickTreeItem.rpath = rightClickTreeItem.ele.attr("data-spath");
        switch(key){
            case "addFolder" :
                $("#addFileModal").modal();
                $("#btnAdd").on('click', addFolderHandler);
                break;
            case "addFile":
                $("#addFileModal").modal();
                $("#btnAdd").on('click', addFileHandler);
                break;
            case "del":
                deleteFileHandler();
                break;
        }   
    };

    function deleteFileHandler(){
        var pp = rightClickTreeItem.ele.attr("data-spath");
        var fn = rightClickTreeItem.ele.attr("data-fn");
        var delrequrl = "/proj/" + testSepcProjectName +"/del/" + pp;
        $.get(delrequrl, function(result){
            clearTreeNodeEvent(rightClickTreeItem.ele.parent("li"), true).remove();
        });
    }

    function clearTreeNodeEvent(node, isDeep){
        node.find('span').off('click', treeNodeClick);
        node.find('a > span').off('click', treeLeafClick);
        if (isDeep) {
            node.find('li.parent_li > span').off('click', treeNodeClick);     
            node.find('li.parent_li > a > span').off('click', treeLeafClick);
        }
        return node;
    }

    function addTreeNodeEvent(node, isDeep) {
        node.addClass('parent_li');
        node.find('span').on('click', treeNodeClick);
        node.find('a > span').on('click', treeLeafClick);
        if (isDeep) {
            node.find('li.parent_li > span').on('click', treeNodeClick);     
            node.find('li.parent_li > a > span').on('click', treeLeafClick);
        }
        return node;
    }

    var addFolderHandler = function(){
        $("#btnAdd").off('click', addFolderHandler);
        $("#addFileModal").modal('hide');

        var pp = rightClickTreeItem.ele.attr("data-spath");
        var fn = rightClickTreeItem.ele.attr("data-fn");
        var subContainer = rightClickTreeItem.ele.parent("li").find("."+fn);
        var addFileName = $('#recipient-fname').val();
        var addrequrl = "/proj/" + testSepcProjectName +"/add/folder/" + pp + "/" + addFileName;

        $.get(addrequrl, function(result){
            var c = subContainer.first();
            c.append(result);
            var t = c.children().last();
            addTreeNodeEvent(t);
        });
    };

    var addFileHandler = function(){
        $("#btnAdd").off('click', addFileHandler);
        $("#addFileModal").modal('hide');

        var pp = rightClickTreeItem.ele.attr("data-spath");
        var fn = rightClickTreeItem.ele.attr("data-fn");
        var subContainer = rightClickTreeItem.ele.parent("li").find("."+fn);
        var addFileName = $('#recipient-fname').val();
        var addrequrl = "/proj/" + testSepcProjectName +"/add/file/" + pp + "/" + addFileName;

        $.get(addrequrl, function(result){
            var c = subContainer.first();
            c.append(result);
            var t = c.children().last();
            addTreeNodeEvent(t);
        }); 
    };

    $.contextMenu({ // right click menu
        selector: '.context-menu-treeNode', 
        callback: contextMenuClickHandler,
        items: {
            "addFolder": {name: "添加文件夹"},
            "addFile": {name: "新建文件"},
            "del": {name: "删除"}
        }
    });

    $.contextMenu({
        selector: '.context-menu-treeLeaf', 
        callback: contextMenuClickHandler,
        items: {
            "del": {name: "删除文件"}
        }
    });

    var treeNodeClick = function(e){
        var children = $(this).parent('li.parent_li').find(' > ul > li');
        if (children.length > 0) { // it contain children, excute animation
            if (children.is(":visible")) {
                children.hide('fast');
            } else {
                children.show('fast');
            }            
        } else {
            var subContainer = $(this).parent("li").find("ul");
            var querySubUrl = "/proj/" + testSepcProjectName + "/scode/view/" + $(this).attr("data-spath");

            $.get(querySubUrl, function(result){
                subContainer.html(result);

                subContainer.find("li").addClass('parent_li').find(' > span').attr('title', 'Collapse this branch');
                subContainer.find('li.parent_li > span').on('click', treeNodeClick);
                subContainer.find('li.parent_li > a > span').on('click', treeLeafClick);
            })    
        }
        e.stopPropagation();
    };

    var treeLeafClick = function(e){
        var rpath = $(this).attr("data-spath");
        var requrl = "/proj/" + testSepcProjectName + "/src/" + rpath;

        if (editingItem.rpath === rpath ) {
            console.log("is already in edting....")
        } else {
            editingItem.rpath = rpath;
            if (editingItem.renderEle != null) {
                editingItem.renderEle.getWrapperElement().parentNode.removeChild(editingItem.renderEle.getWrapperElement());
                editingItem.renderEle = null;    
            }
            // editingItem.renderEle.
            $.get(requrl, function(response){
                var txaEle = document.getElementById("txaEditor");
                txaEle.value = response;
                // console.log(response);
                editingItem.renderEle = CodeMirror.fromTextArea(txaEle, {
                    lineNumbers: true,
                    matchBrackets: true,
                    theme: "vibrant-ink", //theme: "mdn-like",
                    mode:mirroModel(rpath)   // a test : "text/x-scala"
                }); 
            });
        }
    };

    var saveHandler = function(e){
        if (editingItem && editingItem.renderEle) {
            editingItem.renderEle.save();
            var txaEle = document.getElementById("txaEditor");
            var requestBody = txaEle.value;    

            // /proj/:proj/save/*f
            var requrl = "/proj/" + testSepcProjectName + "/save/" + editingItem.rpath;
            $.post(requrl, requestBody, function(result){
                console.log(result);
            });
        } else {
            console.log('nothing to save');
        }
    };

    $.get("/proj/" + testSepcProjectName + "/scode/view", function(result){
        $("#divFileStruct").html(result); 
        $('.tree li').addClass('parent_li').find(' > span').attr('title', 'Collapse this branch');
        $('.tree li.parent_li > span').on('click', treeNodeClick);
        $('.tree li.parent_li > a > span').on('click', treeLeafClick);
        $('#btnSave').on('click', saveHandler);
    });

    $('#btnOnNavbar').on('click', runApplication);

    function showTerminal(evt){
        var termHeight = $('#divTermPanel').css("height");
        // console.log("show---" + termHeight);
        if (termHeight == "20px") {
            $('#divEditorContainer').css("bottom", "200px");
            $('#divFileStruct').css("bottom", "200px");
            $('#divTermPanel').css("height", "200px");
            $('#divTerminal').css("height", "180px");
            $('#divTerminal').show("fast");
            createTerm();
        }
    };

    function hideTerminal(){
        var termHeight = $('#divTermPanel').css("height");
        if (termHeight != "0px") {
            $('#divTerminal').hide("fast");
            $('#divEditorContainer').css("bottom", "20px");
            $('#divFileStruct').css("bottom", "20px");
            $('#divTerminal').css("height", "0px");
            $('#divTermPanel').css("height", "20px");
        }
    };

    var terminalSocket = null;

    function runApplication(){
        showTerminal();
        var requestRun = "/proj/run/" + testSepcProjectName;
        $.get(requestRun, function(result){
            if (result.runing || result.sessionId) {
                connectToRunningTerminal();        
            }else {
                alert(JSON.stringify(result)); // error
            }
        });
    };

    function connectToRunningTerminal(){
        if (terminalSocket != null) return;
        var socketUrl = "ws://" + document.domain + ":9527/socket/console/" + testSepcProjectName;
        terminalSocket = new WebSocket(socketUrl);
        // terminalSocket.onopen = createTerm;
        terminalSocket.onclose = terminalSocketClose;
        terminalSocket.onmessage = terminalSocketMessage;
        terminalSocket.onerror = terminalSocketErr;
    };

    var term = null;
    function createTerm(evt){
        if (term == null) {
            var terminalContainer = $("#divTerminal")[0];
            term = new Terminal();
            term.open(terminalContainer);
            term._initialized = true;
            term.fit();    
        }
    };

    function terminalSocketClose(evt){ 
        terminalSocket = null;
        term.writeln("\x1b[41mTerminal Disconnected\x1b[0m");
        term.writeln("");
        term.writeln("");
    };
    function terminalSocketMessage(evt){ 
        term.writeln(evt.data);
    };
    function terminalSocketErr(evt){
        term.writeln("\x1b[41mTerminal socket error:");
        term.writeln(evt.data + "\x1b[0m");
    };

    $('#btnStop').on('click', function(evt){
        var apiurl = "/proj/stop/" + testSepcProjectName;
        $.get(apiurl, function(result){
            console.log(result);
        });
    });

    $('#spnShowConsole').on('click', function(){
        showTerminal();
        var requestRun = "/proj/info/" + testSepcProjectName;
        $.get(requestRun, function(result){
            if (result.runing) {
                connectToRunningTerminal();        
            }
        });
    });
    $('#spnHideConsole').on('click', hideTerminal);

    function extensionByPath(p) {
        return (/[.]/.exec(p)) ? /[^.]+$/.exec(p) : undefined;
    }

    // console.log(mirroModel("/Users/marco/Documents/workspace/jvm/scala/test.scala"));
    // console.log(mirroModel("test.sbt"));
    // console.log(mirroModel("test.js"));
    // console.log(mirroModel("test.xml"));
    // console.log(mirroModel("test.java"));
    // console.log(mirroModel("test.css"));
    // console.log(mirroModel("test.scala.html"));
    // console.log(mirroModel("test.less"));
    // console.log(mirroModel("/Users/marco/Documents/temp/autotoolt4/jzlog/166f-58abe688-2"));
    // console.log(mirroModel("test.other"));
    // console.log(mirroModel("test"));
    function mirroModel(path){
        const fextsion = extensionByPath(path);
        if ( path.lastIndexOf("jzlog/")>=0 ) return "text/x-textile";
        if (!fextsion) return "";
        switch(fextsion.toString().toLowerCase()){
            case "sbt" : ;
            case "scala" : ;
                return "text/x-scala";
                break;
            case "java" : ;
                return "text/x-java";
                break;                    
            case "html":;
            case "htm":;
                return "application/x-ejs";
                break;
            case "js":
            case "javascript":
                return "text/javascript";
                break;
            case "css":
                return "text/css";
                break;
            case "less":
                return "text/x-less";
                break;
            case "sass":
                return "text/x-sass";
                break;
        }
        return "";
    }
});