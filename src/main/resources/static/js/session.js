let username; // Name of current user
let sessions;
let calendar;
let events = [];
const HTTP_SUCCESS = 200;
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
    // const momentDate = moment(data[i].dateTime, 'EEE MMM dd HH:mm:ss Z YYYY')
    // const date = momentDate.toDate()
    const date = new Date(data[i].dateTime)
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

function addSession(name, startDate, arg) {
  console.log("adding a session " + startDate)
  fetch("/addSession", {
    method: 'POST',
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      tutor: username,
      tutees: name,
      dateTime: startDate,
      duration: 5
    })
  }).then(rsp => {
    if (rsp.ok) {
      calendar.addEvent({
        title: name,
        start: arg.start,
        end: arg.end,
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
      dateTime: startDate,
      duration: 5
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
  // .then(rsp => rsp.json())
  // .then(data => {
  //   sessions = data
  //   updateCalendarWithEvents(data)
  // })
  // .catch(err => console.log(err))

}

document.addEventListener('DOMContentLoaded', function () {
  console.log("after get sessions")
  var calendarEl = document.getElementById('calendar');
  calendar = new FullCalendar.Calendar(calendarEl, {
    headerToolbar: {
      left: "prev,next today",
      center: "title",
      initialView: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek'
    },
    navLinks: true, // can click day/week names to navigate views
    editable: true,
    selectable: true,
    selectMirror: true,
    businessHours: true,
    select: function (arg) {
      const formattedTime = arg.start.toString().slice(0, 24)
      const title = prompt('Event Title:');
      if (title) {
        addSession(title, formattedTime, arg)
      }
      calendar.unselect()
    }
  });
  calendar.render();
  getSessions()
});


