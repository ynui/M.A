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

var DATA_URL = buildUrlWithContextPath("data");
var LOGOUT_URL = buildUrlWithContextPath("logout");

function ajaxUserList() {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getUserList"
        },
        success: showUsers
    });
}

function showUsers(json) {
    var usersPlace = document.getElementById("usersPlace");
    $(usersPlace).empty();
    for (var i = 0; i < json.length; i++) {
        // var newDiv = document.createElement('div');
        // usersPlace.append(newDiv);
        var currUser = document.createElement('button');
        currUser.textContent = json[i];
        usersPlace.appendChild(currUser);
    }

}

function generateUsers() {
    ajaxUserList();
}

window.onload = function () {
    generateUsers();
};
