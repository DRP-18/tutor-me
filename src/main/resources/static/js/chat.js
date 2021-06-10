
let socket = new WebSocket(window.location.href.replace("http", "ws") + "/chat")

socket.onopen = function (e) { console.log("Success") }

socket.onmessage = function (e) { 
    addMessageToBacklog(e.data)
}


function addMessageToBacklog (str) {
    var contents = document.getElementById("chatbacklog").value
    document.getElementById("chatbacklog").value = contents + str + "\n"
}
function sendMessage() {
    var message = document.getElementById("chatbox").value;
    addMessageToBacklog(message)
    socket.send("Other Person says: " + message)
}