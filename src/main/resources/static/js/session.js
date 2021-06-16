let username; // Name of current user
let sessions;
var calendar;
let events = [];
let selectedEvent;
let formattedTime;
username = getCookie("user_id")

function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop().split(';').shift();
  }
}

function updateCalendarWithEvents(data) {
  console.log("inside update")
  for (var i = 0; i < data.length; i++) {
    console.log(data[i])
    const date = new Date(data[i].startTime)
    console.log("date ", date)
    calendar.addEvent({
      title: data[i].tutees,
      start: date,
      end: date,
      allDay: false
    })
  }
  console.log("Finished update")
}

function addSession(name, startDate, endDate, arg) {
  console.log("adding a session " + startDate + "-" + endDate)
  fetch("/addSession", {
    method: 'POST',
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      tutor: username,
      tutees: name,
      startTime: startDate,
      endTime: endDate
    })
  }).then(rsp => {
    if (rsp.ok) {
      calendar.addEvent({
        title: name,
        startStr: startDate,
        endStr: endDate,
        allDay: false
      })
    } else {
      rsp.json().then(err => console.log(err.error))
    }
  })
}

function removeSession(name, startDate, arg) {
  console.log("removing a session")
  fetch("/removeSession", {
    method: 'POST',
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      tutor: username,
      tutees: name,
      startTime: startDate,
      endTime: 5
    })
  }).then(rsp => {
    console.log(rsp)
    arg.event.remove()
  })
  .catch(err => console.log(err))
}

async function getSessions() {
  console.log("inside get session")
  let response = await fetch("/getSessions", {
    method: 'POST',
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      message: username
    })
  })
  console.log("after fetch")
  sessions = await response.json()
  console.log("after json")
  updateCalendarWithEvents(sessions)
}

function addSessionWithModalInfo() {
  const name = document.getElementById("tuteeNameModal").value
  const startDate = document.getElementById("startDateModal").value
  const endDate = document.getElementById("endDateModal").value
  addSession(name, startDate, endDate, selectedEvent)
  $('#addSessionModal').modal('hide');
}

// converts "24/06/2021, 12:34:56"
// to "2021-06-24T12:34"
function convertLocalToCorrectFormat(time) {
  const year = time.slice(6, 10)
  const month = time.slice(3, 5)
  const day = time.slice(0, 2)
  const timePart = time.slice(12, 17)
  return year + '-' + month + '-' + day + 'T' + timePart
}

document.addEventListener('DOMContentLoaded', function () {
  var calendarEl = document.getElementById('calendar');
  calendar = new FullCalendar.Calendar(calendarEl, {
    headerToolbar: {
      left: "prev,next today",
      center: "title",
      right: 'dayGridMonth,dayGridWeek,dayGridDay,listWeek'
    },
    navLinks: true, // can click day/week names to navigate views
    editable: true,
    selectable: true,
    selectMirror: true,
    businessHours: true,
    dayMaxEvents: true, // allow "more" link when too many events
    select: function (arg) {
      formattedTime = arg.start.toISOString().slice(0, 16)
      selectedEvent = arg
      const startDate = document.getElementById("startDateModal")
      const endDate = document.getElementById("endDateModal")
      startDate.value = convertLocalToCorrectFormat(arg.start.toLocaleString())
      endDate.value = convertLocalToCorrectFormat(arg.end.toLocaleString())
      $('#addSessionModal').modal('show');
      calendar.unselect()
    }
  });
  calendar.render();
  getSessions();
});


