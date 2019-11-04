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

var UPLOAD = buildUrlWithContextPath("upload");

function loadFile(event) {
    $("#errorUploadFileMessage").empty();
    var file = event.target.files[0];
    var reader = new FileReader();
    reader.onload = function () {
        var content = reader.result;
        $.ajax({
            url: UPLOAD,
            type: 'POST',
            data: {
                file: content
            },
            success: uploadRepoCallback
        });
    };
    reader.readAsText(file);
}

function uploadRepoCallback(json) {
    if (json == null) {
    } else {
        $('<span>' + json + '</span>').appendTo($("#errorUploadFileMessage"));
    }
}
