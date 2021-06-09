import React, { createContext, useState, useRef, useEffect } from "react";
import Peer from "simple-peer";
import SockJS from "sockjs-client"
import Stomp from "stompjs"

const SocketContext = createContext();
let stompClient;





/* Pass full URL of deployed server*/
// const socket = io("ws://localhost:5000");

const ContextProvider = ({ children }) => {
    /*These are state fields */
    const [stream, setStream] = useState(null);
    const [me, setMe] = useState("");
    const [call, setCall] = useState({});
    const [callAccepted, setCallAccepted] = useState(false);
    const [callEnded, setCallEnded] = useState(false);
    const [name, setName] = useState("");
    const userID = getCookie("user_id");



    const myVideo = useRef();
    const userVideo = useRef();
    const connectionRef = useRef();


    useEffect(() => {
        const socket = new SockJS('/videoCall-video')
        stompClient = Stomp.over(socket)
        /* Get permission for microphone and webcam*/
        navigator.mediaDevices.getUserMedia({ video: true, audio: true }) /* returns a promise*/
            .then((currentStream) => {
                setStream(currentStream);
                myVideo.current.srcObject = currentStream;
            });
        stompClient.connect({}, onConnected, onError)
    }, []); /* Has an empty dependancy array*/
    
    function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    }
    
    const onConnected = () => {
        console.log("This is my user ID: " + userID)
        stompClient.subscribe('/topic/video/' + userID + '/incomingCall', incomingCall)
        // stompClient.subscribe('/topic/video/' + userID + '/', onMessageReceived)
    }

    const onError = () => {
        console.log("Error with socket connection!")
    }

    const incomingCall = (payload) => {
        const message = JSON.parse(payload.body);
        setCall({ isReceivedCall: true, from: message.caller, name:message.caller, signal: message.signal})
        console.log("incoming call " + message)
        // recievingCall = true
        // caller = message.caller
        // callerSignal = message.signal
    }

    // acceptCall
    const answerCall = () => {
        setCallAccepted(true);
        /*simple peer library usage */
        /* Initiator is who starts call
            stream from earlier getUserMedia
        */
        const peer = new Peer({ initiator: false, trickle: false, stream });

        peer.on("signal", (data) => {
            stompClient.send("/app/video.acceptCall", {},
            JSON.stringify({ signal: data, callee: call.from })) //Since we are returning the message to the caller
        });

        peer.on("stream", (currentStream) => {
            /* This is the other persons stream*/
            userVideo.current.srcObject = currentStream;
        });

        peer.signal(call.signal);

        connectionRef.current = peer;

    }
    // CallPeer
    const callUser = (id) => {
        /*we are the person calling */
        const peer = new Peer({ initiator: true, trickle: false,       config: {

                iceServers: [
                    {
                        urls: "stun:numb.viagenie.ca",
                        username: "sultan1640@gmail.com",
                        credential: "98376683"
                    },
                    {
                        urls: "turn:numb.viagenie.ca",
                        username: "sultan1640@gmail.com",
                        credential: "98376683"
                    }
                ]
            },
            stream: stream,
        });
        console.log("The user has been called by " + id)
        console.log("Message being sent: ")

        peer.on("signal", (data) => {
        console.log(JSON.stringify({ callee: id, caller: userID, signal: data }))
            stompClient.send("/app/video.callUser", {},
                JSON.stringify({ callee: id, caller: userID, signal: data }))
        });

        peer.on("stream", (currentStream) => {
            if (userVideo.current) {
                userVideo.current.srcObject = currentStream;
            }
        });

        const onCallAccepted = (signal) => {
            setCallAccepted(true);
            peer.signal(signal)
        }
        stompClient.subscribe('/topic/video/' + userID + '/callAccepted', onCallAccepted)
  

        connectionRef.current = peer;
    }

    const leaveCall = () => {
        setCallEnded(true);
        connectionRef.current.destroy(); /*Stop recieving input from user camera and microphone */
        // window.location.reload();
    }

    return (
        /*This exposes all the information in this file to the package */
        <SocketContext.Provider value={{ call, callAccepted, myVideo, userVideo, stream, name, setName, callEnded, me, callUser, leaveCall, answerCall }}>
            {children}
        </SocketContext.Provider >
    );

}

export { ContextProvider, SocketContext };

