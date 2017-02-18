/**
 * Created by marco on 2017/2/16.
 */
$(function(){
    "use strict";

    var nodeclick = function(e){
        console.log("-----")
        var children = $(this).parent('li.parent_li').find(' > ul > li');
        if (children.length > 0) { // it contain children, excute animation
            if (children.is(":visible")) {
                children.hide('fast');
            } else {
                children.show('fast');
            }            
        } else {
            var rpath = $(this).attr("data-spath");
            var subContainer = $(this).parent("li").find("ul");
            var querySubUrl = "/proj/autotoolt6/scode/view/" + encodeURIComponent(rpath)

            $.get(querySubUrl, function(result){
                subContainer.html(result);

                subContainer.find("li").addClass('parent_li').find(' > span').attr('title', 'Collapse this branch');
                subContainer.find('li.parent_li > span').on('click', nodeclick);
                subContainer.find('li.parent_li > a > span').on('click', leafclick);
            })    
        }
        e.stopPropagation();
    };

    var leafclick = function(e){
        var rpath = $(this).attr("data-spath");
        var testSepcProjectName = "autotoolt6"
        var requrl = "/proj/" + testSepcProjectName + "/src/" + rpath;
        $.get(requrl, function(response){
            console.log(response);    
        });
    };

    $.get("/proj/autotoolt6/scode/view", function(result){
        $("#divFileStruct").html(result); 
        $('.tree li').addClass('parent_li').find(' > span').attr('title', 'Collapse this branch');
        $('.tree li.parent_li > span').on('click', nodeclick);
        $('.tree li.parent_li > a > span').on('click', leafclick);
    });
});