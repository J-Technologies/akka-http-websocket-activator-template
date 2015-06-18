var tweetHtml =
    '<li> \
        <i class="fa fa-user bg-aqua"></i> \
        <div class="timeline-item"> \
        <span class="time"><i class="fa fa-clock-o"></i>02 January</span> \
    <h3 class="timeline-header no-border"><a href="#">__USERNAME__</a>  __TWEET__</h3> \
    </div> \
    </li>';

$(document).ready(function() {
    var user = getUrlParameter('user');
    if (user) {
        $.ajax({
            url: "http://localhost:8080/users/" + user
        }).then(function (tweets) {
            tweets.reverse().forEach(function(tweet) {
               appendTweet(tweet);
            });
        });
    }

    function getUrlParameter(sParam) {
        var sPageURL = window.location.search.substring(1);
        var sURLVariables = sPageURL.split('&');
        for (var i = 0; i < sURLVariables.length; i++)
        {
            var sParameterName = sURLVariables[i].split('=');
            if (sParameterName[0] == sParam)
            {
                return sParameterName[1];
            }
        }
    }
});

var socket = new WebSocket("ws://127.0.0.1:8080/all");

socket.onmessage = function (msg) {
    var tweet = JSON.parse(msg.data);
    appendTweet(tweet);
}

function appendTweet(tweet) {
    $("#tweets li:first-child").after(tweetHtml.replace("__USERNAME__", tweet.user.name).replace("__TWEET__", tweet.text));
}