showNotifications();
setInterval(showNotifications, 5000);

function showNotifications() {
    $.ajax({
        url: DATA_URL,
        data: {
            action: "showNotifications"
        },
        success: showNotificationsCallback
    });
}

function showNotificationsCallback(json) {
    $("#notificationPlace").empty();
    var pos = document.getElementById('notificationPlace');
    for (var i = json.length - 1; i >= 0; i--) {
        pos.appendChild(document.createTextNode(json[i]));
        pos.appendChild(document.createElement('br'));
        pos.appendChild(document.createTextNode("------"));
        pos.appendChild(document.createElement('br'));
    }
}
