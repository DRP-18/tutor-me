'use strict'

let stompClient;
let username;
let allMessages;
let notChattedPeople = {}

const connect = (event) => {

  username = "New User"

  if (username) {

    const socket = new SockJS('/textChat-chat')
    stompClient = Stomp.over(socket)
    stompClient.connect({}, onConnected, onError)
  }
  // event.preventDefault()
}

const onConnected = () => {
  stompClient.subscribe('/topic/chat', onMessageReceived)
  const userId = getCookie("user_id")
  stompClient.send("/app/chat.getMessages", {},
      JSON.stringify({sender: userId}))
  stompClient.subscribe('/topic/chat-' + userId + '-allMessages', saveMessages)
  // stompClient.subscribe('/topic/chat-' + userId, saveUsername)

}

const saveMessages = (payload) => {
  const message = JSON.parse(payload.body)
  // Receives a map of all the messages sent
  // With empty bodies if conversations havent been started yet.
  allMessages = JSON.parse(message.messages)
  console.log("Got all of the messages " + allMessages + '-' + message)
  // Saves the uninitiated conversations in a seperate object
  const newMessageOptions = document.getElementById("newMessageOptions")
  for (const [k, v] of Object.entries(allMessages)) {
    if (v.length === 0) {
      var option = document.createElement('a')
      option.setAttribute('onclick', 'newConversation(' + k + ')')
      option.innerText = k.toString()
      option.classList.add("dropdown-item")
      option.id = "newChat" + k.toString()
      newMessageOptions.appendChild(option)
      notChattedPeople[k] = v
      delete allMessages[k]
    }
  }
}

function newConversation(newId) {
  console.log("Adding new chat for " + newId)
  const newMessageOptions = document.getElementById("newMessageOptions")
  const newIdOption = document.getElementById("newChat" + newId.toString())
  newMessageOptions.removeChild(newIdOption)
  addSideBarEntry(newId)
}

function addSideBarEntry(newId) {
  const entry = document.createElement('div')
  entry.classList.add("friend-drawer")
  entry.classList.add("friend-drawer--onhover")
  const nameDiv = document.createElement('div')
  nameDiv.classList.add("text")
  const name = document.createElement('h6')
  name.innerText = newId
  nameDiv.appendChild(name)
  entry.appendChild(nameDiv)
  const sidebar = document.getElementById("sideBarMessages")
  sidebar.appendChild(entry)
  sidebar.appendChild(document.createElement('hr'))
}

function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop().split(';').shift();
  }
}

const onError = (error) => {
  console.log("Something went wrong")
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

const sendMessage = (event) => {
  const messageInput = document.querySelector('#message')
  const messageContent = messageInput.value.trim()

  if (messageContent && stompClient) {
    const chatMessage = {
      sender: username,
      content: messageInput.value,
      type: 'CHAT',
      time: moment().calendar()
    }
    stompClient.send("/app/chat.send", {}, JSON.stringify(chatMessage))
    messageInput.value = ''
  }
  event.preventDefault();
}

const onMessageReceived = (payload) => {
  const message = JSON.parse(payload.body);

  const chatCard = document.createElement('div')
  chatCard.className = 'card-body'

  const flexBox = document.createElement('div')
  flexBox.className = 'd-flex justify-content-end mb-4'
  chatCard.appendChild(flexBox)

  const messageElement = document.createElement('div')
  messageElement.className = 'msg_container_send'

  flexBox.appendChild(messageElement)

  if (message.type === 'CONNECT') {
    messageElement.classList.add('event-message')
    message.content = message.sender + ' connected!'
  } else if (message.type === 'DISCONNECT') {
    messageElement.classList.add('event-message')
    message.content = message.sender + ' left!'
  } else {
    messageElement.classList.add('chat-message')

    const avatarContainer = document.createElement('div')
    avatarContainer.className = 'img_cont_msg'
    const avatarElement = document.createElement('div')
    avatarElement.className = 'circle user_img_msg'
    const avatarText = document.createTextNode(message.sender[0])
    avatarElement.appendChild(avatarText);
    avatarElement.style['background-color'] = getAvatarColor(message.sender)
    avatarContainer.appendChild(avatarElement)

    messageElement.style['background-color'] = getAvatarColor(message.sender)

    flexBox.appendChild(avatarContainer)

    const time = document.createElement('span')
    time.className = 'msg_time_send'
    time.innerHTML = message.time
    messageElement.appendChild(time)

  }

  messageElement.innerHTML = message.content

  const chat = document.querySelector('#chat')
  chat.appendChild(flexBox)
  chat.scrollTop = chat.scrollHeight
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

// const loginForm = document.querySelector('#login-form')
// loginForm.addEventListener('submit', connect, true)
window.onload = function () {
  const messageControls = document.getElementById('message-controls')
  // messageControls.addEventListener('submit', sendMessage, true)
  connect({})

}
