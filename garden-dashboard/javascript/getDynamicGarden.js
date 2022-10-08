$( document ).ready(function() {
    var ws = new WebSocket("ws://localhost:8001/");
    ws.onopen = function()
    {
        // Web Socket is connected, send data using send()
        ws.send("Connecting to the Garden Service");
        alert("Connected");
    };
    ws.onmessage = function (evt) 
    {
        var received_msg = evt.data;
        const myArray = received_msg.split(";");
        //alert(received_msg);
        let elem = document.getElementById(String(myArray[0]));
        elem.innerHTML = String(myArray[1]);
        //alert("Message is received...");
    };
    ws.onclose = function()
    { 
        // websocket is closed.
        let elem = document.getElementById("conn");
        elem.innerHTML = String("Connection Terminated.");
        elem = document.getElementById("conn2");
        elem.innerHTML = String("Refresh to reconnect.");
        alert("Connection Terminated..."); 
    };
 });
