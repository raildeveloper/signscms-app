/**
 * Created by administrator on 24/5/17.
 */
var allSchedules;
var allDevices;
var allViews;
var allLinks;
var allSections;
var action;
var schedule;


function scheduleOperations() {

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
        action = getUrlParameter('action');
        if (action != null) {
            if (action == 'add') {
                getAllCnfData();
            }
            if (action == 'edit') {
                getEditData();
            }


        } else {
            console.log("NO ACTION FOUND IN URL");
        }

        //getAllSchedules();
        //window.setInterval(getAllSchedules, 30000);
    }
}

function getAllCnfData() {

    $("#sch_id").html("Add Schedule");
    $.ajax({
        type: "POST",
        url: "/addSchedule?format=json&action=add",
        success: function (data) {
            displayAddSchedule(data);
        },
        error: function (e) {
            console.log("ERROR: ", e);
        },
        done: function (e) {
            console.log("DONE");
        }
    });
}

function getEditData() {
    var scheduleId = getUrlParameter('scheduleId');
    $("#sch_id").html("Edit Schedule");
    $.ajax({
        type: "POST",
        url: "/addSchedule?format=json&action=edit&scheduleId=" + scheduleId,
        success: function (data) {
            displayEditSchedule(data);
        },
        error: function (e) {
            console.log("ERROR: ", e);
        },
        done: function (e) {
            console.log("DONE");
        }
    });

}


function displayEditSchedule(data) {

    console.log(JSON.stringify(data));
    allDevices = data[0];
    allViews = data[1];
    allLinks = data[2];
    allSections = data[3];
    schedule = data[4];

    jQuery.each(allDevices, function (i, val) {
        //console.log(i)
        if (val.deviceId == schedule.cnfDevice) {
            $('#device').append('<option value="' + val.deviceId + '"data-subtext="' + val.description + '" >' + val.deviceName + '</option>');
        } else {
            $('#device').append('<option disabled value="' + val.deviceId + '"data-subtext="' + val.description + '" >' + val.deviceName + '</option>');
        }

    });

    $('select[name=devicePicker]').val(schedule.cnfDevice);
    $("#device").selectpicker("refresh");

    jQuery.each(allViews, function (i, val) {
        if (schedule.cnfDevice == val.associated_Device) {
            //console.log(i)
            $('#view').append('<option value="' + val.viewId + '" data-content=\'<img src= "' + val.images[0] + '" > \'>' + val.viewName + '</option>');
            //$('#view').append('<option value="' + val.viewId + 'data-thumbnail="' + val.images[0] + '" >' + val.viewName + '</option>');
        }

    });
    $('select[name=viewPicker]').val(schedule.scheduleCnfView);
    $("#view").selectpicker("refresh");

    $('#datepicker').datetimepicker({
        format: 'LT'
    });

    //Jan 1, 1970 1:57:00 AM


    $('#datepicker_e').datetimepicker({
        format: 'LT'
    });


    var mStartTime = moment(schedule.startTime, "MMM DD, YYYY hh:mm:ss A");
    var mst = mStartTime.format("hh:mm:ss A");
    $('#datepicker').data("DateTimePicker").date(mst);

    var mEndTime = moment(schedule.endTime, "MMM DD, YYYY hh:mm:ss A");
    var met = mEndTime.format("hh:mm:ss A");
    $('#datepicker_e').data("DateTimePicker").date(met);
}

function displayAddSchedule(data) {

    //console.log(JSON.stringify(data));
    allDevices = data[0];
    allViews = data[1];
    allLinks = data[2];
    allSections = data[3];


    jQuery.each(allDevices, function (i, val) {
        $('#device').append('<option value="' + val.deviceId + '"data-subtext="' + val.description + '" >' + val.deviceName + '</option>');

    });
    $("#device").selectpicker("refresh");

    $('#datepicker').datetimepicker({
        format: 'LT'
    });

    $('#datepicker_e').datetimepicker({
        format: 'LT'
    });


}

function displayModesForSelectedDevice(deviceId) {

    $('#view').children('option:not(:first)').remove();

    jQuery.each(allViews, function (i, val) {
        if (deviceId == val.associated_Device) {
            $('#view').append('<option value="' + val.viewId + '" data-content=\'<img src= "' + val.images[0] + '" > \'>' + val.viewName + '</option>');
        }

    });
    $("#view").selectpicker("refresh");


}

function submitSchedule() {

    if (action == 'add') {
        return submitInsert();
    }
    if (action == 'edit') {
        return submitEdit();
    }

}


