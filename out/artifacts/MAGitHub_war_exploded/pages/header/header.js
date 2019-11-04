function getUsername() {
    ajaxGetUsername();
}

window.onload = function(){
    getUsername();
};

function showUsername(json) {
    document.getElementById("titlePlace").innerText = "Welcome " + json[0] + "!";
    // var homeIcon = document.createElement('img');
    // homeIcon.setAttribute('src','../../resources/home.png');
    // homeIcon.setAttribute('onClick', 'home()');
    // homeIcon.setAttribute('onClick', 'home()');
    // document.getElementById('titlePlace').appendChild(homeIcon);
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
    innerContent = parent.window.frames['content'].document;
    innerContent.location.reload(true);
}


function ajaxLogout() {
    $.ajax({
        url: LOGOUT_URL,
        type: 'GET',
        success: logoutCallback
    });
}

function logoutCallback() {
    window.top.location.href = "../../index.html";
}

function logout() {
    ajaxLogout();
}
