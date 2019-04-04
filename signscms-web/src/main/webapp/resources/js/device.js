/**
 * Created by administrator on 24/5/17.
 */
var allDevices;
var allViews;
var allLinks;
var allSections;

function loadAllMachineViews() {

    var isAuthenticated = $('#hdnSession').val();
    //console.log("isAuthenticated" + isAuthenticated);
    //alert(isAuthenticated);
    if (isAuthenticated.trim() != 'true') {
        window.location.href = "/Controller";
    } else {
        $(document).ready(function () {
            $('a[href="#navbar-more-show"], .navbar-more-overlay').on('click', function (event) {
                event.preventDefault();
                $('body').toggleClass('navbar-more-show');
                if ($('body').hasClass('navbar-more-show')) {
                    $('a[href="#navbar-more-show"]').closest('li').addClass('active');
                } else {
                    $('a[href="#navbar-more-show"]').closest('li').removeClass('active');
                }
                return false;
            });
        });

        getAllMachineViews();
        window.setInterval(getAllMachineViews, 30000);
    }
}

function getAllMachineViews() {
    $.ajax({
        type: "POST",
        url: "/Devices?format=json&action=getAllData",
        data: $('#Login_Form').serialize(), // serializes the form's elements.
        success: function (data) {
            displayAllDevices(data);
        },
        error: function (e) {
            console.log("ERROR: ", e);
        },
        done: function (e) {
            console.log("DONE");
        }
    });
}


function displayAllDevices(data) {

    //console.log(JSON.stringify(data));
    allDevices = data[0];
    allViews = data[1];
    allLinks = data[2];
    allSections = data[3];
    $("#deviceRows").empty();
    jQuery.each(allDevices, function (i, val) {
        jQuery('<div/>', {
            id: 'div_row_col' + i,
            class: 'col-lg-3 col-md-3 col-sm-6 col-xs-12'
        }).appendTo('#deviceRows');

        jQuery('<div/>', {
            id: 'div_my_list' + i,
            class: 'my-list'
        }).appendTo('#div_row_col' + i);

        jQuery('<h3/>', {
            id: 'my_list_h3_' + i,
            text: "Sign : " + val.deviceName
        }).appendTo('#div_my_list' + i);

        var modeName = getModeForDevice(val.deviceId, allViews, allLinks);
        jQuery('<h3/>', {
            id: 'my_list_view_h3_' + i,
            text: "Mode : " + modeName
        }).appendTo('#div_my_list' + i);

        jQuery('<div/>', {
            id: 'div_img' + i,
            class: 'imgDivClass'
        }).appendTo('#div_my_list' + i);

        var imgSrc = getImageForDevice(val.deviceId, allViews, allLinks);
        jQuery('<img/>', {
            id: 'my_list_image_' + i,
            src: imgSrc
        }).appendTo('#div_img' + i);



        jQuery('<div/>', {
            id: 'my_list_description_' + i,
            text: val.description,
            class: 'offer'
        }).appendTo('#div_my_list' + i);

        jQuery('<a/>', {
            id: 'my_list_detail_a_href' + i,
            class: 'btn btn-info',
            text: 'Change Sign',
            //href: "#",
            onclick: 'changeSignForDevice("' + val.deviceId + '")'
        }).appendTo('#div_my_list' + i);


        /*
         jQuery('<span/>', {
         id: 'my_span_horizontal_' + i,
         text: "Width: " + val.pixelsHorizontal + "px"
         }).appendTo('#div_my_list' + i);
         jQuery('<span/>', {
         id: 'my_span_vertical_' + i,
         text: "Height: " + val.pixelsVertical + "px",
         class: 'pull-right'
         }).appendTo('#div_my_list' + i);

         jQuery('<div/>', {
         id: 'my_list_detail_' + i,
         class: 'detail'
         }).appendTo('#div_my_list' + i);

         jQuery('<p/>', {
         id: 'my_list_detail_p' + i,
         text: "Current Sign"
         }).appendTo('#my_list_detail_' + i);

         jQuery('<img/>', {
         id: 'my_list_detail_img' + i,
         src: imgSrc
         }).appendTo('#my_list_detail_' + i);

         jQuery('<span/>', {
         id: 'my_span_horizontal_' + i,
         text: "Width: " + val.pixelsHorizontal + "px"
         }).appendTo('#my_list_detail_' + i);
         jQuery('<span/>', {
         id: 'my_span_vertical_' + i,
         text: "Height: " + val.pixelsVertical + "px",
         class: 'pull-right'
         }).appendTo('#my_list_detail_' + i);


         jQuery('<div/>', {
         id: 'my_list_detail_img' + i,
         text: val.description,
         class: 'offer'
         }).appendTo('#my_list_detail_' + i);

         jQuery('<a/>', {
         id: 'my_list_detail_a_href' + i,
         class: 'btn btn-info',
         text: 'Change Sign',
         //href: "#",
         onclick: 'changeSignForDevice("' + val.deviceId + '")'
         }).appendTo('#my_list_detail_' + i);*/
    });

}

function getImageForDevice(deviceId, allViews, allLinks) {
    var imgSrc = null;
    jQuery.each(allLinks, function (i, link) {
        //console.log("link -->" + JSON.stringify(link));
        if (link.deviceId == deviceId) {
            imgSrc = getImageSrcFromView(link.viewId, allViews);
        }
    });
    return imgSrc;
}

function getModeForDevice(deviceId, allViews, allLinks) {
    var modeName = null;
    jQuery.each(allLinks, function (i, link) {
        //console.log("link -->" + JSON.stringify(link));
        if (link.deviceId == deviceId) {
            modeName = getModeNameFromView(link.viewId, allViews);
        }
    });
    return modeName;
}


function getModeNameFromView(viewId, allViews) {
    var mode = null;
    jQuery.each(allViews, function (i, view) {
        //console.log("view -->" + JSON.stringify(view));
        if (view.viewId == viewId) {
            mode = view.viewName;
        }
    });
    return mode;
}


function getImageSrcFromView(viewId, allViews) {
    var img = null;
    jQuery.each(allViews, function (i, view) {
        //console.log("view -->" + JSON.stringify(view));
        if (view.viewId == viewId) {
            img = view.images[0];
        }
    });
    return img;
}

function redirectToSigns() {
    setTimeout(function () {
        window.location.href = "/Devices";
    }, 1000);
}

function redirectToSchedule() {
    setTimeout(function () {
        window.location.href = "/Schedule";
    }, 1000);
}

function redirectToContactUs() {
    setTimeout(function () {
        window.location.href = "/Contact";
    }, 1000);
}

function redirectToAdmin() {
    setTimeout(function () {
        window.location.href = "/Admin";
    }, 1000);
}

function changeSignForDevice(deviceId) {
    //console.log("Chaneg Sign for Device " + deviceId);

    setTimeout(function () {
        window.location.href = "/ChangeSign?action=view&deviceId=" + deviceId;
    }, 1000);
}


