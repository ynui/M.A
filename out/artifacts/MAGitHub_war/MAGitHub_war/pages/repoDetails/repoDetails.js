var repoName;
var remoteRepoName = null;
var remoteRepoOwner = null;
var data = [];
var data2 = [];

function goHome() {
    // window.top.location.reload();
    // innerContent.location.reload(true);
    window.open("../content/content.html", "content");

}

function createNewBranch() {
    var name = prompt("Please enter name for the new branch");
    $.ajax({
        url: DATA_URL,
        data: {
            action: "createNewBranch",
            repository: repoName,
            branchName: name
        },
        success: createNewBranchCallback
    });
}

function createNewBranchCallback(json) {
    if (json[0] === true) {
        alert('A new branch was created successfully!')
        innerContent.location.reload(true)
    } else
        alert(json[0]);
}


function goToUpdateWcPage() {
    window.localStorage.setItem("repoName", repoName);
    window.open("../WC/WC.html", "content");
}

function getRepoName() {
    repoName = localStorage.getItem("repoName");
    // localStorage.removeItem("repoName");
}

function showBranches() {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getBranches",
            repository: repoName
        },
        success: showBranchesCallback,
        error: function () {
            innerContent.location.reload(true)
        }
    });
}

function makeCheckout(branchName) {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "makeCheckout",
            repository: repoName,
            branchName: branchName
        },
        success: makeCheckoutCallback
    });
}

function makeCheckoutCallback(json) {
    if (json[0] === true) {
        alert('Checkout Made Successfully!');
        innerContent.location.reload(true)
    } else
        alert(json[0]);
}

function deleteOneBranch(branchName) {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "deleteBranch",
            repository: repoName,
            branchName: branchName
        },
        success: deleteBranchCallback
    });
}

function deleteBranchCallback(json) {
    if (json[0] === true) {
        alert('Branch was deleted successfully!');
        innerContent.location.reload(true)
    } else
        alert(json[0]);
}

function pullBranch(branchName) {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "pullBranch",
            repository: repoName,
            branchName: branchName
        },
        success: pullBranchCallback
    });
}

function pullBranchCallback(json) {
    if (json[0] === true) {
        alert('Branch was pulled successfully!');
        innerContent.location.reload(true)
    } else
        alert(json[0]);
}


function pushBranch(branchName) {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "pushBranch",
            repository: repoName,
            branchName: branchName
        },
        success: pushBranchCallback
    });
}

function pushBranchCallback(json) {
    if (json[0] === true) {
        alert('Branch was pushed successfully!');
        innerContent.location.reload(true)
    } else
        alert(json[0]);
}


function createPR() {
    var baseName = document.getElementById('baseBranch').value;
    var targetName = document.getElementById('targetBranch').value;
    var note = document.getElementById('massagePR').value;
    // if (baseName === "" || targetName === "" || note === "") {
    if (note === "") {
        alert('All fields must not be empty!');
        return;
    }
    $.ajax({
        url: DATA_URL,
        data: {
            action: "createPR",
            repository: repoName,
            remoteRepoOwner: remoteRepoOwner,
            baseName: baseName,
            targetName: targetName,
            note: note
        },
        success: function () {
            alert('Pull request has been sent!');
            innerContent.location.reload(true)
        }
    });
}


function pushBranchPR(branchName) {
    pushBranch(branchName);
}

function showBranchesCallback(json) {
    if (json != null && json.length > 0) {
        $("#baseBranch").empty();
        var position = document.getElementById("branches");
        position.innerText = "";
        position.appendChild(document.createTextNode("Branches:"));
        position.appendChild(document.createElement('br'));
        for (var i = 0; i < json.length; i += 4) {
            var checkoutBtn = document.createElement('button');
            checkoutBtn.innerText = "Checkout!";
            checkoutBtn.setAttribute('branchName', json[i + 2]);
            checkoutBtn.onclick = function (ev) {
                makeCheckout(this.getAttribute('branchName'));
            };
            var deleteBranchBtn = document.createElement('button');
            deleteBranchBtn.innerText = "Delete";
            deleteBranchBtn.setAttribute('branchName', json[i + 2]);
            deleteBranchBtn.onclick = function (ev) {
                deleteOneBranch(this.getAttribute('branchName'));
            };
            if (json[i + 1] === false) {
                var pushBtn = document.createElement('button');
                pushBtn.innerText = "Push";
                pushBtn.setAttribute('branchName', json[i + 2]);
                pushBtn.onclick = function (ev) {
                    pushBranchPR(this.getAttribute('branchName'));
                };
            } else {
                var menuItem = document.getElementById('baseBranch').appendChild(document.createElement('option'));
                menuItem.value = json[i + 2];
                menuItem.textContent = json[i + 2];
            }
            var pullBtn = document.createElement('button');
            pullBtn.innerText = "Pull";
            pullBtn.setAttribute('branchName', json[i + 2]);
            pullBtn.onclick = function (ev) {
                pullBranch(this.getAttribute('branchName'));
            };
            if (json[i] === true)
                position.appendChild(document.createTextNode("Active --> "));
            position.appendChild(document.createTextNode(json[i + 2]));
            position.appendChild(document.createElement('br'));
            position.appendChild(document.createTextNode(json[i + 3]));
            position.appendChild(document.createElement('br'));
            if (json[i] === true) {
                if (remoteRepoName !== null) {
                    position.appendChild(pullBtn);
                    if (json[i + 1] === false)
                        position.appendChild(pushBtn);
                }
            } else {
                position.appendChild(checkoutBtn);
                position.appendChild(deleteBranchBtn);
            }
            position.appendChild(document.createElement('br'));
        }
    }
}

