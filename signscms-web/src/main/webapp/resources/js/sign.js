/**
 * Created by administrator on 19/4/17.
 */

var signDisplayed;
var sectionDisplayed;
var hostname;
var section;
var border;

function getSignForDevice() {

    getSignImages();
    // Poll Frequency set to 2 secs
    window.setInterval(getSignImages, 2000);
    //window.setInterval(getSignImages, 200000);
}

function getSignImages() {
    hostname = getUrlParameter('hostname');
    section = getUrlParameter('section');
    border = getUrlParameter('border');
    var url;
    if (section != null) {
        url = "/Sign?format=json&section=" + section;
        getSectionDetails(url);
    } else {
        url = "/Sign?format=json&hostname=" + hostname;
        getSignDetails(url);
    }
}

function getSignDetails(url) {
    $.ajax({
        //type : "POST",
        contentType: "application/json",
        url: url,
        //data : JSON.stringify(search),
        dataType: 'json',
        timeout: 100000,
        success: function (data) {
            display(data);
        },
        error: function (e) {
            console.log("ERROR: ", e);
            //display(e);
        },
        done: function (e) {
            console.log("DONE");
        }
    });
}

function getSectionDetails(url) {

    $.ajax({
        contentType: "application/json",
        url: url,
        dataType: 'json',
        timeout: 100000,
        success: function (data) {
            //console.log("SUCCESS: ", data);
            displaySection(data);
        },
        error: function (e) {
            console.log("ERROR: ", e);
        },
        done: function (e) {
            console.log("DONE");
        }
    });

}

function displaySection(data) {

    if (border != null) {
        displaySectionNoBorder(data);
    } else {

        var refreshDom = false;

        if (sectionDisplayed != null) {
            var jsonSectionDisplayed = JSON.stringify(sectionDisplayed);
            var jsonSectionData = JSON.stringify(data);

            if (jsonSectionDisplayed == jsonSectionData) {
            } else {
                sectionDisplayed = data;
                refreshDom = true;
            }
        } else {
            // First time - sign Displayed null
            sectionDisplayed = data;
            refreshDom = true;
        }


        if (data != null && refreshDom == true) {
            $("#outer").empty();

            //console.log(JSON.stringify(data));
            var len = data.length;
            for (var i = 0; i < len; i++) {


                jQuery('<div/>', {
                    id: 'div_base_' + i,
                    class: 'outerbasecontainer',
                    width: data[i].pixelsHorizontal + 96,
                    height: data[i].pixelsVertical
                }).appendTo('#outer');

                jQuery('<div/>', {
                    id: 'div_label_' + i,
                    class: 'labelContainerSign',
                    width: "96px",
                    height: "96px",
                    text: data[i].cnfDevice.pi_name
                }).appendTo('#div_base_' + i);

                var images_size = data[i].images.length;
                for (var j = 0; j < images_size; j++) {
                    jQuery('<div/>', {
                        id: 'div_image_' + i + '_' + j,
                        class: 'imageContainer'
                    }).appendTo('#div_base_' + i);
                    if (data[i].viewName == 'CLOCK') {
                        jQuery('#div_image_' + i + '_' + j).load('resources/html/section_clock/clock1_' + i + '.html');
                        jQuery('<div/>', {
                            id: 'div_image_1' + i + '_' + j,
                            class: 'imageContainer'
                        }).appendTo('#div_base_' + i);
                        jQuery('#div_image_1' + i + '_' + j).load('resources/html/section_clock/clock10_' + i + '.html');
                    } else {
                        jQuery('<img/>', {
                            id: 'image_' + i + '_' + j,
                            src: data[i].images[j]
                        }).appendTo('#div_image_' + i + '_' + j);

                    }// End of IF Clock
                }
                if (data[i].viewName == 'TEST') {
                    var topPosition = (i * 96) + 58;
                    jQuery('<div/>', {
                        id: 'div_h3_' + i,
                        class: 'indicator_test'
                    }).appendTo('#div_base_' + i);

                    $('#div_h3_' + i).css('top', topPosition + 'px');
                    jQuery('<h3/>', {
                        id: 'h3_' + i,
                        text: "Sign under Test"
                    }).appendTo('#div_h3_' + i);
                }
            }
        }
    }
}

