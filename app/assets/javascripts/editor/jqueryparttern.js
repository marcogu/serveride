/**
 * Created by marco on 2017/2/16.
 */
$(function(){

    var nodeclicktest = function(e){
        var rpath = $(this).attr("data-spath");
        var subContainer = $(this).parent("li").find("ul");
        var querySubUrl = "/proj/autotoolt6/scode/view/" + encodeURIComponent(rpath)

        $.get(querySubUrl, function(result){
            subContainer.html(result);

            subContainer.find("li").addClass('parent_li').find(' > span').attr('title', 'Collapse this branch');
            subContainer.find('li.parent_li > span').on('click', nodeclicktest);
        })
    };

    // var nodeclick = function(e){
    //     console.log($(this));
    //     var children = $(this).parent('li.parent_li').find(' > ul > li');
    //     if (children.is(":visible")) {
    //         children.hide('fast');
    //         console.log($(this).attr("mypath"));
    //         $(this).attr('title', 'Expand this branch').find(' > i').addClass('icon-plus-sign').removeClass('icon-minus-sign');
    //     } else {
    //         console.log($(this).attr("mypath"));
    //         children.show('fast');
    //         $(this).attr('title', 'Collapse this branch').find(' > i').addClass('icon-minus-sign').removeClass('icon-plus-sign');
    //     }
    //     e.stopPropagation();
    // };

    var modifyHandler = function(e){
        $('#divFileStruct').unbind('DOMSubtreeModified');
        $('.tree li').addClass('parent_li').find(' > span').attr('title', 'Collapse this branch');
        $('.tree li.parent_li > span').on('click', nodeclicktest);
    };

    $('#divFileStruct').bind('DOMSubtreeModified', modifyHandler);

    $.get("/proj/autotoolt6/scode/view", function(result){
        $("#divFileStruct").html(result);
    });
});