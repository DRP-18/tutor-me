'use strict';

let stompClient;
/* Pass full URL of deployed server*/
// const socket = io("ws://localhost:5000");

/*These are state fields */
let stream;
let me = "";
let users = {};
let call = {};
let callAccepted = false;
let callEnded = false;
let theirName = "";
let iceCandidates = {};
const userID = getCookie("user_id");
let mySrcObject = {};
let theirSrcObject = {};
var connectionRef = {};

const connect = (event) => {
  const socket = new SockJS('/videoCall-video');
  stompClient = Stomp.over(socket);
  /* Get permission for microphone and webcam*/
  navigator.mediaDevices.getUserMedia(
      {video: true, audio: true}) /* returns a promise*/
  .then((currentStream) => {
    stream = currentStream;
    mySrcObject = currentStream;
    document.getElementById("myVideo").srcObject = currentStream
  });
  stompClient.connect({}, onConnected, onError)
};

function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop().split(';').shift();
  }
}

const onConnected = () => {
  console.log("This is my user ID: " + userID);
  stompClient.subscribe('/topic/video/' + userID + '/incomingCall',
      incomingCall);
  stompClient.subscribe('/topic/video/' + userID + '/username',
      onUsernameReceived);
  stompClient.subscribe('/topic/video/' + userID + '/endCall', leaveCall);
  stompClient.subscribe('/topic/video/' + userID + '/iceCandidates',
      saveIceCandidates);

  stompClient.send("/app/video.getAllUsers", {},
      JSON.stringify({message: userID}));
  stompClient.send("/app/video.iceCandidates", {},
      JSON.stringify({message: userID}))
};

const onError = () => {
  console.log("Error with socket connection!")
};

function findUsersName(id) {
  for (const [k, v] of Object.entries(users)) {
    if (k == id) {
      return v
    }
  }
}

const onUsernameReceived = (payload) => {
  console.log("This is the " + payload);
  const message = JSON.parse(payload.body);
  users = message.data;
  let name;
  for (const [k, v] of Object.entries(message.data)) {
    if (k == userID) {
      name = v;
      delete users[k];
      break
    }
  }
  me = userID;
  theirName = name;
  console.log(Object.keys(users));

  for (const [k, v] of Object.entries(users)) {
    const newConvDropDown = document.getElementById('newConvDropDown');
    const li = document.createElement('li');
    const aTag = document.createElement('a');
    aTag.onclick = function () {
      callUser(k)
    };
    aTag.innerText = v;
    li.appendChild(aTag);
    newConvDropDown.appendChild(li)
  }
};

const saveIceCandidates = (payload) => {
  console.log("This is the " + payload);
  const message = JSON.parse(payload.body);
  console.log("These are the ice candidates " + message);
  iceCandidates = message;
};

const incomingCall = (payload) => {
  const message = JSON.parse(payload.body);
  console.log("Person calling me " + message.callerName);
  document.getElementById('accept').style.display = "block";

  call = {
    isReceivedCall: true,
    from: message.caller,
    name: message.callerName,
    signal: message.signal
  };
  console.log("incoming call " + message);
  const callNotification = document.getElementById('callNotification');
  callNotification.innerText = "Incoming call from " + message.callerName
};

// acceptCall
const answerCall = () => {
  document.getElementById('callNotification').innerText = "";
  document.getElementById('end').style.display = "block";
  document.getElementById('shareScreen').style.display = "block";
  document.getElementById('accept').style.display = "none";

  callAccepted = true;
  /*simple peer library usage */
  /* Initiator is who starts call
      stream from earlier getUserMedia
  */
  const peer = new SimplePeer({
    initiator: false, trickle: false, stream: stream,
    config: {
      iceServers: iceCandidates
    }
  });

  peer.on("signal", (data) => {
    stompClient.send("/app/video.acceptCall", {},
        JSON.stringify({signal: data, callee: userID, caller: call.from})) //Since we are returning the message to the caller
  });

  peer.on("stream", (currentStream) => {
    /* This is the other persons stream*/
    setTheirStream(currentStream)
  });

  peer.signal(call.signal);

  connectionRef = peer;

};

function setTheirStream(currentStream) {
  theirSrcObject = currentStream;
  const theirVid = document.getElementById("theirVideo");
  theirVid.srcObject = currentStream;
  theirVid.style.display = "block"
  theirVid.controls = "controls"
}

// CallPeer
const callUser = (id) => {
  /*we are the person calling */

  const peer = new SimplePeer({
    initiator: true, trickle: false,
    reconnectTimer: 100,
    iceTransportPolicy: 'relay',
    config: {
      iceServers: iceCandidates
    }, stream: stream,
  });
  console.log("The user has been called by " + id);
  console.log("Message being sent: ");
  document.getElementById('callNotification').innerText = "Calling "
      + users[id];

  peer.on("signal", (data) => {
    console.log(JSON.stringify({callee: id, caller: userID, signal: data}));
    stompClient.send("/app/video.callUser", {},
        JSON.stringify({callee: id, caller: userID, signal: data}))
  });

  peer.on("stream", (currentStream) => {
    setTheirStream(currentStream)
  });

  const onCallAccepted = (signal) => {
    document.getElementById('callNotification').innerText = "";
    document.getElementById('end').style.display = "block";
    document.getElementById('shareScreen').style.display = "block";
    callAccepted = true;
    const message = JSON.parse(signal.body);
    call = {name: findUsersName(id), from: id};
    peer.signal(message.signal)
  };
  stompClient.subscribe('/topic/video/' + userID + '/callAccepted',
      onCallAccepted);

  connectionRef = peer;
};

function resetCall() {
  callAccepted = false;
  callEnded = false;
  call = {};
  theirSrcObject.srcObject = {};
  const video = document.getElementById("theirVideo");
  video.style.display = "none";
  video.controls = ""
  // video.srcObject.getVideoTracks().forEach(track => {
  //   track.stop();
  //   video.srcObject.removeTrack(track);
  //   video.style.display = "none"
  // });
}

const leaveCall = () => {
  document.getElementById('end').style.display = "none";
  document.getElementById('shareScreen').style.display = "none";
  console.log("LEAVING THE CALL " + call.from);
  if (callEnded === false) {
    stompClient.send("/app/video.endCall", {},
        JSON.stringify({message: call.from}));
  }
  callEnded = true;
  connectionRef.destroy(); /*Stop receiving input from user camera and microphone */
  // window.location.reload();
  resetCall()
};

function shareScreen() {
  navigator.mediaDevices.getDisplayMedia({cursor: true}).then(
      incomingStream => {
        const screenTrack = incomingStream.getTracks()[0];
        connectionRef.replaceTrack(stream.getVideoTracks()[0], screenTrack,
            stream);
        document.getElementById(
            'callNotification').innerText = "You are Screen Sharing";
        screenTrack.onended = function () {
          document.getElementById('callNotification').innerText = "";
          connectionRef.replaceTrack(screenTrack, stream.getTracks()[1], stream)
        }
      });
}

window.onload = function () {
  connect({})
};
