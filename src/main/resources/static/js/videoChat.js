// import Peer from "simple-peer";

let stompClient
const userID = getCookie("user_id")
console.log("This is the userID: ", userID)
let stream
let recievingCall = false
let callAccepted = false
let caller = ""
let callerSignal
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
    stompClient.connect({}, onConnected, onError)
}

const onConnected = () => {
    stompClient.subscribe('/topic/video/' + userID + '/incomingCall', incomingCall)
    stompClient.subscribe('/topic/video/' + userID + '/callAccepted', onCallAccepted)
    // stompClient.subscribe('/topic/video/' + userID + '/', onMessageReceived)
}

const onError = () => {
    console.log("Error with socket connection!")
}

const incomingCall = (payload) => {
    const message = JSON.parse(payload.body);
    console.log(message)
    recievingCall = true
    caller = message.caller
    callerSignal = message.signal
}

const onCallAccepted = (payload) => {
    console.log(message)
}

connect({})