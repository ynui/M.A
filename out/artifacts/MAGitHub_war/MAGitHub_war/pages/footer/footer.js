ajaxUserList();
setInterval(ajaxUserList,5000);

function ajaxUserList() {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "getUserList"
        },
        success: showUsers
    });
}

function forkRepo(repoName, userName) {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "forkRepo",
            repoToFork: repoName,
            owner: userName
        },
        success: forkRepoFeedback
    });}

    function forkRepoFeedback(json) {
        if (json[0] === true) {
            alert('Forked successfully!');
            innerContent.location.reload(true)
        } else
            alert(json[0]);
    }

function showSomeoneRepos(json) {
    if (json != null) {
        innerContent = parent.window.frames['content'].document;
        var position = innerContent.getElementById('someoneRepos');
        position.innerText = "";
        var header = innerContent.createElement('strong');
        header.innerText = json[json.length - 1] + "'s repositories:";
        header.classList.add('bold');
        position.appendChild(header);

        // innerContent.getElementById('someoneRepos').textContent = json[json.length - 1] + "'s repositories:";
        innerContent.getElementById('someoneRepos').appendChild(innerContent.createElement('br'));
        // $('<span>' + json[0] + "'s repositories:" + '</span>').appendTo($("#ourRepos"));
        for (var i = 0; i < json.length - 1; i += 5) {
            var forkBtn = document.createElement('button');
            forkBtn.textContent = "Fork " + json[i];
            forkBtn.setAttribute('repoName', json[i]);
            forkBtn.setAttribute('owner', json[json.length - 1]);
            forkBtn.onclick = function (ev) {
                forkRepo(this.getAttribute('repoName'), this.getAttribute('owner'));
                // goToRepoDetailsWindow(this.getAttribute('repoName'));
            };
            position.appendChild(innerContent.createTextNode("Name: " + json[i]));
            position.appendChild(innerContent.createElement('br'));
            position.appendChild(innerContent.createTextNode("Active Branch: " + json[i + 1]));
            position.appendChild(innerContent.createElement('br'));
            position.appendChild(innerContent.createTextNode("Number of Branches: " + json[i + 2]));
            position.appendChild(innerContent.createElement('br'));
            position.appendChild(innerContent.createTextNode("Last Commit"));
            position.appendChild(innerContent.createElement('br'));
            position.appendChild(innerContent.createTextNode("Time: " + json[i + 3]));
            position.appendChild(innerContent.createElement('br'));
            position.appendChild(innerContent.createTextNode("Message: " + json[i + 4]));
            position.appendChild(innerContent.createElement('br'));
            position.appendChild(forkBtn);
            position.appendChild(innerContent.createElement('br'));
        }
    }
}

function showUsers(json) {
    var usersPlace = document.getElementById("usersPlace");
    $(usersPlace).empty();
    usersPlace.appendChild(document.createTextNode("MAGit Users: "));
    for (var i = 0; i < json.length; i++) {
        var name = json[i];
        var currUser = document.createElement('button');
        currUser.textContent = json[i];
        currUser.onclick = function (e) {
            $.ajax({
                url: DATA_URL,
                data: {
                    action: "getUserReposByName",
                    username: $(this).text()
                },
                success: showSomeoneRepos
            });
        };
        usersPlace.appendChild(currUser);
    }

}

function generateUsers() {
    ajaxUserList();
}

window.onload = function () {
    generateUsers();
};
