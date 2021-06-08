'use strict'

let stompClient
const userID = getCookie("userid")
console.log("This is the userID: ", userID)
let stream
let userVideo, partnerVideo, socket = {}

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
  }


const connect = (event) => {
    const socket = new SockJS('/videoCall-video')
    console.log("Started connection")
    stompClient = Stomp.over(socket)
    navigator.mediaDevices.getUserMedia({video:true, audio:true}).then(st => {
        stream = st
        if (userVideo.current) {
            userVideo.current.srcObject = st
        }
    })

    // const userID = stompClient.id
    // if (!users[userID]) {
    //     users[userID] = userID;
    // }
    stompClient.connect({}, onConnected, onError)
}

const onConnected = () => {
    stompClient.subscribe('/topic/video', onMessageReceived)
    stompClient.subscribe('/topic/video', onMessageReceived)
    stompClient.subscribe('/topic/video', onMessageReceived)

    stompClient.send("/app/video.getID",{}, {})
}

const onError = () => {
    console.log("Error with socket connection!")
}

const onMessageReceived = (payload) => {
    const message = JSON.parse(payload.body);
    console.log(message)
}

connect({})