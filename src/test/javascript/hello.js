$(document).ready(function() {
    $.ajax({
        url: "http://localhost:9999/users/<user>",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", "Basic <foobar>");
        }
    }).then(function(data, status, jqxhr) {
       $('.name').append(data.name);
       $('.mail').append(data.mail);
       // https://stackoverflow.com/questions/14221722/set-cookie-on-browser-with-ajax-request-via-cors
       // http://dontpanic.42.nl/2015/04/cors-with-spring-mvc.html
       // $.cookie("Set-Cookie", jqxhr.getResponseHeader("Set-Cookie")); // Set-Cookie is secure (cannot be accessed) .. but Browser does not set it as well :(
       console.log(jqxhr);
    });
});