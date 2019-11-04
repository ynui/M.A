function checkSession() {
    $.ajax({
        url: "login",
        success: function (json) {
            if (json) {
                // data.redirect contains the string URL to redirect to
                window.location.href = json;
            }
            else
                alert('kaka')
        }
    });
}
