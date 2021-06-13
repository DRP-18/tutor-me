'use strict'

let stompClient;
const userId = getCookie("user_id")
let username; // Name of current user
let allMessages; // All the messages that this user has had before
let allUsersDetails; // All ids, user names and further user detail
let currentSelectedChat; // Id of person currently talking to
let notChattedPeople = {} // All the ids of people this user hasnt chatted with

const connect = (event) => {

  username = getCookie("user_id")

  if (username != null) {
    const socket = new SockJS('/textChat-chat')
    stompClient = Stomp.over(socket)
    stompClient.connect({}, onConnected, onError)
  }
}

const onConnected = () => {
  stompClient.subscribe('/topic/chat-' + userId + '-allUserDetails',
      saveUsersDetails)
  stompClient.subscribe('/topic/chat-' + userId + '-allMessages', saveMessages)
  document.getElementById("sendMessage").addEventListener("click", sendMessage)
  stompClient.subscribe('/topic/chat-' + userId + '-receiveMessage',
      receiveMessage)

  // Sent to populate allUsersDetails object
  stompClient.send("/app/chat.getUsersDetails", {},
      JSON.stringify({sender: userId}))

  // stompClient.subscribe('/topic/chat-' + userId, saveUsername)
}

const onError = (error) => {
  console.log("Something went wrong")
}

//Receives the reply of all messages sent by user, parses it and populates allMessages
const saveMessages = (payload) => {
  const message = JSON.parse(payload.body)
  // Receives a map of all the messages sent
  // With empty bodies if conversations haven't been started yet.
  allMessages = JSON.parse(message.messages)
  console.log("Got all of the messages " + allMessages + '-' + message)
  // Saves the uninitiated conversations in a seperate object
  const newMessageOptions = document.getElementById("newMessageOptions")
  // Adds people we havent chatted to to notChattedPeople,
  // and creates dropdown box entries for new conversations option
  for (const [k, v] of Object.entries(allMessages)) {
    console.log(k + " " + v)
    if (v.length === 0) {
      const option = document.createElement('a');
      option.setAttribute('onclick', 'newConversation(' + k + ', true)')
      option.innerText = allUsersDetails[k].name
      option.classList.add("dropdown-item")
      option.id = "newChat" + k.toString()
      newMessageOptions.appendChild(option)
      notChattedPeople[k] = v
      delete allMessages[k]
    }
  }
}

const saveUsersDetails = (payload) => {
  const message = JSON.parse(payload.body)
  allUsersDetails = JSON.parse(message.details)
  console.log("Got all details " + allUsersDetails + '-' + message)

  // Sent to populate the allMessages object
  stompClient.send("/app/chat.getMessages", {},
      JSON.stringify({sender: userId}))
}

// Creates a new sidebar entry for chat and removes from new conversations dropdown box
function newConversation(newId, openMessage) {
  console.log("Adding new chat for " + newId)
  allMessages[newId] = notChattedPeople[newId];
  delete notChattedPeople[newId]
  const newMessageOptions = document.getElementById("newMessageOptions")
  const newIdOption = document.getElementById("newChat" + newId.toString())
  newMessageOptions.removeChild(newIdOption)
  addSideBarEntry(newId)
  if (openMessage) {
    clickOnSideBarMessage(newId)
  }
}

// Creates a new side bar element for provided user
function addSideBarEntry(newId) {
  const entry = document.createElement('div')
  entry.classList.add("friend-drawer")
  entry.classList.add("friend-drawer--onhover")
  entry.onclick = function () {
    clickOnSideBarMessage(newId)
  }
  const nameDiv = document.createElement('div')
  nameDiv.classList.add("text")
  const name = document.createElement('h6')
  name.innerText = allUsersDetails[newId].name
  nameDiv.appendChild(name)
  entry.appendChild(nameDiv)
  const sidebar = document.getElementById("sideBarMessages")
  sidebar.appendChild(entry)
  sidebar.appendChild(document.createElement('hr'))
}

