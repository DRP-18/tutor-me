// 'use strict'

// let stompClient;
const userId = getCookie("user_id")
// let username; // Name of current user
// let allMessages; // All the messages that this user has had before

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
      return parts.pop().split(';').shift();
    }
  }

  // Function to show elements from localStorage
function showNotes() {
    let notes = localStorage.getItem("notes");

    if (notes == null){
     notesObj = [];
    } else {
     notesObj = JSON.parse(notes);
    }

    let html = "";

    notesObj.forEach(function(element, index) {
        html += `<div class="noteCard my-2 mx-2 card"
            style="width: 18rem;">
                <div class="card-body">
                    <h5 class="card-title">
                        Note ${index + 1}
                    </h5>
                    <p class="card-text">
                        ${element}
                    </p>

                  <button id="${index}" onclick=
                    "deleteNote(this.id)"
                    class="btn btn-primary">
                    Delete Note
                </button>
            </div>
        </div>`;
    });

    let notesElm = document.getElementById("notes");

    if (notesObj.length != 0) notesElm.innerHTML = html;
     else
        notesElm.innerHTML = `Nothing to show!
        Use "Add a Note" section above to add notes.`;

}

const connect = (event) => {

    username = getCookie("user_id")
    if (username != null) {
      const socket = new SockJS('/notes-note')
      stompClient = Stomp.over(socket)
      stompClient.connect({}, onConnected, onError)
    }

  }
  
  // subscriebe is what we hear back on
  // send is what we are sending.
  const onConnected = () => {
    
    // stompClient.subscribe('/notes-' + userId + '-allNotes', saveNote)
    document.getElementById("addButton").addEventListener("click", addNote)
    
    stompClient.subscribe('/notes-' + userId + '-receiveNotes', receiveNotes)
    // stompClient.subscribe('/notes-' + userId + '-deleteNote', deleteNote)

    // Sent to populate allUsersDetails object
    stompClient.send("/app/chat.getUsersDetails", {},
        JSON.stringify({sender: userId}))
  
        
    showNotes();
    // stompClient.subscribe('/topic/chat-' + userId, saveUsername)
  }
  
  const onError = (error) => {
    console.log("Something went wrong")
  }


  const addNote = () => {
    const addText = document.getElementById("addText");
    const noteContent = addText.value.trim()
    
  if (noteContent !== null) {
        const chatMessage = {
          content: noteContent,
          sender: username,
          recipient: "",
          time: ""
        }
        stompClient.send("/notes.addNote", {}, JSON.stringify(chatMessage))
        noteContent.value = ''
    }
    // let notes = localStorage.getItem("notes");

    // send note to backend
    // add notes to existing ones in BE

    // if (notes == null){
    //  notesObj = [];
    // } else{
    //  notesObj = JSON.parse(notes);
    // }

    // notesObj.push(addText.value);
    // localStorage.setItem("notes", JSON.stringify(notesObj));
    // addText.value = "";

    // showNotes();
};


const receiveNotes = (payload) => {
    const notes = JSON.parse(payload.body)
    // console.log("received message " + message)
    // allMessages[message.sender.id].push(message)
    // if (message.sender.id == currentSelectedChat) {
    //   const chatPanel = document.getElementById("chatPanel")
    //   chatPanel.append(addReceivingMessageToChatPanel(message.message))
    //   console.log("add message to panel")
    // } else {
    //   if (message.sender.id in notChattedPeople) {
    //     newConversation(message.sender.id, false)
    //   }
    //   sendNotification(message)
    //   console.log("sending notification")
    // }
  }
  
// Function to delete a note
const deleteNote = () => {
    //get notes from database 
    let notes = localStorage.getItem("notes");

    if (notes == null) notesObj = [];
    else notesObj = JSON.parse(notes);

    notesObj.splice(index, 1);

    localStorage.setItem("notes",
        JSON.stringify(notesObj));

    showNotes();
}

//addnote
//getnote.

//send a message

//subscribe to channel