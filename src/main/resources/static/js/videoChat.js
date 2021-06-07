'use strict'

let stompClient

const connect = (event) => {
    const socket = new SockJS('/videoCall-video')
    console.log("Started connection")
    stompClient = Stomp.over(socket)
    stompClient.connect({}, onConnected, onError)
}

const onConnected = () => {
    stompClient.subscribe('/topic/video', onMessageReceived)
    stompClient.send("/app/video.message",
        {},
        JSON.stringify({ message: "Test message" })
    )
}

const onError = () => {
    console.log("Error with socket connection!")
}

const onMessageReceived = (payload) => {
    const message = JSON.parse(payload.body);
    console.log(message)
}

connect({})