function submitEdit() {

    var scheduleId = schedule.cnfScheduleId;
    var selectedDeviceId = $('#device').val();
    var selectedViewId = $('#view').val();
    var startTime = $('#st_date').val();
    var endTime = $('#et_date').val();

    $.ajax({
        type: "POST",
        url: "/addSchedule?format=json&action=update",
        data: jQuery.param({
            scheduleId: scheduleId,
            device: selectedDeviceId,
            view: selectedViewId,
            starttime: startTime,
            endtime: endTime
        }),
        success: function (data) {
            forwardToSchedule(data);
        },
        error: function (e) {
            console.log("ERROR: ", e);
        },
        done: function (e) {
            console.log("DONE");
        }
    });
    return false;
}


function submitInsert() {

    var selectedDeviceId = $('#device').val();
    var selectedViewId = $('#view').val();
    var startTime = $('#st_date').val();
    var endTime = $('#et_date').val();

    $.ajax({
        type: "POST",
        url: "/addSchedule?format=json&action=insert",
        //data: $('#scheduleOperForm').serialize(), // serializes the form's elements.
        data: jQuery.param({device: selectedDeviceId, view: selectedViewId, starttime: startTime, endtime: endTime}),
        success: function (data) {
            forwardToSchedule(data);
        },
        error: function (e) {
            console.log("ERROR: ", e);
        },
        done: function (e) {
            console.log("DONE");
        }
    });
    return false;
}

function forwardToSchedule(data) {

    setTimeout(function () {
        window.location.href = "/Schedule";
    }, 1000);

}


function getAllSchedules() {
    $.ajax({
        type: "POST",
        url: "/Schedule?format=json&action=getAllData",
        //data: $('#Login_Form').serialize(), // serializes the form's elements.
        success: function (data) {
            displayAllSchedules(data);
        },
        error: function (e) {
            console.log("ERROR: ", e);
        },
        done: function (e) {
            console.log("DONE");
        }
    });
}

function displayAllSchedules(data) {
    $("#schTableBody").empty();
    allSchedules = data[0];
    allDevices = data[1];
    allViews = data[2];
    allLinks = data[3];
    allSections = data[4];

    jQuery.each(allSchedules, function (i, val) {
        jQuery('<tr/>', {
            id: 'tr_' + i
        }).appendTo('#schTableBody');

        jQuery('<td/>', {
            id: 'td_device' + i,
            text: val.cnfDevice
        }).appendTo('#tr_' + i);

        jQuery('<td/>', {
            id: 'td_view' + i,
            text: val.scheduleCnfView
        }).appendTo('#tr_' + i);

        jQuery('<td/>', {
            id: 'td_startTime' + i,
            text: val.startTime
        }).appendTo('#tr_' + i);

        jQuery('<td/>', {
            id: 'td_endTime' + i,
            text: val.endTime
        }).appendTo('#tr_' + i);

        jQuery('<td/>', {
            id: 'td_status' + i,
            text: val.status
        }).appendTo('#tr_' + i);

        jQuery('<td/>', {
            id: 'td_action' + i,
            class: 'text-center'
        }).appendTo('#tr_' + i);

        jQuery('<a/>', {
            id: 'td_action_a' + i,
            class: 'btn btn-info btn-xs',
            href: "#",
            text: ' Edit '
        }).appendTo('#td_action' + i);

        jQuery(' <span/>', {
            id: 'td_action_a_span' + i,
            class: 'glyphicon glyphicon-edit'
        }).appendTo('#td_action_a' + i);

        jQuery(' <a/>', {
            id: 'td_action_a_del' + i,
            class: 'btn btn-danger btn-xs',
            href: "#",
            text: ' Del '
        }).appendTo('#td_action' + i);

        jQuery(' <span/>', {
            id: 'td_action_a_span' + i,
            class: 'glyphicon glyphicon-remove'
        }).appendTo('#td_action_a_del' + i);

    });


}

function displayAllDevices(data) {

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
            onclick: 'changeSignForDevice("' + val.deviceId + '")'
        }).appendTo('#div_my_list' + i);


    });

}

function getImageForDevice(deviceId, allViews, allLinks) {
    var imgSrc = null;
    jQuery.each(allLinks, function (i, link) {
        if (link.deviceId == deviceId) {
            imgSrc = getImageSrcFromView(link.viewId, allViews);
        }
    });
    return imgSrc;
}

function getModeForDevice(deviceId, allViews, allLinks) {
    var modeName = null;
    jQuery.each(allLinks, function (i, link) {
        if (link.deviceId == deviceId) {
            modeName = getModeNameFromView(link.viewId, allViews);
        }
    });
    return modeName;
}


function getModeNameFromView(viewId, allViews) {
    var mode = null;
    jQuery.each(allViews, function (i, view) {
        if (view.viewId == viewId) {
            mode = view.viewName;
        }
    });
    return mode;
}


function getImageSrcFromView(viewId, allViews) {
    var img = null;
    jQuery.each(allViews, function (i, view) {
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
    setTimeout(function () {
        window.location.href = "/ChangeSign?action=view&deviceId=" + deviceId;
    }, 1000);
}

function addSchedule() {

    setTimeout(function () {
        window.location.href = "/addSchedule?action=add";
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

