var data = [];
var repoName;

function goToRepoDetailsWindow() {
    window.open("../repoDetails/repoDetails.html", "content");
}

function makeCommit() {
    var note = prompt("Please enter note for the new commit:");
    $.ajax({
        url: DATA_URL,
        data: {
            action: "makeCommit",
            repository: repoName,
            note: note
        },
        success: makeCommitFeedback
    });
}

function makeCommitFeedback(json) {
    if (json[0] === true) {
        alert('Commit Made Successfully');
        showStatus();
    } else {
        alert(json[0]);
    }
}

function createFile() {
    var data = document.getElementById('newFileArea').value;
    var name = document.getElementById('newFileName').value;
    if (data === "") {
        alert('Files\'s content cannot be left empty!\nWrite some words...');
        return;
    }
    if (name === "") {
        alert('File\'s name cannot be left empty!\nWrite some words...');
        return;
    }
    $.ajax({
        url: DATA_URL,
        data: {
            action: "createFile",
            repository: repoName,
            content: data,
            name: name
        },
        success: function (json) {
            if (json[0] === true) {
                showStatus();
                alert('Created!');
                document.getElementById('newFileArea').value = "";
                document.getElementById('newFileName').value = "";
            } else {
                alert(json[0]);
            }
        }
    });
}

function deleteFile(path) {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "deleteFile",
            path: path
        },
        success: function (json) {
            if (json[0] === true) {
                showStatus();
                alert('deleted!');
                document.getElementById('dataArea').value = "";
            } else {
                alert(json[0]);
            }
        }
    });
}

function saveChanges(path, data) {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "editFile",
            repository: repoName,
            path: path,
            content: data
        },
        success: function (json) {
            if (json[0] === true) {
                showStatus();
                alert('Saved!');
            } else {
                alert(json[0]);
            }
        }
    });
}


function showWcFiles() {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getFilesFromWC",
            repository: repoName
        },
        success: showWcFilesCallback
    });
}


function addChildrenToNode(treeElement, sha1) {
    $.ajax({
        url: DATA_URL,
        async: false,
        data: {
            action: "getWCFolderRep",
            sha1: sha1,
            repository: repoName
        },
        success: function (json) {
            for (var i = 0; i < json.length; i += 4) {
                var nestedElement = {name: json[i], children: [], sha1: json[i + 1], path: json[i + 3]};
                if (json[i + 2] === "FOLDER") {
                    addChildrenToNode(nestedElement, json[i + 1]);
                }
                treeElement.children.push(nestedElement)
            }
        }
    });
}

function showWcFilesCallback(json) {
    data = [];
    for (var i = 0; i < json.length; i += 4) {
        var treeElement = {name: json[i], children: [], sha1: json[i + 1], path: json[i + 3]};
        if (json[i + 2] === "FOLDER") {
            addChildrenToNode(treeElement, json[i + 1]);
        }
        data.push(treeElement);
    }
    var tree = new TreeView(data, 'treeView');
    tree.on('select', function (target, data) {
        showFileData(target.data.sha1, target.data.path, target.data.name);
    });
    window.TreeView();
}

function showFileData(sha1, path, name) {
    document.getElementById('dataArea').innerText = "";
    document.getElementById('pathPlace').innerText = name;
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getFileDataWC",
            sha1: sha1,
            repository: repoName
        },
        success: function (json) {
            document.getElementById('dataArea').value = json[0];
            document.getElementById('saveBtn').onclick = function (ev) {
                var data = document.getElementById('dataArea').value;
                saveChanges(path, data);
            };
            document.getElementById('deleteBtn').onclick = function (ev) {
                deleteFile(path);
            };
        }
    });
}

function showWcOpenChanges() {
    document.getElementById('createdFiles').innerText = "";
    document.getElementById('changedFiles').innerText = "";
    document.getElementById('deletedFiles').innerText = "";
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getWcStatus",
            repository: repoName
        },
        success: showWcStatus
    });
}

function showWcStatus(json) {
    var created = json[0];
    var changed = json[1];
    var deleted = json[2];
    if (json[0].length === 0 && json[1].length === 0 && json[2].length === 0) {
        document.getElementById('createdFiles').innerText = "Woo-Hoo!\nYour Working Copy is clean as a whistle!";
        document.getElementById('titleMsg').innerText = "";
        return;
    }
    $("#titleMsg").empty();
    document.getElementById('titleMsg').appendChild(document.createTextNode('There are changes in your repository since the last commit:'));
    if (created.length > 0) {
        var pos = document.getElementById('createdFiles');
        pos.appendChild(document.createTextNode("Created Files:"));
        pos.appendChild(document.createElement('br'));
        for (var i in created) {
            var realPath = created[i].substring(13);
            realPath = realPath.substring(realPath.indexOf("\\") + 1);
            pos.appendChild(document.createTextNode(realPath));
            pos.appendChild(document.createElement('br'));
        }
    }
    if (changed.length > 0) {
        pos = document.getElementById('changedFiles');
        pos.appendChild(document.createTextNode("Changed Files:"));
        pos.appendChild(document.createElement('br'));
        for (var i in changed) {
            var realPath = changed[i].substring(13);
            realPath = realPath.substring(realPath.indexOf("\\") + 1);
            pos.appendChild(document.createTextNode(realPath));
            pos.appendChild(document.createElement('br'));
        }
    }
    if (deleted.length > 0) {
        pos = document.getElementById('deletedFiles');
        pos.appendChild(document.createTextNode("Deleted Files:"));
        pos.appendChild(document.createElement('br'));
        for (var i in deleted) {
            var realPath = deleted[i].substring(13);
            realPath = realPath.substring(realPath.indexOf("\\") + 1);
            pos.appendChild(document.createTextNode(realPath));
            pos.appendChild(document.createElement('br'));
        }
    }
}

window.onload = function () {
    repoName = localStorage.getItem("repoName");
    document.getElementById('repoName').innerText = repoName;
    // localStorage.removeItem("repoName");
    showStatus();
};

function showStatus() {
    showWcFiles();
    showWcOpenChanges();
}
