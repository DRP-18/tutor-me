'use strict';
let stompClient;
const userId = getCookie("user_id");

function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop().split(';').shift();
  }
}

const onConnected = () => {
  stompClient.subscribe("/topic/user-" + userId + "-updateProfile")
};

const onError = (error) => {
  console.log("Something went wrong")
};

const saveDetails = () => {
  const name = document.getElementById("newName").value.trim();
  const status = document.getElementById("newStatus").value.trim();
  const avatar = document.getElementById("profilePic").alt;
  stompClient.send("/app/user.updateProfile", {},
      JSON.stringify({
        userId: userId, name: name,
        status: status, avatar: avatar
      }));
};

document.getElementById("updateProfile").addEventListener("click", saveDetails);
if (userId != null) {
  const socket = new SockJS('/textChat-chat');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, onConnected, onError)
}