function displaySectionNoBorder(data) {

    //console.log("display without border");
    var refreshDom = false;

    if (sectionDisplayed != null) {
        var jsonSectionDisplayed = JSON.stringify(sectionDisplayed);
        var jsonSectionData = JSON.stringify(data);

        if (jsonSectionDisplayed == jsonSectionData) {
        } else {
            sectionDisplayed = data;
            refreshDom = true;
        }
    } else {
        // First time - sign Displayed null
        sectionDisplayed = data;
        refreshDom = true;
    }


    if (data != null && refreshDom == true) {
        $("#outer").empty();

        //console.log(JSON.stringify(data));
        var len = data.length;
        for (var i = 0; i < len; i++) {


            jQuery('<div/>', {
                id: 'div_base_' + i,
                class: 'outerbasecontainernoborder',
                width: data[i].pixelsHorizontal,
                height: data[i].pixelsVertical
            }).appendTo('#outer');



            var images_size = data[i].images.length;
            for (var j = 0; j < images_size; j++) {
                jQuery('<div/>', {
                    id: 'div_image_' + i + '_' + j,
                    class: 'imageContainer'
                }).appendTo('#div_base_' + i);
                if (data[i].viewName == 'CLOCK') {
                    jQuery('#div_image_' + i + '_' + j).load('resources/html/section_clock/clock1_' + i + '.html');
                    jQuery('<div/>', {
                        id: 'div_image_1' + i + '_' + j,
                        class: 'imageContainer'
                    }).appendTo('#div_base_' + i);
                    jQuery('#div_image_1' + i + '_' + j).load('resources/html/section_clock/clock10_' + i + '.html');
                } else {
                    jQuery('<img/>', {
                        id: 'image_' + i + '_' + j,
                        src: data[i].images[j]
                    }).appendTo('#div_image_' + i + '_' + j);

                }// End of IF Clock
            }
            if (data[i].viewName == 'TEST') {
                var topPosition = (i * 96) + 58;
                jQuery('<div/>', {
                    id: 'div_h3_' + i,
                    class: 'indicator_test'
                }).appendTo('#div_base_' + i);

                $('#div_h3_' + i).css('top', topPosition + 'px');
                jQuery('<h3/>', {
                    id: 'h3_' + i,
                    text: "Sign under Test"
                }).appendTo('#div_h3_' + i);
            }
        }
    }
}

function display(data) {

    var refreshDom = false;

    if (signDisplayed != null) {
        var jsonSignDisplayed = JSON.stringify(signDisplayed);
        var jsonData = JSON.stringify(data);
        //console.log(jsonData);
        if (jsonSignDisplayed == jsonData) {
        } else {
            signDisplayed = data;
            refreshDom = true;
        }
    } else {
        // First time - sign Displayed null
        signDisplayed = data;
        refreshDom = true;
    }


    if (data != null && refreshDom == true) {
        $("#baseContainer").empty();
        $("#labelContainer").empty();
        $("#labelContainer").addClass('labelContainerSign');
        //var displayText= "Device - \n" + data.cnfDevice.pi_name;
        $("#labelContainer").text(data.cnfDevice.pi_name);

        $("#baseContainer").width(data.pixelsHorizontal);
        $("#baseContainer").height(data.pixelsVertical);
        $("#baseContainer").css('float', 'left');
        if (data.images != null) {
            var images_size = data.images.length;
            for (var i = 0; i < images_size; i++) {
                jQuery('<div/>', {
                    id: 'div_image_' + i,
                    class: 'imageContainer'
                }).appendTo('#baseContainer');
                if (data.viewName == 'CLOCK') {

                    jQuery('<div>', {
                        id: 'div_clock_' + i,
                        class: 'clock'
                    }).appendTo('#div_image_', i);


                    /* jQuery('#div_image_' + i).load('resources/html/clock.html');
                     jQuery('<div/>', {
                         id: 'div_image_1' + i,
                         class: 'imageContainer'
                     }).appendTo('#baseContainer');
                     jQuery('#div_image_1' + i).load('resources/html/clock2.html');*/
                } else {
                    jQuery('<img/>', {
                        id: 'image_' + i,
                        src: data.images[i]
                    }).appendTo('#div_image_' + i);
                    if (data.viewName == 'TEST') {
                        jQuery('<div/>', {
                            id: 'div_h3_' + i,
                            class: 'indicator_test'
                        }).appendTo('#baseContainer');

                        jQuery('<h3/>', {
                            id: 'h3_' + i,
                            text: "Sign under Test"
                        }).appendTo('#div_h3_' + i);
                    }

                }// End of Clock
            }
        }
    }
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

function update(id) {
    $(id).html(moment().format('HH:mm:ss'));
}
