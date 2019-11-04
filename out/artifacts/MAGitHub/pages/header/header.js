// extract the context path using the window.location data items
function calculateContextPath() {
    var pathWithoutLeadingSlash = window.location.pathname.substring(1);
    var contextPathEndIndex = pathWithoutLeadingSlash.indexOf('/');
    return pathWithoutLeadingSlash.substr(0, contextPathEndIndex)
}

// returns a function that holds within her closure the context path.
// the returned function is one that accepts a resource to fetch,
// and returns a new resource with the context path at its prefix
function wrapBuildingURLWithContextPath() {
    var contextPath = calculateContextPath();
    return function(resource) {
        return "/" + contextPath + "/" + resource;
    };
}

// call the wrapper method and expose a final method to be used to build complete resource names (buildUrlWithContextPath)
var buildUrlWithContextPath = wrapBuildingURLWithContextPath();

var DATA_URL = buildUrlWithContextPath("data");
var LOGOUT_URL = buildUrlWithContextPath("logout");

function getUsername() {
    ajaxGetUsername();
}

window.onload = function(){
    getUsername();
};

function showUsername(json) {
    document.getElementById("titlePlace").innerText = "Welcome " + json[0] + "!";
}

function ajaxGetUsername() {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getUsername"
        },
        success: showUsername
    });
}

function home() {
    window.open("../welcome/content.html", "content");
}


function ajaxLogout() {
    $.ajax({
        url: LOGOUT_URL,
        type: 'GET',
        success: logoutCallback
    });
}

function logoutCallback() {
    window.top.location.href="../../index.html";
}

function logout() {
    ajaxLogout();
}
