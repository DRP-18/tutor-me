'use strict';
let stompClient;
const userId = getCookie("user_id");
var initalName = document.getElementById("nameUnderProfilePic").innerText
var initalStatus = document.getElementById("statusUnderProfilePic").innerText
var initialAvatar = document.getElementById("profilePic").alt

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
  initalName = name
  initalStatus = status
  initialAvatar = avatar
  document.getElementById("updateProfile").disabled = true
  document.getElementById("nameUnderProfilePic").innerText = initalName
  document.getElementById("statusUnderProfilePic").innerText = initalStatus
};

function enableUpdate() {
  const name = document.getElementById("newName").value.trim();
  const status = document.getElementById("newStatus").value.trim();
  const avatar = document.getElementById("profilePic").alt;
  const updateBtn = document.getElementById("updateProfile")
  updateBtn.disabled = !((avatar !== initialAvatar) || (status !== initalStatus)
      || (name !== initalName))
}

function removeTutee(id) {
  console.log("Removing " + id)
  stompClient.send("/app/user.removeTutee", {},
      JSON.stringify({
        tutorId: userId, tuteeId: id
      }));
  const row = document.getElementById("r-" + id).parentNode.parentNode
  row.parentNode.removeChild(row)
}

document.getElementById("updateProfile").addEventListener("click", saveDetails);
if (userId != null) {
  const socket = new SockJS('/textChat-chat');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, onConnected, onError)
}

document.getElementById("newName").onchange = enableUpdate
document.getElementById("newStatus").onchange = enableUpdate
