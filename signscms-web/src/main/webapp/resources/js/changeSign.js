/**
 * Created by administrator on 24/5/17.
 */

var deviceId;
var allViews;
var allLinks;

function loadAllSigns() {
    var isAuthenticated = $('#hdnSession').val();
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


        getAllViews();
        //window.setInterval(getAllViews, 30000);
    }
}

function getAllViews() {

    deviceId = getUrlParameter("deviceId");
    //console.log("device Id " + deviceId);
    $.ajax({
        type: "POST",
        url: "/ChangeSign?format=json&action=view&deviceId=" + deviceId,
        success: function (data) {
            displayAllViews(data);
        },
        error: function (e) {
            console.log("ERROR: ", e);
        },
        done: function (e) {
            console.log("DONE");
        }
    });
}
function displayAllViews(data) {
    // console.log(JSON.stringify(data));
    var allDevices = data[0];
    var allViews = data[1];
    var allLinks = data[2];
    var selectedDevice;
    //console.log("selected sign" + deviceId);
    jQuery.each(allDevices, function (i, device) {

        //console.log("Device Id " + device.deviceId);
        if(device.deviceId == deviceId){
            selectedDevice = device;
        }

    });
    document.getElementById("selectedSign").innerHTML = "Selected Sign: " + selectedDevice.deviceName;
    var associateView = getViewForDeviceId(deviceId, allLinks);
    //console.log("associated view " + associateView);
    jQuery.each(allViews, function (i, view) {
        if (associateView == view.viewId) {
            jQuery('<div/>', {
                id: 'div_view_row_s' + i,
                class: 'col-lg-3 col-md-3 col-sm-6 col-xs-12'
            }).appendTo('#views');
            jQuery('<div/>', {
                id: 'div_view_list_s' + i,
                class: 'my-list_selected'
            }).appendTo('#div_view_row_s' + i);
            jQuery('<h3/>', {
                id: 'my_view_list_h3_s_' + i,
                text: "Current Mode : " + view.viewName
            }).appendTo('#div_view_list_s' + i);
            jQuery('<img/>', {
                id: 'my_list_image_s_' + i,
                src: view.images[0]
            }).appendTo('#div_view_list_s' + i);
            jQuery('<div/>', {
                id: 'my_view_description_s_' + i,
                text: view.description,
                class: 'offer'
            }).appendTo('#div_view_list_s' + i);
            /*jQuery('<span/>', {
                id: 'my_view_span_horizontal_s_' + i,
                text: "Width: " + view.pixelsHorizontal + "px"
            }).appendTo('#div_view_list_s' + i);
            jQuery('<span/>', {
                id: 'my_view_span_vertical_s_' + i,
                text: "Height: " + view.pixelsVertical + "px",
                class: 'pull-right'
            }).appendTo('#div_view_list_s' + i);*/


        }
    });

    jQuery.each(allViews, function (i, view) {
        if(selectedDevice.deviceId == view.associated_Device) {
        //console.log("val " + JSON.stringify(val));
        if (associateView != view.viewId) {
            jQuery('<div/>', {
                id: 'div_view_row' + i,
                class: 'col-lg-3 col-md-3 col-sm-6 col-xs-12'
            }).appendTo('#views_select');

            jQuery('<div/>', {
                id: 'div_view_list' + i,
                class: 'my-list'
            }).appendTo('#div_view_row' + i);
            jQuery('<h3/>', {
                id: 'my_view_list_h3_' + i,
                text: "Mode : " + view.viewName
            }).appendTo('#div_view_list' + i);
            jQuery('<img/>', {
                id: 'my_list_image_' + i,
                src: view.images[0]
            }).appendTo('#div_view_list' + i);

            jQuery('<div/>', {
                id: 'my_view_description_' + i,
                text: view.description,
                class: 'offer'
            }).appendTo('#div_view_list' + i);

            /*jQuery('<span/>', {
                id: 'my_view_span_horizontal_' + i,
                text: "Width: " + view.pixelsHorizontal + "px"
            }).appendTo('#div_view_list' + i);
            jQuery('<span/>', {
                id: 'my_view_span_vertical_' + i,
                text: "Height: " + view.pixelsVertical + "px",
                class: 'pull-right'
            }).appendTo('#div_view_list' + i);*/

            jQuery('<a/>', {
                id: 'my_list_detail_a_href' + i,
                class: 'btn btn-info',
                text: 'Select',
                //href: "#",
                onclick: 'applySelectedSign("' + view.viewId + '")'
            }).appendTo('#div_view_list' + i);


        }}
    });
}

function applySelectedSign(viewId) {
    //console.log("View Id" + viewId);
    var cnfm = confirm("Apply Selected Mode to Sign");
    if (cnfm) {
        $.ajax({
            type: "POST",
            url: "/ChangeSign?format=json&action=change&deviceId=" + deviceId + "&viewId=" + viewId,
            success: function (data) {
                //console.log("success update");
                window.location.href = "/Devices";
            },
            error: function (e) {
                console.log("ERROR: ", e);
            },
            done: function (e) {
                console.log("DONE");
            }
        });
    } else {
        return false;
    }
}
function getViewForDeviceId(deviceId, allLinks) {
    var viewId = null;
    jQuery.each(allLinks, function (i, link) {
        //console.log("link -->" + JSON.stringify(link));
        if (link.deviceId == deviceId) {
            viewId = link.viewId;
        }
    });
    return viewId;
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
function getUrlParameter(sParam) {
    var sPageURL = decodeURIComponent(window.location.search.substring(1)),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : sParameterName[1];
        }
    }
}

