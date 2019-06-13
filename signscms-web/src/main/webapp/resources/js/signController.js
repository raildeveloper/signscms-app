/**
 * Created by administrator on 27/4/17.
 */
function authenticate() {
    //console.log("In authenticate");
    $.ajax({
        type: "POST",
        url: "/Controller?action=authenticate",
        data: $('#Login_Form').serialize(), // serializes the form's elements.
        success: function (data) {
            displayOrForward(data);
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

function displayOrForward(data) {

    //console.log(JSON.stringify(data));
    var authenticated = data.authenticated;
    //.log("authenticated" + authenticated);

    //alert("HERE !!");
    if (authenticated === false) {
        //console.log("HERE !!");
        $('#errorMessage').text("Incorrect Username or Password.");
        //console.log($(errorMessage));
    } else {
        setTimeout(function () {
            window.location.href = "/Devices";
        }, 1000);
    }
}
