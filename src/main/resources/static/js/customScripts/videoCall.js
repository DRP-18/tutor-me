'use strict';

let stompClient;
/* Pass full URL of deployed server*/
// const socket = io("ws://localhost:5000");

/*These are state fields */
let stream;
let me = "";
let allUsersDetails = {};
let call = {};
let callAccepted = false;
let callEnded = true;
let myName = "";
let iceCandidates = {};
const userID = getCookie("user_id");
let mySrcObject = {};
let theirSrcObject = {};
let connectionRef = {};
let personCalling = "";

let groupCallId;
let peers = [];
let calls = [];
let peerSrcObjects = [];
let peersConnected;
const allVideoElements = ["myVideo", "theirVideo", "myGroupVideo",
  "dashBoardVideo", "peerVideo1", "peerVideo2", "peerVideo3", "peerVideo4",
  "peerVideo5"];

const connect = (event) => {
  const socket = new SockJS('/videoCall-video');
  stompClient = Stomp.over(socket);
  /* Get permission for microphone and webcam*/
  navigator.mediaDevices.getUserMedia(
      {video: true, audio: true}) /* returns a promise*/
  .then((currentStream) => {
    stream = currentStream;
    mySrcObject = currentStream;
    document.getElementById("myVideo").srcObject = currentStream;
    document.getElementById("dashBoardVideo").srcObject = currentStream;
    document.getElementById("myGroupVideo").srcObject = currentStream
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
  stompClient.subscribe('/topic/video/' + userID + '/userDetails',
      saveUserDetails);
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

function indivCallSetUp() {
  stompClient.subscribe('/topic/video/' + userID + '/incomingCall',
      incomingCall);
  stompClient.subscribe('/topic/video/' + userID + '/endCall', leaveCall);
  stompClient.subscribe('/topic/video/' + userID + '/alreadyInCall',
      alreadyInCall);
}

function findUsersName(id) {
  for (const [k, v] of Object.entries(allUsersDetails)) {
    if (k == id) {
      return v
    }
  }
}

const saveUserDetails = (payload) => {
  console.log("This is the " + payload);
  const message = JSON.parse(payload.body);
  allUsersDetails = JSON.parse(message.details);
  let name;
  for (const [k, v] of Object.entries(allUsersDetails)) {
    if (k == userID) {
      name = v.name;
      delete allUsersDetails[k];
      break
    }
  }
  me = userID;
  myName = name;
  document.getElementById("myGroupVideoName").innerText = myName;
  let counter = 1;
  for (const [k, v] of Object.entries(allUsersDetails)) {
    addDropDownOption(k, v.name);
    if (counter <= 3) {
      addDashBoardQuickCallOption(k, v, counter)
    }
    counter++;
  }
};

function addDropDownOption(id, name, group) {
  const newConvDropDown = document.getElementById('newConvDropDown');
  const groupNewConvDropDown = document.getElementById('groupNewConvDropDown');
  const li = document.createElement('li');
  const aTag = document.createElement('a');
  aTag.onclick = function () {
    callUser(id, false)
  };
  aTag.innerText = name;
  li.appendChild(aTag);
  newConvDropDown.appendChild(li);
  li.removeChild(aTag);

  const aTagGroup = document.createElement('a');
  aTagGroup.onclick = function () {
    callUser(id, true)
  };
  aTagGroup.innerText = name;

  li.appendChild(aTagGroup);
  groupNewConvDropDown.appendChild(li)
}

function addDashBoardQuickCallOption(id, user, userNumber) {
  const nameTag = document.getElementById(
      'nameUnderProfilePicUser' + userNumber);
  nameTag.innerText = user.name;
  const statusTag = document.getElementById(
      'statusUnderProfilePicUser' + userNumber);
  statusTag.innerText = user.status;
  const imgTag = document.getElementById('profilePicUser' + userNumber);
  imgTag.src = '/img/avatars/' + user.avatar + '.png';
  imgTag.alt = user.avatar;
  const userTag = document.getElementById('user' + userNumber);
  userTag.style.display = 'block'
}

const saveIceCandidates = (payload) => {
  console.log("This is the " + payload);
  const message = JSON.parse(payload.body);
  console.log("These are the ice candidates " + message);
  iceCandidates = message;
};

const incomingCall = (payload, group = false) => {
  const message = JSON.parse(payload.body);
  console.log("Person calling me " + message.callerName);

  call = {
    isReceivedCall: true,
    from: message.caller,
    name: message.callerName,
    signal: message.signal
  };
  console.log("incoming call " + message);
  if (group) {
    document.getElementById('groupAccept').style.display = "block";
    const callNotification = document.getElementById('groupCallNotification');
    callNotification.innerText = "Incoming group call from "
        + message.callerName
  } else {
    document.getElementById('accept').style.display = "block";
    const callNotification = document.getElementById('callNotification');
    callNotification.innerText = "Incoming call from " + message.callerName
  }
};

// acceptCall
const answerCall = (group) => {
  document.getElementById('callNotification').innerText = "";
  document.getElementById('groupCallNotification').innerText = "";
  document.getElementById('end').style.display = "block";
  document.getElementById('shareScreen').style.display = "block";
  Array.from(document.getElementsByClassName('dropDownText')).forEach(
      it => it.style.display = "none");
  document.getElementById('newCallDropDown').style.display = "none";
  document.getElementById('accept').style.display = "none";
  document.getElementById('groupAccept').style.display = "none";

  if (group) {
    document.getElementById(
        "groupCallNotification").innerText = "Connecting...";
    stompClient.send("/app/video.groupAcceptCall", {},
        JSON.stringify({callee: userID, caller: call.from}))
  } else {

    callAccepted = true;
    callEnded = false;
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
  }
};

function setTheirStream(currentStream) {
  theirSrcObject = currentStream;
  const theirVid = document.getElementById("theirVideo");
  theirVid.srcObject = currentStream;
  theirVid.style.display = "block";
  theirVid.controls = "controls";
}

function setGroupStream(currentStream, name = "") {
  peerSrcObjects.push(currentStream);
  const theirVid = document.getElementById("peerVideo" + peersConnected);
  theirVid.srcObject = currentStream;
  theirVid.style.display = "block";
  theirVid.controls = "controls";
  theirVid.parentElement.style.display = "block";
  const theirVidName = document.getElementById(
      "peerVideo" + peersConnected + "Name");
  theirVidName.innerText = name;
  setVideoDimensions(theirVid)
}

// CallPeer
const callUser = (id, group) => {
  /*we are the person calling */
  personCalling = allUsersDetails[id].name;
  if (group) {
    document.getElementById('groupCallNotification').innerText = "Calling "
        + personCalling;
    stompClient.send("/app/video.groupCallUser", {},
        JSON.stringify({callee: id, caller: userID}))
  } else {
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
        + personCalling;

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
      document.getElementById('groupCallNotification').innerText = personCalling
          + " is connecting...";
      document.getElementById('end').style.display = "block";
      document.getElementById('shareScreen').style.display = "block";
      Array.from(document.getElementsByClassName('dropDownText')).forEach(
          it => it.style.display = "none");
      document.getElementById('newCallDropDown').style.display = "none";
      callAccepted = true;
      callEnded = false;
      const message = JSON.parse(signal.body);
      call = {name: findUsersName(id), from: id};
      peer.signal(message.signal)
    };
    stompClient.subscribe('/topic/video/' + userID + '/callAccepted',
        onCallAccepted);

    connectionRef = peer;
  }
};

function clearMessage() {
  document.getElementById('callNotification').innerText = "";
  document.getElementById('groupCallNotification').innerText = "";
}

function alreadyInCall() {
  document.getElementById('callNotification').innerText = personCalling
      + " is already in a call";
  setTimeout(clearMessage, 4500);
  resetCall()
}

function resetCall() {
  callAccepted = false;
  callEnded = true;
  call = {};
  theirSrcObject.srcObject = {};
  const video = document.getElementById("theirVideo");
  video.style.display = "none";
  video.controls = ""
}

const leaveCall = () => {
  document.getElementById('end').style.display = "none";
  document.getElementById('shareScreen').style.display = "none";
  Array.from(document.getElementsByClassName('dropDownText')).forEach(
      it => it.style.display = "block");
  document.getElementById('newCallDropDown').style.display = "block";
  console.log("LEAVING THE CALL " + call.from);
  if (callEnded === false) {
    stompClient.send("/app/video.endCall", {},
        JSON.stringify({callee: userID, caller: call.from}));
  }
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

function setVideoDimensions(vid) {
  let width;
  width = vid.parentNode.offsetWidth;
  vid.height = 400;
  vid.width = width;
}

function individualCalls() {
  document.getElementById("callDashboard").style.display = "none";
  document.getElementById("individualCall").style.display = "block";
  const myVid = document.getElementById("myVideo");
  const theirVid = document.getElementById("theirVideo");
  setVideoDimensions(myVid);
  setVideoDimensions(theirVid);
  indivCallSetUp()
}

function groupCalls() {
  document.getElementById("callDashboard").style.display = "none";
  document.getElementById("groupCall").style.display = "block";
  const myGroupVideo = document.getElementById("myGroupVideo");
  setVideoDimensions(myGroupVideo);
  groupCallSetUp()
}

function groupCallSetUp() {
  stompClient.subscribe('/topic/video/' + userID + '/groupId',
      newGroupCallID);
  stompClient.subscribe('/topic/video/' + userID + '/groupIncomingCall',
      groupIncomingCall);
  stompClient.subscribe('/topic/video/' + userID + '/newPeer',
      addJoiningPeer);
  stompClient.subscribe('/topic/video/' + userID + '/peersInRoom',
      createPeersInRoom);
  stompClient.subscribe('/topic/video/' + userID + '/returningSignal',
      sendReturningSignal);

  peersConnected = 0;
}

function getGroupId() {
  document.getElementById("startGroupCall").style.display = "none";
  document.getElementById("groupNewCallDropDown").style.display = "block";
  stompClient.send("/app/video.getGroupId", {},
      JSON.stringify({message: userID}));
}

const newGroupCallID = (payload) => {
  groupCallId = JSON.parse(payload.body);
  console.log("group id is: " + groupCallId)
};

const groupIncomingCall = (payload) => {
  incomingCall(payload, true)
};

function resizeAllVideos() {
  for (const videoTagName in allVideoElements) {
    const vidTag = document.getElementById(videoTagName);
    setVideoDimensions(vidTag);
  }
}

const addJoiningPeer = (payload) => {
  const message = JSON.parse(payload.body);
  document.getElementById(
      'groupCallNotification').innerText = message.callerName
      + " is joining the call";
  document.getElementById('groupCallNotification').style.display = 'block';
  setTimeout(clearMessage, 5000);

  const peer = new SimplePeer({
    initiator: false, trickle: false,
    reconnectTimer: 100,
    iceTransportPolicy: 'relay',
    config: {
      iceServers: iceCandidates
    }, stream: stream,
  });

  peer.on("signal", (signal) => {
    stompClient.send("/app/video.returnToNewPeer", {},
        JSON.stringify(
            {caller: message.caller, callee: userID, signal: signal}));
  });

  peer.on("stream", (currentStream) => {
    /* This is the other persons stream*/
    peersConnected++;
    setGroupStream(currentStream, message.callerName)
  });

  peer.signal(message.signal);
  peers.push({id: message.peerId, peer: peer});
};

const createPeersInRoom = (payload) => {
  const message = JSON.parse(payload.body);
  document.getElementById(
      "groupCallNotification").innerText = "Joining Call...";
  setTimeout(clearMessage, 4500);
  document.getElementById("startGroupCall").style.display = "none";
  document.getElementById("groupNewCallDropDown").style.display = "block";

  for (let i = 0; i < message.peers.length; i++) {
    const person = message.peers[i];
    const peer = new SimplePeer({
      initiator: true, trickle: false,
      reconnectTimer: 100,
      iceTransportPolicy: 'relay',
      config: {
        iceServers: iceCandidates
      }, stream: stream,
    });

    peer.on("signal", (signal) => {
      stompClient.send("/app/video.sendSignalToPeer", {},
          JSON.stringify(
              {caller: userID, callee: person.id, signal: signal}));
    });

    peer.on("stream", (currentStream) => {
      /* This is the other persons stream*/
      peersConnected++;
      setGroupStream(currentStream, person.name)
    });

    peers.push({id: person.id, peer: peer})
  }
};

const sendReturningSignal = (payload) => {
  const message = JSON.parse(payload.body)
  const item = peers.find(p => p.id == message.callee);
  item.peer.signal(message.signal)
}

window.onload = function () {
  connect({});
  const dashBoardVid = document.getElementById("dashBoardVideo");
  setVideoDimensions(dashBoardVid);
};

function resizeAllVideos() {
  for (let i = 0; i < allVideoElements.length; i++) {
    const vidTag = document.getElementById(allVideoElements[i]);
    setVideoDimensions(vidTag);
  }
}

window.onresize = resizeAllVideos;