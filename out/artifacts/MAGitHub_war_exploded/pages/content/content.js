function ajaxUserRepos() {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getUserRepos"
        },
        success: ajaxUserReposCallback
    });
}

function goToRepoDetailsWindow(repoName) {
    // var endOfNameIndex = repoName.indexOf(':');
    // var realRepoName = repoName.substr(0, endOfNameIndex);
    window.localStorage.setItem("repoName", repoName);
    // window.top.location.href=("../repoDetails/repoDetails.html");
    window.open("../repoDetails/repoDetails.html", "content");
}

function ajaxUserReposCallback(json) {
    if (json != null) {
        var header = document.createElement('strong');
        header.innerText = json[json.length - 1] + "'s repositories:";
        header.classList.add('bold');
        document.getElementById('ourRepos').appendChild(header);
        document.getElementById('ourRepos').appendChild(document.createElement('br'));
        // $('<span>' + json[0] + "'s repositories:" + '</span>').appendTo($("#ourRepos"));
        for (var i = 0; i < json.length - 1; i += 5) {
            var repoBtn = document.createElement('button');
            repoBtn.textContent = json[i] + " details";
            repoBtn.setAttribute('repoName', json[i]);
            repoBtn.onclick = function (ev) {
                goToRepoDetailsWindow(this.getAttribute('repoName'));
            };
            document.getElementById('ourRepos').appendChild(document.createTextNode("Name: " + json[i]));
            document.getElementById('ourRepos').appendChild(document.createElement('br'));
            document.getElementById('ourRepos').appendChild(document.createTextNode("Active Branch: "+ json[i+1]));
            document.getElementById('ourRepos').appendChild(document.createElement('br'));
            document.getElementById('ourRepos').appendChild(document.createTextNode("Number of Branches: "+ json[i+2]));
            document.getElementById('ourRepos').appendChild(document.createElement('br'));
            document.getElementById('ourRepos').appendChild(document.createTextNode("Last Commit"));
            document.getElementById('ourRepos').appendChild(document.createElement('br'));
            document.getElementById('ourRepos').appendChild(document.createTextNode("Time: "+json[i+3]));
            document.getElementById('ourRepos').appendChild(document.createElement('br'));
            document.getElementById('ourRepos').appendChild(document.createTextNode("Message: "+json[i+4]));
            document.getElementById('ourRepos').appendChild(document.createElement('br'));
            document.getElementById('ourRepos').appendChild(repoBtn);
            document.getElementById('ourRepos').appendChild(document.createElement('br'));
        }
    }
}

function showUserReposDetails() {
    ajaxUserRepos();
}

window.onload = function () {
    localStorage.removeItem('repoName');
    showUserReposDetails();
};

function loadFile(event) {
    $("#errorUploadFileMessage").empty();
    var file = event.target.files[0];
    var reader = new FileReader();
    reader.onload = function () {
        var content = reader.result;
        $.ajax({
            url: UPLOAD_URL,
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
    $("#uploadFileMessage").empty();
    if (json[0] === true) {
        innerContent = parent.window.frames['content'].document;
        innerContent.location.reload(true);
    } else {
        $('<span>' + "Error!" + '<br>' + json[1] + '</span>').appendTo($("#uploadFileMessage"));
    }
}