function showCommits() {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getHeadBranchCommits",
            repository: repoName
        },
        success: getHeadBranchCommitsCallback
    });
}

function showCommitFiles(sha1) {
    showCommitRep(sha1);
}

function showCommitFilesPR(sha1) {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getCommitRep",
            sha1: sha1,
            repository: repoName
        },
        success: showCommitFilesPRCallback
    });
}

function showCommitFilesPRCallback(json) {
    data2 = [];
    for (var i = 0; i < json.length; i += 3) {
        var treeElement = {name: json[i], children: [], sha1: json[i + 1]};
        if (json[i + 2] === "FOLDER") {
            addChildrenToNode(treeElement, json[i + 1]);
        }
        data2.push(treeElement);
    }
    var tree = new TreeView(data2, 'PRtreeView');
    tree.on('select', function (target, data) {
        showFileDataPR(target.data.sha1);
    });
    window.TreeView();
    window.TreeView();
}


function getHeadBranchCommitsCallback(json) {
    if (json != null) {
        var position = document.getElementById("commits");
        position.innerText = "";
        for (var i = 0; i < json.length; i++) {
            var showCommitFilesBtn = document.createElement('button');
            showCommitFilesBtn.setAttribute('sha1', json[i].sha1)
            showCommitFilesBtn.onclick = function (ev) {
                showCommitFiles(this.getAttribute('sha1'));
            };
            showCommitFilesBtn.textContent = "Show Files";
            position.appendChild(document.createTextNode(json.length - i + "."));
            position.appendChild(document.createElement('br'));
            position.appendChild(document.createTextNode("By: " + json[i].author));
            position.appendChild(document.createElement('br'));
            position.appendChild(document.createTextNode("Time: " + json[i].date));
            position.appendChild(document.createElement('br'));
            position.appendChild(document.createTextNode(json[i].note));
            position.appendChild(document.createElement('br'));
            position.appendChild(document.createTextNode(json[i].sha1));
            position.appendChild(document.createElement('br'));
            position.appendChild(showCommitFilesBtn);
            position.appendChild(document.createElement('br'));
        }
    }
}

function getRemoteBranches(remoteRepoOwner, remoteRepoName) {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getRemoteRepoBranches",
            repository: remoteRepoName,
            owner: remoteRepoOwner
        },
        success: getRemoteBranchesCallback,
        error: function () {
            innerContent.location.reload(true)
        }
    });
}

function getRemoteBranchesCallback(json) {
    if (json !== null && json.length > 0) {
        $("#targetBranch").empty();
        for (var i = 0; i < json.length; i++) {
            var menuItem = document.getElementById('targetBranch').appendChild(document.createElement('option'));
            menuItem.value = json[i];
            menuItem.textContent = json[i];
        }
    }
}

function getRemoteRepoName() {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getRemoteRepo",
            repository: repoName
        },
        success: getRemoteRepoNameCallback
    });
}


function getRemoteRepoNameCallback(json) {
    if (json[0] !== false) {
        document.getElementById('createPR').style.visibility = "visible";
        remoteRepoOwner = json[0];
        remoteRepoName = json[1];
        document.getElementById("remoteRepoName").innerText = "Remote Repository:\n" + remoteRepoName + "\nOwner: " + remoteRepoOwner;
        getRemoteBranches(remoteRepoOwner, remoteRepoName);
    }
}

function showPullRequests() {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getPullRequests",
            repository: repoName
        },
        success: getPullRequestsCallback
    });
}

function showRequestCommits(baseBranchName, targetBranchName) {
    document.getElementById('PRfilesData').style.visibility = "visible";
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getPrCommits",
            repository: repoName,
            baseBranch: baseBranchName,
            targetBranch: targetBranchName
        },
        success: showRequestCommitsCallback
    });
}

