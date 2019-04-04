/**
 * Created by administrator on 24/5/17.
 */

var deviceId;
var allViews;
var allLinks;

function loadContactUsForm() {
    var isAuthenticated = $('#hdnSession').val();
    if (isAuthenticated.trim() != 'true') {
        window.location.href = "/Controller";
    } else {

        $(document).ready(function () {

            $('#characterLeft').text('240 characters left');
            $('#message').keydown(function () {
                var max = 240;
                var len = $(this).val().length;
                if (len >= max) {
                    $('#characterLeft').text('You have reached the limit');
                    $('#characterLeft').addClass('red');
                    $('#btnSubmit').addClass('disabled');
                }
                else {
                    var ch = max - len;
                    $('#characterLeft').text(ch + ' characters left');
                    $('#btnSubmit').removeClass('disabled');
                    $('#characterLeft').removeClass('red');
                }
            });


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


        //getAllViews();
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

function saveContactUsForm() {

    var name = $("#name").val();
    var email = $("#email").val();
    var mobile = $("#mobile").val();
    var subject = $("#subject").val();
    var message = $("#message").val();
    console.log(name + ":" + email + ":" + mobile + ":" + subject + ":" + message);

    var frm = document.getElementById('contactUsForm');
    var form_data = new FormData();

    console.log(form_data);

    form_data.append('name', name);
    form_data.append('email', email);
    form_data.append('mobile', mobile);
    form_data.append('subject', subject);
    form_data.append('message', message);
    console.log(form_data);


    for (var p of form_data) {
        console.log(p);
    }


    var fm = $('form')[0]
    var fData = new FormData(fm);
    /*
        for (var p of fData) {
            console.log(p);
        }

        var uploadFile = $('#upload').val();
        console.log("upload " + uploadFile);
        //var frm = document.getElementById('contactUsForm');
        var form_Data = new FormData();


        if(uploadFile != null){
            console.log("HERE !!")
            form_Data.append('file',$('#upload').prop("files")[0]);
        }

        $('#contactUsForm').ajaxForm({
            //type: "POST",
            //url: "/Contact?format=json&action=insert",
            success: function(msg) {
                alert("File has been uploaded successfully");
            },
            error: function(msg) {
                console.log("Error", msg);
            }
        });
    */
    //var frm = document.forms[0];
    //var fData = new FormData(frm);

    //console.log("Form Data" + formData);

    //var uFile = $("#upload")[0].files;

    //console.log(uFile);
    // console.log("File name: " + uFile.fileName);
    // console.log("File size: " + uFile.fileSize);
    // console.log("Binary content: " + uFile.getAsBinary());
//        console.log("Text content: " + uFile.getAsText(""));

    alert('WAIT');

    $.ajax({
        type: "POST",
        url: "/Contact?format=json&action=insert",
        //data:jQuery.param({name:name, email:email, mobile:mobile, subject:subject, message: message}),
        data: fData,
        //data: new FormData(document.getElementById("contactUsForm")),
        contentType: 'multipart/form-data',
        async: false,
        cache: false,
        //contentType: false,
        processData: false,
        //data:formData,
        success: function (data) {
            //console.log("success update");
            window.location.href = "/Devices";
        },
        error: function (e) {
            alert(e);
            console.log("ERROR: ", e);
        },
        done: function (e) {
            alert(e);
            console.log("DONE");
        }
    });

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

