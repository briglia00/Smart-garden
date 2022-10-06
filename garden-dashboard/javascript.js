<script type="text/javascript">
// Let us open a web socket
var ws = new WebSocket("ws://127.0.0.1:80/");
ws.onopen = function()
{
    // Web Socket is connected, send data using send()
    ws.send("Message to send");
    alert("Message is sent...");
};
ws.onmessage = function (evt)
{
    var received_msg = evt.data;
    alert("Message is received...");
};
ws.onclose = function()
{
    // websocket is closed.
    alert("Connection is closed...");
};
</script>

echo "Reading response:\n\n";
while ($out = socket_read($socket, 2048)) {
    echo "<br><br>$out<br><br>";
}

socket_close($socket);
