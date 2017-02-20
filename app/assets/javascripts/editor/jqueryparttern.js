/**
 * Created by marco on 2017/2/16.
 */

history.pushState(null, null, location.href);
window.onpopstate = function(event) {
    history.go(1);
};

$(function(){
    "use strict";

    var testSepcProjectName = "autotoolt6";
    var editingItem = {
        rpath:"",
        renderEle:null
    };

    var rightClickTreeItem = {
        rpath:"",
        ele:null
    }

    var contextMenuClickHandler = function(key, options){
        rightClickTreeItem.ele = options.$trigger;
        rightClickTreeItem.rpath = rightClickTreeItem.ele.attr("data-spath");
        $("#addFileModal").modal();
    };

    var addFileModalHideHandler = function(e){
        // taste print:
        // var i1 = rightClickTreeItem.ele.find("> span");
        // var i2 = rightClickTreeItem.ele.find("> a > span");
        // console.log(rightClickTreeItem.ele.attr("data-treenode"));
        // logic:
        var isNode = rightClickTreeItem.ele.attr("data-treenode") == "node";
        if (isNode) { // add sub item in right click node

        } else { // right click on file item and append tree item after current click item.

        }
        console.log(rightClickTreeItem.ele.parent());
    };
    $("#addFileModal").on('hide.bs.modal', addFileModalHideHandler);

    $.contextMenu({ // right click menu
        selector: '.context-menu-tree', 
        callback: contextMenuClickHandler,
        items: {
            "add": {name: "添加"},
            "del": {name: "删除"}
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
            var querySubUrl = "/proj/autotoolt6/scode/view/" + $(this).attr("data-spath");

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
                    theme: "ambiance",
                    mode: "text/x-scala"  // a test
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

    $.get("/proj/autotoolt6/scode/view", function(result){
        $("#divFileStruct").html(result); 
        $('.tree li').addClass('parent_li').find(' > span').attr('title', 'Collapse this branch');
        $('.tree li.parent_li > span').on('click', treeNodeClick);
        $('.tree li.parent_li > a > span').on('click', treeLeafClick);
        $('#btnSave').on('click', saveHandler);
    });

    $('#btnOnNavbar').on('click', function(){
        runApplication();
    });

    function showTerminal(evt){
        var termHeight = $('#divTermPanel').css("height");
        console.log("show---" + termHeight);
        if (termHeight == "20px") {
            $('#divEditorContainer').css("bottom", "200px");
            $('#divFileStruct').css("bottom", "200px");
            $('#divTermPanel').css("height", "200px");
            $('#divTerminal').css("height", "180px");
            $('#divTerminal').show("fast");
        }
    };

    function hideTerminal(){
        var termHeight = $('#divTermPanel').css("height");
        if (termHeight != "0px") {
            $('#divEditorContainer').css("bottom", "20px");
            $('#divFileStruct').css("bottom", "20px");
            $('#divTerminal').css("height", "0px");
            $('#divTermPanel').css("height", "20px");
            $('#divTerminal').hide("fast");
        }
    };

    var terminalSocket = null;

    function runApplication(){
        var requestRun = "/proj/run/" + testSepcProjectName;
        $.get(requestRun, function(result){
            if (result.runing || result.sessionId) {
                showTerminal();
                var socketUrl = "ws://localhost:9527/socket/console/" + testSepcProjectName;
                terminalSocket = new WebSocket(socketUrl);
                terminalSocket.onopen = createTerm;
        //         terminalSocket.onclose = terminalSocketClose;
                terminalSocket.onmessage = terminalSocketMessage;
        //         terminalSocket.onerror = terminalSocketErr;
            }
        });
    };

    var term = null;
    function createTerm(evt){
        var terminalContainer = $("#divTerminal")[0];
        term = new Terminal();
        term.open(terminalContainer);
        term._initialized = true;
        term.fit();
    };

    function terminalSocketClose(evt){ hideTerminal(); };
    function terminalSocketMessage(evt){ 
        term.write(evt.data);
    };
    function terminalSocketErr(evt){};

    $('#btnStop').on('click', function(evt){
        var apiurl = "/proj/stop/" + testSepcProjectName;
        $.get(apiurl, function(result){
            console.log(result);
        });
    });

    $('#spnShowConsole').on('click', showTerminal);
    $('#spnHideConsole').on('click', hideTerminal);


});