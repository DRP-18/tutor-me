'use strict'

let stompClient
let username

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
  const ID = getCookie("user_id")
  stompClient.send("/app/chat.existingUser", {},
      JSON.stringify({sender: ID, type: 'CONNECT'}))
  stompClient.subscribe('/topic/chat-' + ID, saveUsername)

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
  messageControls.addEventListener('submit', sendMessage, true)
  connect({})
}
