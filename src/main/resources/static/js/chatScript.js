'use strict';

let stompClient;
const userId = getCookie("user_id");
let username; // Name of current user
let allMessages; // All the messages that this user has had before
let allUsersDetails; // All ids, user names and further user detail
let currentSelectedChat; // Id of person currently talking to
let notChattedPeople = {}; // All the ids of people this user hasnt chatted with
let myAvatarId;

const connect = (event) => {

  username = getCookie("user_id");

  if (username != null) {
    const socket = new SockJS('/textChat-chat');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected, onError)
  }
};

const onConnected = () => {
  console.log("Running");
  stompClient.subscribe('/topic/chat-' + userId + '-allUserDetails',
      saveUsersDetails);
  stompClient.subscribe('/topic/chat-' + userId + '-allMessages', saveMessages);
  document.getElementById(
      "sendMessage").addEventListener("click", sendMessage);
  stompClient.subscribe('/topic/chat-' + userId + '-receiveMessage',
      receiveMessage);

  // Sent to populate allUsersDetails object
  stompClient.send("/app/chat.getUsersDetails", {},
      JSON.stringify({sender: userId}))

  // stompClient.subscribe('/topic/chat-' + userId, saveUsername)
};

const onError = (error) => {
  console.log("Something went wrong")
};

//Receives the reply of all messages sent by user, parses it and populates allMessages
const saveMessages = (payload) => {
  const message = JSON.parse(payload.body);
  // Receives a map of all the messages sent
  // With empty bodies if conversations haven't been started yet.
  allMessages = JSON.parse(message.messages);
  console.log("Got all of the messages " + allMessages + '-' + message);
  // Saves the uninitiated conversations in a seperate object
  const newMessageOptions = document.getElementById("newMessageOptions");
  // Adds people we havent chatted to to notChattedPeople,
  // and creates dropdown box entries for new conversations option
  for (const [k, v] of Object.entries(allMessages)) {
    console.log(k + " " + v);
    if (v.length === 0) {
      // const option = document.createElement('a');
      // option.setAttribute('onclick', 'newConversation(' + k + ', true)')
      // option.innerText = allUsersDetails[k].name
      // option.classList.add("dropdown-item")
      // option.id = "newChat" + k.toString()
      // newMessageOptions.appendChild(option)
      notChattedPeople[k] = v;
      delete allMessages[k]
    }
  }
};

const saveUsersDetails = (payload) => {
  const message = JSON.parse(payload.body);
  allUsersDetails = JSON.parse(message.details);
  console.log("Got all details " + allUsersDetails + '-' + message);
  for (const [k, v] of Object.entries(allUsersDetails)) {
    if (k === userId) {
      myAvatarId = v.avatar;
      break
    }
  }
  // Sent to populate the allMessages object
  stompClient.send("/app/chat.getMessages", {},
      JSON.stringify({sender: userId}))
};

// Creates a new sidebar entry for chat and removes from new conversations dropdown box
function newConversation(newId, openMessage) {
  console.log("Adding new chat for " + newId);
  allMessages[newId] = notChattedPeople[newId];
  delete notChattedPeople[newId];
  // const newMessageOptions = document.getElementById("newMessageOptions")
  const newIdOption = document.getElementById("newChat" + newId.toString());
  newIdOption.parentNode.removeChild(newIdOption);
  addSideBarEntry(newId);
  if (openMessage) {
    clickOnSideBarMessage(newId)
  }
  $(".chat").niceScroll();
}

// Creates a new side bar element for provided user
function addSideBarEntry(newId) {
  const sideBar = document.getElementById("chatSideBar");
  const entry = document.createElement('div');
  entry.onclick = function () {
    clickOnSideBarMessage(newId)
  };
  const aDiv = document.createElement('a');
  aDiv.classList.add("list-group-item");
  aDiv.classList.add("list-group-item-action");
  aDiv.classList.add("border-0");

  const div = document.createElement('div');
  div.classList.add("badge");
  div.classList.add("bg-success");
  div.classList.add("float-right");
  div.id = "unread-" + newId;
  div.innerText = "5";
  aDiv.appendChild(div);

  const div2 = document.createElement('div');
  div2.classList.add("d-flex");
  div2.classList.add("align-items-start");

  const imgTag = document.createElement('img');
  imgTag.src = '/img/avatars/' + allUsersDetails[newId].avatar + '.png';
  imgTag.classList.add("rounded-circle");
  imgTag.classList.add("mr-1");
  imgTag.width = "40";
  imgTag.height = "40";
  div2.appendChild(imgTag);

  const div3 = document.createElement('div');
  div3.classList.add("flex-grow-1");
  div3.classList.add("ml-3");
  div3.innerText = allUsersDetails[newId].name;

  const div4 = document.createElement('div');
  div4.classList.add("small");

  const span = document.createElement('div');
  span.classList.add("fas");
  span.classList.add("fa-circle");
  span.classList.add("chat-online");
  div4.appendChild(span);
  // div4.innerText = "Online"

  div3.appendChild(div4);
  div2.appendChild(div3);

  aDiv.appendChild(div2);
  entry.appendChild(aDiv);
  sideBar.appendChild(entry)
}

