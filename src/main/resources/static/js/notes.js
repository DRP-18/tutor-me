// 'use strict'

let stompClient;
const userId = getCookie("user_id")
let username; // Name of current user
let index = 0; // global note index
let html = "";
let nothingTag = true;

// let allMessages; // All the messages that this user has had before

function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop().split(';').shift();
  }
}

// Function to show elements from localStorage
// function showNotes() {
//     let notes = localStorage.getItem("notes");

//     let notesObj;
//     if (notes == null) {
//         notesObj = [];
//     } else {
//         notesObj = JSON.parse(notes);
//     }

//     notesObj.forEach(function(element, index) {
//         html += `<div class="noteCard my-2 mx-2 card"
//             style="width: 18rem;">
//                 <div class="card-body">
//                     <h5 class="card-title">
//                         Note ${index + 1}
//                     </h5>
//                     <p class="card-text">
//                         ${element}
//                     </p>

//                   <button id="${index}" onclick=
//                     "deleteNote(this.id)"
//                     class="btn btn-primary">
//                     Delete Note
//                 </button>
//             </div>
//         </div>`;
//     });

//     let notesElm = document.getElementById("notes");

//     if (notesObj.length != 0) notesElm.innerHTML = html;
//      else
//         notesElm.innerHTML = `Nothing to show!
//         Use "Add a Note" section above to add notes.`;

// }

const addHTML = (element, id) => {
  // html += `<div id="Note ${id}" class="noteCard my-2 mx-2 card"
  //             style="width: 18rem;">
  //                 <div class="card-body">
  //                     <h5 class="card-title">
  //                         Note ${index + 1}
  //                     </h5>
  //                     <p class="card-text">
  //                         ${element}
  //                     </p>

  //                   <button id="${id}" onclick=
  //                     "deleteNote(${id})"
  //                     class="btn btn-primary">
  //                     Delete Note
  //                 </button>
  //             </div>
  //         </div>`;
  // index = index + 1

  console.log("Making note")
  const note = document.createElement('div')
  note.id = ("Note " + id)
  note.classList.add("noteCard")
  note.classList.add("my-2")
  note.classList.add("mx-2")
  note.classList.add("card")

  note.style = ("width: 18rem;")

  console.log("Making inner note")
  const innerNote = document.createElement('div')
  innerNote.classList.add("card-body")

  const header = document.createElement('h5')
  header.innerText = ('Note ' + (++index))

  const paragraph = document.createElement('p')
  paragraph.classList.add("card-text")
  paragraph.innerText = (element)

  const button = document.createElement('button')
  button.id = (id)
  button.onclick = function () {
    deleteNote(id)
  }
  button.classList.add("btn")
  button.classList.add("btn-primary")
  button.innerText = ('Delete Note')

  console.log("putting it togetehr")
  innerNote.appendChild(header)
  innerNote.appendChild(paragraph)
  innerNote.appendChild(button)

  note.appendChild(innerNote)

  const notesDraw = document.getElementById("notes")
  notesDraw.appendChild(note)
}

//make button id the note id

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
  console.log("got here")

  // stompClient.subscribe('/notes-' + userId + '-allNotes', saveNote)
  document.getElementById("addButton").addEventListener("click", addNote)

  stompClient.subscribe('/topic/notes-' + userId + '-receiveNotes',
      receiveNotesAndDisplay)
  stompClient.subscribe('/topic/notes-' + userId + '-newNoteId', getNewNoteId)

  stompClient.send('/app/notes.getNotes', {}, JSON.stringify({message: userId}))

  // stompClient.subscribe('/notes-' + userId + '-deleteNote', deleteNote)

  // Sent to populate allUsersDetails object
  // stompClient.send("/app/chat.getUsersDetails", {},
  //     JSON.stringify({sender: userId}))

  console.log("Connecting")

  // showNotes();
  // stompClient.subscribe('/topic/chat-' + userId, saveUsername)
}

const onError = (error) => {
  console.log("Something went wrong")
}

const addNote = () => {
  const addText = document.getElementById("addText");
  const noteContent = addText.value.trim()

  console.log("adding a new note with context" + noteContent)

  if (noteContent !== null) {
    const noteMessage = {
      content: noteContent,
      sender: username
    }
    stompClient.send("/app/notes.addNote", {}, JSON.stringify(noteMessage))

    //   let notesElm = document.getElementById("notes");

    //   if(nothingTag) {
    //     notesElm.innerHTML = ''
    //     nothingTag = false;
    //   }

    //   addHTML(noteContent)

    //   if (notes.length == 0) {
    //       notesElm.innerHTML = `Nothing to show!
    //       Use "Add a Note" section above to add notes.`;
    //       nothingTag = true;
    // }

    //   notesElm.value = ''

    //   console.log("adding a new note" + noteMessage)
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

const getNewNoteId = (payload) => {
  const addText = document.getElementById("addText");
  const noteContent = addText.value.trim()

  const message = JSON.parse(payload.body)

  const noteId = message.message

  let notesElm = document.getElementById("notes");

  if (nothingTag) {
    notesElm.innerHTML = ''
    nothingTag = false;
  }

  // addHTML(noteContent)    
  addHTML(noteContent, noteId)

  if (notes.length == 0) {
    notesElm.innerHTML = `Nothing to show!
      Use "Add a Note" section above to add notes.`;
    nothingTag = true;
  }

  notesElm.value = ''

  // console.log("adding a new note" + noteMessage)

}

const receiveNotesAndDisplay = (payload) => {

  console.log("Displaying notes" + payload.body)
  const notes = JSON.parse(payload.body)

  let notesElm = document.getElementById("notes");

  if (nothingTag) {
    notesElm.innerHTML = ''
    nothingTag = false;
  }

  notes.map(note => addHTML(note.content, note.id));

  // noteContents.forEach(function(element) { addHTML(element)});

  //add noteid to addhtml function

  if (notes.length == 0) {
    notesElm.innerHTML = `Nothing to show!
          Use "Add a Note" section above to add notes.`;
    nothingTag = true;
  }
}

// Function to delete a note
const deleteNote = (noteId) => {
  //get notes from database
  var elem = document.getElementById("Note " + noteId)
  elem.parentNode.removeChild(elem);

  console.log(noteId)
  stompClient.send('/app/notes.deleteNote', {},
      JSON.stringify({name: userId, status: noteId}))

  // var button = document.getElementById(noteId)
  // button.parentNode.removeChild(button);

  return false;
//     `<div class="noteCard my-2 mx-2 card"
//     style="width: 18rem;">
//         <div class="card-body">
//             <h5 class="card-title">
//                 Note ${index + 1}
//             </h5>
//             <p class="card-text">
//                 ${element}
//             </p>

//           <button id="${id}" onclick=
//             "deleteNote(this.id)"
//             class="btn btn-primary">
//             Delete Note
//         </button>
//     </div>
// </div>`;

  // if (notes == null) notesObj = [];
  // else notesObj = JSON.parse(notes);

  // notesObj.splice(index, 1);

  // localStorage.setItem("notes",
  //     JSON.stringify(notesObj));

  // showNotes();
}

connect({});

//addnote
//getnote.

//send a message

//subscribe to channel