function showSingleCommitDiff(commitSha1) {
    $.ajax({
        url: DATA_URL,
        async: false,
        data: {
            action: "showCommitDiff",
            repository: repoName,
            commitSha1: commitSha1
        },
        success: showSingleCommitDiffCallback
    });
}

function showSingleCommitDiffCallback(json) {
    var created = json[0];
    var changed = json[1];
    var deleted = json[2];
    if (json[0].length === 0 && json[1].length === 0 && json[2].length === 0) {
        return;
    }
    var pos = document.getElementById('PRcommits');
    pos.appendChild(document.createElement('br'));
    if (created.length > 0) {
        pos.appendChild(document.createTextNode("Created Files:"));
        pos.appendChild(document.createElement('br'));
        for (var i in created) {
            pos.appendChild(document.createTextNode(created[i]));
            pos.appendChild(document.createElement('br'));
        }
    }
    if (changed.length > 0) {
        pos.appendChild(document.createTextNode("Changed Files:"));
        pos.appendChild(document.createElement('br'));
        for (var i in changed) {
            pos.appendChild(document.createTextNode(changed[i]));
            pos.appendChild(document.createElement('br'));
        }
    }
    if (deleted.length > 0) {
        pos.appendChild(document.createTextNode("Deleted Files:"));
        pos.appendChild(document.createElement('br'));
        for (var i in deleted) {
            pos.appendChild(document.createTextNode(deleted[i]));
            pos.appendChild(document.createElement('br'));
        }
    }
}

function showRequestCommitsCallback(json) {
    if (!(json !== null && json.length > 0)) {
        if(json.length === 0)
            alert('No changes!\nNothing to show...');
        return;
    }
    $("#PRcommits").empty();
    var position = document.getElementById('PRcommits');
    for (var i = 0; i < json.length; i++) {
        var showCommitFilesBtn = document.createElement('button');
        showCommitFilesBtn.setAttribute('sha1', json[i].sha1)
        showCommitFilesBtn.onclick = function (ev) {
            showCommitFilesPR(this.getAttribute('sha1'));
        };
        showCommitFilesBtn.innerText = "Show Files";
        position.appendChild(document.createTextNode(json.length - i + "."));
        position.appendChild(document.createElement('br'));
        position.appendChild(document.createTextNode("By: " + json[i].author));
        position.appendChild(document.createElement('br'));
        position.appendChild(document.createTextNode("Time: " + json[i].date));
        position.appendChild(document.createElement('br'));
        position.appendChild(document.createTextNode(json[i].note));
        position.appendChild(document.createElement('br'));
        position.appendChild(document.createTextNode(json[i].sha1));
        position.appendChild(document.createElement('br'));
        position.appendChild(showCommitFilesBtn);
        showSingleCommitDiff(json[i].sha1);
        position.appendChild(document.createElement('br'));
    }
}

function getPullRequestsCallback(json) {
    var position = document.getElementById("PRdetails");
    if (!(json !== null && json.length > 0)) {
        position.appendChild(document.createTextNode("You currently have no pull requests for this repository!"));
        return;
    }
    $("#PRdetails").empty();
    for (var i = 0; i < json.length; i++) {
        position.appendChild(document.createElement('br'));
        position.appendChild(document.createTextNode(i + 1 + "."));
        position.appendChild(document.createElement('br'));
        position.appendChild(document.createTextNode(json[i].message));
        position.appendChild(document.createElement('br'));
        position.appendChild(document.createTextNode("Created by: " + json[i].creator));
        position.appendChild(document.createElement('br'));
        position.appendChild(document.createTextNode("PR data on branch: " + json[i].baseBranchName));
        position.appendChild(document.createElement('br'));
        position.appendChild(document.createTextNode("Branch to be merged: " + json[i].targetBranchName));
        position.appendChild(document.createElement('br'));
        position.appendChild(document.createTextNode("Created on: " + json[i].dateCreated));
        position.appendChild(document.createElement('br'));
        position.appendChild(document.createTextNode("Status: " + json[i].requestStatus));
        position.appendChild(document.createElement('br'));
        if (json[i].requestStatus === "OPEN") {
            var acceptBtn = document.createElement('button');
            var rejectBtn = document.createElement('button');
            acceptBtn.setAttribute('baseBranch', json[i].baseBranchName);
            acceptBtn.setAttribute('targetBranch', json[i].targetBranchName);
            acceptBtn.setAttribute('id', json[i].id);
            rejectBtn.setAttribute('id', json[i].id);
            acceptBtn.innerText = "Accept";
            rejectBtn.innerText = "Reject";
            acceptBtn.onclick = function (ev) {
                var baseBranch = this.getAttribute('baseBranch');
                var targetBranch = this.getAttribute(('targetBranch'));
                var id = this.getAttribute('id');
                $.ajax({
                    url: DATA_URL,
                    data: {
                        action: "acceptPR",
                        repository: repoName,
                        baseBranch: baseBranch,
                        targetBranch: targetBranch,
                        id: id
                    },
                    success: acceptPRCallback
                });
            };
            rejectBtn.onclick = function (ev) {
                var id = this.getAttribute('id');
                var reason = prompt("Please enter your reason for rejecting this pull request:");
                $.ajax({
                    url: DATA_URL,
                    data: {
                        action: "rejectPR",
                        repository: repoName,
                        reason: reason,
                        id: id
                    },
                    success: rejectPRCallback
                });
            };
            position.appendChild(acceptBtn);
            position.appendChild(rejectBtn);
        } else if (json[i].requestStatus === "REJECTED") {
            position.appendChild(document.createTextNode("Reason: " + json[i].rejectMessage));
        }
        if (json[i].requestStatus === "OPEN") {
            var showChanges = document.createElement('button');
            showChanges.innerText = "Show Data";
            showChanges.setAttribute('baseBranch', json[i].baseBranchName);
            showChanges.setAttribute('targetBranch', json[i].targetBranchName);
            showChanges.onclick = function (ev) {
                showRequestCommits(this.getAttribute('baseBranch'), this.getAttribute('targetBranch'));
            }
            position.appendChild(document.createElement('br'));
            position.appendChild(showChanges);
        }
    }
}

