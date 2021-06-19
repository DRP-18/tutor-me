let sessions;
let calendar;
let events = [];
let selectedEvent;
let formattedTime;
let TIME_TO_SHOW_ERROR = 5000; // in milliseconds
const username = getCookie("user_id");
const userType = getCookie("user_type");
const isTutee = (userType === "tutee");

function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop().split(';').shift();
  }
}

function updateCalendarWithEvents(data) {
  console.log("inside update");
  for (var i = 0; i < data.length; i++) {
    console.log(data[i]);
    const startDate = new Date(data[i].startTime);
    const endDate = new Date(data[i].endTime);
    console.log("startDate ", startDate);
    calendar.addEvent({
      title: data[i].tutees,
      start: startDate,
      end: endDate,
      allDay: false
    })
  }
  console.log("Finished update")
}

function addSession(name, startDate, endDate, arg) {
  console.log("adding a session " + startDate + "-" + endDate);
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
        start: startDate,
        end: endDate,
        allDay: false
      });
    } else {
      rsp.json().then(err => {
        console.log(err.error);
        displayError(err.error)
      })
    }
  })
}

function removeSession(name, startDate, endDate, event) {
  console.log("removing a session");
  fetch("/removeSession", {
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
    console.log(rsp);
    if (rsp.ok) {
      event.remove()
    } else {
      rsp.json().then(err => {
        console.log(err.error);
        displayError(err.error)
      })
    }
  })
}

function displayError(errMsg) {
  document.getElementById("calendarErrorMsg").innerText = errMsg;
  $('#calendarErrorMsg').fadeIn('slow', function () {
    $('#calendarErrorMsg').delay(TIME_TO_SHOW_ERROR).fadeOut();
  });
}

async function getSessions() {
  let response = await fetch("/getSessions", {
    method: 'POST',
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      message: username
    })
  });
  sessions = await response.json();
  updateCalendarWithEvents(sessions)
}

function addSessionWithModalInfo() {
  const name = document.getElementById("tuteeNameModal").value;
  const startDate = document.getElementById("startDateModal").value;
  const endDate = document.getElementById("endDateModal").value;
  addSession(name, startDate, endDate, selectedEvent);
  $('#addSessionModal').modal('hide');
}

// converts "24/06/2021, 12:34:56"
// to "2021-06-24T12:34"
function convertLocalToCorrectFormat(time) {
  const year = time.slice(6, 10);
  const month = time.slice(3, 5);
  const day = time.slice(0, 2);
  const timePart = time.slice(12, 17);
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
      if (isTutee) {
        displayError("Not a tutor, only tutors can organise sessions")
      } else {
        formattedTime = arg.start.toISOString().slice(0, 16);
        selectedEvent = arg;
        const startDate = document.getElementById("startDateModal");
        const endDate = document.getElementById("endDateModal");
        startDate.value = convertLocalToCorrectFormat(
            arg.start.toLocaleString());
        endDate.value = convertLocalToCorrectFormat(arg.end.toLocaleString());
        $('#addSessionModal').modal('show');
      }
      calendar.unselect()
    },
    eventClick: function (arg) {
      if (isTutee) {
        displayError("Not a tutor, only tutors can delete sessions")
      } else {
        if (confirm('Are you sure you want to delete this event?')) {
          const startDate = convertLocalToCorrectFormat(
              arg.event.start.toLocaleString());
          let endDate = startDate;
          if (arg.event.end != null) {
            endDate = convertLocalToCorrectFormat(
                arg.event.end.toLocaleString())
          }
          removeSession(arg.event.title, startDate, endDate, arg.event)
        }
      }
    },
  });
  calendar.render();
  getSessions();
});