// Opens up the clicked users chats page, to update the top bar information
// and display their associated chats
function clickOnSideBarMessage(clickedId) {
  console.log("clicked on side bar " + clickedId)
  currentSelectedChat = clickedId
  //Change the name and status at top of page
  document.getElementById(
      "currentChatTopBarName").innerText = allUsersDetails[clickedId].name
  document.getElementById(
      "currentChatTopBarStatus").innerText = allUsersDetails[clickedId].status

  //Update the displayed messages
  let messageDiv;
  const chatPanel = document.getElementById("chatPanel")
  chatPanel.innerHTML = ""
  const messageList = allMessages[clickedId.toString()]
  if (messageList != null) {
    messageList.slice().reverse().forEach(function (message) {
      if (message.sender.id != clickedId) {
        messageDiv = addSendingMessageToChatPanel(message.message)
      } else {
        messageDiv = addReceivingMessageToChatPanel(message.message)
      }
      chatPanel.prepend(messageDiv)
    });
  }

  // Remove the unread notifications text
  const unreadSidebar = document.getElementById("unread-" + clickedId)
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
  const rowDiv = document.createElement('div')
  rowDiv.classList.add("row")
  rowDiv.classList.add("no-gutters")
  const colDiv = document.createElement('div')
  colDiv.classList.add("col-md-3")
  colDiv.classList.add("offset-md-9")
  const messageDiv = document.createElement('div')
  messageDiv.classList.add("chat-bubble")
  messageDiv.classList.add("chat-bubble--right")
  messageDiv.innerText = messageContent
  colDiv.appendChild(messageDiv)
  rowDiv.appendChild(colDiv)
  return rowDiv
}

function addReceivingMessageToChatPanel(messageContent) {
  const rowDiv = document.createElement('div')
  rowDiv.classList.add("row")
  rowDiv.classList.add("no-gutters")
  const colDiv = document.createElement('div')
  colDiv.classList.add("col-md-3")
  const messageDiv = document.createElement('div')
  messageDiv.classList.add("chat-bubble")
  messageDiv.classList.add("chat-bubble--left")
  messageDiv.innerText = messageContent
  colDiv.appendChild(messageDiv)
  rowDiv.appendChild(colDiv)
  return rowDiv
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
  username = message.sender
  console.log(username)

  console.log(JSON.stringify({sender: username, type: 'CONNECT'}))
  stompClient.send("/app/chat.newUser",
      {},
      JSON.stringify({sender: username, type: 'CONNECT'})
  )
}

const sendMessage = () => {
  const messageInput = document.getElementById("messageBox")
  const messageContent = messageInput.value.trim()
  if (messageContent !== null) {
    const chatMessage = {
      content: messageContent,
      sender: username,
      recipient: currentSelectedChat,
      time: moment().calendar()
    }
    console.log("sending message " + chatMessage)
    stompClient.send("/app/chat.send", {}, JSON.stringify(chatMessage))
    messageInput.value = ''
    const chatPanel = document.getElementById("chatPanel")
    chatPanel.append(addSendingMessageToChatPanel(messageContent))
    allMessages[currentSelectedChat].push(
        {sender: {id: userId}, message: messageContent})
  }
}

const receiveMessage = (payload) => {
  const message = JSON.parse(payload.body)
  console.log("received message " + message)
  allMessages[message.sender.id].push(message)
  if (message.sender.id == currentSelectedChat) {
    const chatPanel = document.getElementById("chatPanel")
    chatPanel.append(addReceivingMessageToChatPanel(message.message))
    console.log("add message to panel")
  } else {
    if (message.sender.id in notChattedPeople) {
      newConversation(message.sender.id, false)
    }
    sendNotification(message)
    console.log("sending notification")
  }
}

const sendNotification = (message) => {
  const startingMessage = "Unread: "
  const messagePreLength = startingMessage.length; // Length of message before number
  const unreadSidebar = document.getElementById("unread-" + message.sender.id)
  if (unreadSidebar.innerText === "") {
    unreadSidebar.innerText = startingMessage + "1"
  } else {
    const alreadyUnseenMessages = unreadSidebar.innerText.slice(
        messagePreLength)
    unreadSidebar.innerText = startingMessage + (parseInt(alreadyUnseenMessages)
        + 1).toString()
  }
}

const hashCode = (str) => {
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash)
  }
  return hash
}

const getAvatarColor = (messageSender) => {
  const colours = ['#2196F3', '#32c787', '#1BC6B4', '#A1B4C4']
  const index = Math.abs(hashCode(messageSender) % colours.length)
  return colours[index]
}

window.onload = function () {
  const messageControls = document.getElementById('message-controls')
  // messageControls.addEventListener('submit', sendMessage, true)
  connect({})

}