function acceptPR() {
    var baseBranch = this.getAttribute('baseBranch');
    var targetBranch = this.getAttribute(('targetBranch'));
    var id = this.getAttribute('id');
    $.ajax({
        url: DATA_URL,
        data: {
            action: "acceptPR",
            repository: repoName,
            baseBranch: baseBranch,
            targetBranch: targetBranch,
            id: id
        },
        success: acceptPRCallback
    });
}

function acceptPRCallback(json) {
    if (json[0] === true) {
        alert('PR was accepted successfully');
        innerContent.location.reload(true)
    } else
        alert(json[0]);
}

function rejectPR(id) {
    var id = this.getAttribute('id');
    var reason = prompt("Please enter your reason for rejecting this pull request:");
    $.ajax({
        url: DATA_URL,
        data: {
            action: "rejectPR",
            repository: repoName,
            reason: reason,
            id: id
        },
        success: rejectPRCallback
    });
}

function rejectPRCallback(json) {
    if (json[0] === true) {
        alert('PR was rejected successfully');
        innerContent.location.reload(true)
    } else
        alert(json[0]);
}

function showReposDetails() {
    getRepoName();
    getRemoteRepoName();
    document.getElementById("repoName").innerText = repoName;
    showBranches();
    showCommits();
    showPullRequests();
}


function ajaxFolderRep(sha1) {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getCommitRep",
            sha1: sha1,
            repository: repoName
        },
        success: folderRepCallback
    });
}

function addChildrenToNode(treeElement, sha1) {
    $.ajax({
        url: DATA_URL,
        async: false,
        data: {
            action: "getFolderRep",
            sha1: sha1,
            repository: repoName
        },
        success: function (json) {
            for (var i = 0; i < json.length; i += 3) {
                var nestedElement = {name: json[i], children: [], sha1: json[i + 1]};
                if (json[i + 2] === "FOLDER") {
                    addChildrenToNode(nestedElement, json[i + 1]);
                }
                treeElement.children.push(nestedElement)
            }
        }
    });
}

function showFileData(sha1) {
    document.getElementById('fileData').innerText = "";
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getFileData",
            sha1: sha1,
            repository: repoName
        },
        success: function (json) {
            document.getElementById('fileData').innerText = json[0];
        }
    });
}

function showFileDataPR(sha1) {
    document.getElementById('PRfileData').innerText = "";
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getFileData",
            sha1: sha1,
            repository: repoName
        },
        success: function (json) {
            document.getElementById('PRfileData').innerText = json[0];
        }
    });
}

function folderRepCallback(json) {
    data = [];
    for (var i = 0; i < json.length; i += 3) {
        var treeElement = {name: json[i], children: [], sha1: json[i + 1]};
        if (json[i + 2] === "FOLDER") {
            addChildrenToNode(treeElement, json[i + 1]);
        }
        data.push(treeElement);
    }
    var tree = new TreeView(data, 'treeView');
    tree.on('select', function (target, data) {
        showFileData(target.data.sha1);
    });
    window.TreeView();
}

function addFolderRepToData(sha1, data) {
    ajaxFolderRep(sha1);
}

function showCommitRep(sha1) {
    addFolderRepToData(sha1);
}


window.onload = function () {
    showReposDetails();
};