// Opens up the clicked users chats page, to update the top bar information
// and display their associated chats
function clickOnSideBarMessage(clickedId) {
  console.log("clicked on side bar " + clickedId);
  currentSelectedChat = clickedId;
  //Change the name and status at top of page
  document.getElementById(
      "currentChatTopBarName").innerText = allUsersDetails[clickedId].name;
  document.getElementById(
      "currentChatTopBarStatus").innerText = allUsersDetails[clickedId].status;
  document.getElementById("currentUserPic").src = '/img/avatars/'
      + allUsersDetails[clickedId].avatar + '.png';

  //Update the displayed messages
  let messageDiv;
  const chatPanel = document.getElementById("chatPanel");
  chatPanel.innerHTML = "";
  const messageList = allMessages[clickedId.toString()];
  if (messageList != null) {
    messageList.slice().reverse().forEach(function (message) {
      if (message.sender.id != clickedId) {
        messageDiv = addSendingMessageToChatPanel(message.message)
      } else {
        messageDiv = addReceivingMessageToChatPanel(message.message,
            allUsersDetails[clickedId].avatar)
      }
      chatPanel.prepend(messageDiv)
    });
  }

  // Remove the unread notifications text
  const unreadSidebar = document.getElementById("unread-" + clickedId);
  if (unreadSidebar.innerText !== "") {
    unreadSidebar.innerText = ""
  }
}

//message is of the form:
//message: Actual content
//message_id
//sender: This has {id: sender_id, name: sender_name}/
//time: message sent time
function addSendingMessageToChatPanel(messageContent) {
  const chatMsgDiv = document.createElement('div');
  chatMsgDiv.classList.add("chat-message-right");
  chatMsgDiv.classList.add("pb-4");
  const ansRightDiv = document.createElement('div');
  ansRightDiv.classList.add("answer");
  ansRightDiv.classList.add("right");

  const {avatarDiv, textDiv} = addAvatarAndMessage(messageContent, true,
      myAvatarId);

  ansRightDiv.appendChild(avatarDiv);
  ansRightDiv.appendChild(textDiv);
  chatMsgDiv.appendChild(ansRightDiv);
  return chatMsgDiv
}

function addAvatarAndMessage(messageContent, sender, senderAvatarId) {
  const avatarDiv = document.createElement('div');
  avatarDiv.classList.add("avatar");
  const imgTag = document.createElement('img');
  if (sender) {
    imgTag.src = '/img/avatars/' + myAvatarId + '.png';
  } else {
    imgTag.src = '/img/avatars/' + senderAvatarId + '.png';
  }
  const onlineDiv = document.createElement('div');
  onlineDiv.classList.add("status");
  onlineDiv.classList.add("offline");
  avatarDiv.appendChild(imgTag);
  avatarDiv.appendChild(onlineDiv);

  const textDiv = document.createElement('div');
  textDiv.classList.add("text");
  textDiv.innerText = messageContent;
  return {avatarDiv, textDiv};
}

function addReceivingMessageToChatPanel(messageContent, senderAvatarId) {
  const chatMsgDiv = document.createElement('div');
  chatMsgDiv.classList.add("chat-message-left");
  chatMsgDiv.classList.add("pb-4");
  const ansRightDiv = document.createElement('div');
  ansRightDiv.classList.add("answer");
  ansRightDiv.classList.add("left");

  const {avatarDiv, textDiv} = addAvatarAndMessage(messageContent, false,
      senderAvatarId);

  ansRightDiv.appendChild(avatarDiv);
  ansRightDiv.appendChild(textDiv);
  chatMsgDiv.appendChild(ansRightDiv);
  return chatMsgDiv
}

function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop().split(';').shift();
  }
}

const saveUsername = (data) => {
  const message = JSON.parse(data.body);
  username = message.sender;
  console.log(username);

  console.log(JSON.stringify({sender: username, type: 'CONNECT'}));
  stompClient.send("/app/chat.newUser",
      {},
      JSON.stringify({sender: username, type: 'CONNECT'})
  )
};

const sendMessage = () => {
  const messageInput = document.getElementById("messageBox");
  const messageContent = messageInput.value.trim();
  if (messageContent !== null) {
    const chatMessage = {
      content: messageContent,
      sender: username,
      recipient: currentSelectedChat,
      time: moment().calendar()
    };
    console.log("sending message " + chatMessage);
    stompClient.send("/app/chat.send", {}, JSON.stringify(chatMessage));
    messageInput.value = '';
    const chatPanel = document.getElementById("chatPanel");
    chatPanel.append(addSendingMessageToChatPanel(messageContent));

    allMessages[currentSelectedChat].push(
        {sender: {id: userId}, message: messageContent})
  }
};

const receiveMessage = (payload) => {
  const message = JSON.parse(payload.body);
  console.log("received message " + message);
  allMessages[message.sender.id].push(message);
  if (message.sender.id == currentSelectedChat) {
    const chatPanel = document.getElementById("chatPanel");
    chatPanel.append(addReceivingMessageToChatPanel(message.message,
        allUsersDetails[message.sender.id].avatar));
    console.log("add message to panel")
  } else {
    if (message.sender.id in notChattedPeople) {
      newConversation(message.sender.id, false)
    }
    sendNotification(message);
    console.log("sending notification")
  }
};

const sendNotification = (message) => {
  const startingMessage = "Unread: ";
  const messagePreLength = startingMessage.length; // Length of message before number
  const unreadSidebar = document.getElementById("unread-" + message.sender.id);
  if (unreadSidebar.innerText === "") {
    unreadSidebar.innerText = startingMessage + "1"
  } else {
    const alreadyUnseenMessages = unreadSidebar.innerText.slice(
        messagePreLength);
    unreadSidebar.innerText = startingMessage + (parseInt(alreadyUnseenMessages)
        + 1).toString()
  }
};

const hashCode = (str) => {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash)
  }
  return hash
};

const getAvatarColor = (messageSender) => {
  const colours = ['#2196F3', '#32c787', '#1BC6B4', '#A1B4C4'];
  const index = Math.abs(hashCode(messageSender) % colours.length);
  return colours[index]
};

window.onload = function () {
  connect({})
};
