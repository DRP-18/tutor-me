import Peer from "simple-peer";
import SockJS from "sockjs-client"
import Stomp from "webstomp-client"

let stompClient
const userID = getCookie("user_id")
console.log("This is the userID: ", userID)
let stream
let recievingCall = false
let callAccepted = false
let caller = ""
let callerSignal
let userVideo, partnerVideo = {}

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}


const connect = (event) => {
    const socket = new SockJS('/videoCall-video')
    console.log("Started connection")
    stompClient = Stomp.over(socket)
    navigator.mediaDevices.getUserMedia({ video: true, audio: true }).then(st => {
        stream = st
        if (userVideo.current) {
            userVideo.current.srcObject = st
        }
    })
    stompClient.connect({}, onConnected, onError)
}

const onConnected = () => {
    stompClient.subscribe('/topic/video/' + userID + '/incomingCall', incomingCall)
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


function callPeer(id) {
    const peer = new Peer({
        initiator: true,
        trickle: false,
        stream: stream,
    });

    peer.on("signal", data => {
        stompClient.send("/app/video.callUser", {},
            JSON.stringify({ callee: id, caller: userID, signal: data }))
    })

    peer.on("stream", st => {
        if (partnerVideo.current) {
            partnerVideo.current.srcObject = st
        }
    });

    stompClient.subscribe('/topic/video/' + userID + '/callAccepted', onCallAccepted)
    const onCallAccepted = (signal) => {
        callAccepted = true
        peer.signal(signal)
    }
};

function acceptCall() {
    callAccepted = true
    const peer = new Peer({
        initiator: false,
        trickle: true,
        stream: stream,
    });

    peer.on("singal", data => {
        stompClient.send("/app/video.acceptCall", {},
            JSON.stringify({ singal: data, callee: caller })) //Since we are returning the message to the caller
    })

    peer.on("stream", st => {
        partnerVideo.current.srcObject = st
    })

    peer.signal(callerSignal)
}

// const Container = styled.div`
//   height: 100vh;
//   width: 100%;
//   display: flex;
//   flex-direction: column;
// `;
//
// const Row = styled.div`
//   display: flex;
//   width: 100%;
// `;
//
// const Video = styled.video`
//   border: 1px solid blue;
//   width: 50%;
//   height: 50%;
// `;

// let UserVideo
// if (stream) {
//     UserVideo = (
//         <Video playsInline muted ref={userVideo} autoPlay />)
//     ;
// }
//
// let PartnerVideo
// if (callAccepted) {
//     PartnerVideo = (<Video playsInline ref={partnerVideo} autoPlay />);
// }
//
// let incomingCall
// if (recievingCall) {
//     incomingCall = (
//         `<div>
//             <h1>{caller} is calling you</h1>
//             <button onClick={acceptCall}>Accept</button>
//         </div>`
//     )
// }
//
// function refresh() {
//     const code =
//     `<Container>
//         <Row>
//             {UserVideo}
//             {PartnerVideo}
//         </Row>
//         <Row>
//             {Object.keys(users).map(key => {
//                 if (key === yourID) {
//                     return null;
//                 }
//                 return (
//                     <button onClick={()} => callPeer(key)}>Call {key}</button>
//                 );
//             })}
//         </Row>
//         <Row>
//             {incomingCall}
//         </Row>
//     </Container>`
//     document.getElementById("videoBlock").innerHTML = code
// }


connect({})
// refresh()
callPeer("1")
acceptCall()