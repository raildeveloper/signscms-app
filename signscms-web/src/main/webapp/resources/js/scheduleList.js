/**
 * Created by administrator on 24/5/17.
 */
var allSchedules;
var allDevices;
var allViews;
var allLinks;
var allSections;

function loadAllSchedules() {

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

        getAllSchedules();
        //window.setInterval(getAllSchedules, 30000);
    }
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
    console.log(JSON.stringify(data));
    $("#schTableBody").empty();
    allSchedules = data[0];
    allDevices = data[1];
    allViews = data[2];
    allLinks = data[3];
    allSections = data[4];

    jQuery.each(allSchedules, function (i, val) {
        jQuery('<tr/>', {
            id: 'tr_' + i
            //class: ''
        }).appendTo('#schTableBody');
        var deviceName = getDeviceName(val.cnfDevice, allDevices);
        jQuery('<td/>', {
            id: 'td_device' + i,
            //class: '',
            text: deviceName
        }).appendTo('#tr_' + i);
        var viewName = getModeNameFromView(val.scheduleCnfView, allViews);
        jQuery('<td/>', {
            id: 'td_view' + i,
            //class: '',
            text: viewName
        }).appendTo('#tr_' + i);
        var st = new Date(val.startTime);
        st = moment(st).format("HH:mm");
        jQuery('<td/>', {
            id: 'td_startTime' + i,
            //class: '',
            text: st
        }).appendTo('#tr_' + i);
        var et = new Date(val.endTime);
        et = moment(et).format("HH:mm");
        jQuery('<td/>', {
            id: 'td_endTime' + i,
            //class: '',
            text: et
        }).appendTo('#tr_' + i);


        var sta = val.status;
        jQuery('<td/>', {
            id: 'td_status_' + i
            //class: '',
        }).appendTo('#tr_' + i);

        jQuery('<label/>', {
            id: 'label_status_' + i,
            class: 'custom-control custom-checkbox'
        }).appendTo('#td_status_' + i);

        if (sta == "ACTIVE") {
            jQuery('<input/>', {
                id: 'input_status_' + i,
                class: 'custom-control-input',
                style: 'opacity:0;position:absolute;left: -9999px;',
                type: 'checkbox',
                checked: 'checked',
                onchange:'changeStatus("' + val.cnfScheduleId + '","input_status_' + i + '")'
            }).appendTo('#label_status_' + i);
        } else {
            jQuery('<input/>', {
                id: 'input_status_' + i,
                class: 'custom-control-input',
                style: 'opacity:0;position:absolute;left: -9999px;',
                onchange:'changeStatus("' + val.cnfScheduleId + '","input_status_' + i + '")',
                type: 'checkbox'
            }).appendTo('#label_status_' + i);
        }
        jQuery('<span/>', {
            id: 'span_status_' + i,
            class: 'custom-control-indicator'
        }).appendTo('#label_status_' + i);


        jQuery('<td/>', {
            id: 'td_action' + i,
            class: 'text-center'
            //text:val.status
        }).appendTo('#tr_' + i);

        jQuery('<a/>', {
            id: 'td_action_a' + i,
            class: 'btn btn-info btn-xs',
            //href: "#",
            onclick: 'editSchedule("' + val.cnfScheduleId  + '")',
            text: ' Edit '
        }).appendTo('#td_action' + i);

        jQuery(' <span/>', {
            id: 'td_action_a_span' + i,
            class: 'glyphicon glyphicon-edit'
            //text:val.status
        }).appendTo('#td_action_a' + i);

        jQuery(' <a/>', {
            id: 'td_action_a_del' + i,
            class: 'btn btn-danger btn-xs',
            //href: "#",
            onclick: 'deleteSchedule("' + val.cnfScheduleId  + '")',
            text: ' Del '
        }).appendTo('#td_action' + i);

        jQuery(' <span/>', {
            id: 'td_action_a_span' + i,
            class: 'glyphicon glyphicon-remove'
            //text:val.status
        }).appendTo('#td_action_a_del' + i);

        // <td class="text-center"><a class='btn btn-info btn-xs' href="#"><span class="glyphicon glyphicon-edit"></span> Edit</a> <a href="#" class="btn btn-danger btn-xs"><span class="glyphicon glyphicon-remove"></span> Del</a></td>


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
    setTimeout(function () {
        window.location.href = "/ChangeSign?action=view&deviceId=" + deviceId;
    }, 1000);
}

function addSchedule() {
    setTimeout(function () {
        window.location.href = "/addSchedule?action=add";
    }, 1000);
}

function editSchedule(scheduleId) {

    setTimeout(function () {
        window.location.href = "/addSchedule?action=edit&scheduleId=" + scheduleId;
    }, 1000);

}

function deleteSchedule(scheduleId) {

    var cnfm = confirm("Delete this schedule?");
    if(cnfm){
        // ajax call to delete this schedule.
        $.ajax({
            type: "POST",
            url: "/addSchedule?format=json&action=delete&scheduleId=" + scheduleId ,
            success: function (data) {
                //console.log("success update");
                window.location.href = "/Schedule";
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

function getDeviceName(deviceId, allDevices) {
    var deviceName = null;
    jQuery.each(allDevices, function (i, device) {
        if (device.deviceId == deviceId) {
            deviceName = device.deviceName;
        }
    });

    return deviceName;
}

function changeStatus(scheduleId, checkboxId){
    var message = null;
    var checked;
    if($('#'+checkboxId).is(':checked')){
        console.log("checkboxId - checked " + checkboxId);
        message = "Activate this schedule?";
        checked = true;
    } else {
        console.log("checkboxId - unchecked " + checkboxId);
        message = "De-activate this schedule?";
        checked = false;
    }


    var cnfm = confirm(message);
    if(cnfm){
        // Check conflicts of this schedule
        if(checked){
            if(checkConflict(scheduleId)){
                alert("This schedule conflicts with another schedule for the same Escalator.");
                $('#'+checkboxId).prop('checked', false);
            } else {
                changeSchedulesStatus(scheduleId,"ACTIVE");
            }
        } else {
            // This is for Deactivation - no check is required
            changeSchedulesStatus(scheduleId,"INACTIVE");
        }

    } else {
        if(checked){
            $('#'+checkboxId).prop('checked', false);
        } else {
            $('#'+checkboxId).prop('checked', true);
        }
        return false;
    }
}

function changeSchedulesStatus(scheduleId,status){
    $.ajax({
        type: "POST",
        url: "/addSchedule?format=json&action=status&scheduleId=" + scheduleId + "&status=" + status,
        success: function (data) {
            //console.log("success update");
            window.location.href = "/Schedule";
        },
        error: function (e) {
            console.log("ERROR: ", e);
        },
        done: function (e) {
            console.log("DONE");
        }
    });
}

function checkConflict(scheduleId){
    var conflict = false;
    var deviceId = getDeviceIdForSchedule(scheduleId, allSchedules);
    var startTime = getStartTimeForSchedule(scheduleId, allSchedules);
    var endTime = getEndTimeForSchedule(scheduleId, allSchedules);

    jQuery.each(allSchedules, function (i, schedule) {
        if(schedule.status == 'ACTIVE') {
            if (schedule.cnfScheduleId != scheduleId) {
                if (schedule.cnfDevice == deviceId) {
                    // Check if Start Time & End Time Conflict for the same device
                    var mStartTime = moment(schedule.startTime, "MMM DD, YYYY hh:mm:ss A");
                    var mEndTime = moment(schedule.endTime, "MMM DD, YYYY hh:mm:ss A");
                    var sStartTime = moment(startTime, "MMM DD, YYYY hh:mm:ss A");
                    var sEndTime = moment(endTime, "MMM DD, YYYY hh:mm:ss A");

                    if (moment(sStartTime).isBetween(mStartTime, mEndTime)) {
                        conflict = true;
                        return conflict;
                    }
                    if (moment(sEndTime).isBetween(mStartTime, mEndTime)) {
                        conflict = true;
                        return conflict;
                    }
                }
            }
        }

    });
    return conflict;
}

function getDeviceIdForSchedule(scheduleId, schedules){
    var device = null;
    jQuery.each(schedules, function (i, schedule) {
        if(schedule.cnfScheduleId == scheduleId) {
            device = schedule.cnfDevice;
        }
    });
    return device;
}

function getStartTimeForSchedule(scheduleId, schedules){
    var stTime = null;
    jQuery.each(schedules, function (i, schedule) {
        if(schedule.cnfScheduleId == scheduleId) {
            stTime =  schedule.startTime;
        }
    });
    return stTime;
}

function getEndTimeForSchedule(scheduleId, schedules){
    var eTime = null;
    jQuery.each(schedules, function (i, schedule) {
        if(schedule.cnfScheduleId == scheduleId) {
            eTime = schedule.endTime;
        }
    });
    return eTime;
}