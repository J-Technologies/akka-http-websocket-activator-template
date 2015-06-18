var socket = new WebSocket("ws://127.0.0.1:8080/all");

var tweetHtml =
'<li> \
    <i class="fa fa-user bg-aqua"></i> \
    <div class="timeline-item"> \
    <span class="time"><i class="fa fa-clock-o"></i>02 January</span> \
<h3 class="timeline-header no-border"><a href="#">__USERNAME__</a>  __TWEET__</h3> \
</div> \
</li>';

socket.onmessage = function (msg) {
    var tweet = JSON.parse(msg.data);
    $("#tweets li:first-child").after(tweetHtml.replace("__USERNAME__", tweet.user.name).replace("__TWEET__", tweet.text));
    console.log("Tweet: " + tweet.text);
}