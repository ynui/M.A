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
    return function (resource) {
        return "/" + contextPath + "/" + resource;
    };
}

// call the wrapper method and expose a final method to be used to build complete resource names (buildUrlWithContextPath)
var buildUrlWithContextPath = wrapBuildingURLWithContextPath();

var innerContent = parent.window.frames['content'].document;
var DATA_URL = buildUrlWithContextPath("data");
var UPLOAD_URL = buildUrlWithContextPath("upload");
var LOGOUT_URL = buildUrlWithContextPath("logout");
var LOGIN_URL = buildUrlWithContextPath("login");

parent.window.onbeforeunload = function (e) {
    // e.preventDefault();
    $.ajax({
        url: DATA_URL,
        data: {
            action: "clearNotifications"
        },
        success: function () {
            alert('BYE BYE');
        }
    });
};
