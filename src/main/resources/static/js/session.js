let username; // Name of current user

username = getCookie("user_id")

function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop().split(';').shift();
  }
}

function addSession(name, startDate, endDate) {
  console.log(
      "inside addsess on with-" + name + "-" + startDate + "-" + endDate)
  console.log(new Date(startDate).toLocaleString())

  console.log(new Date(startDate).toLocaleDateString())
  console.log(new Date(startDate).toUTCString())

  console.log("array " + JSON.stringify([name]))
  console.log("message " + JSON.stringify({
    tutor: username,
    tutees: name,
    // tutees: JSON.stringify([name]),
    dateTime: startDate,
    duration: 5
  }))

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
  }).then(rsp => console.log(rsp))
}

function removeSession(name, startDate, endDate) {
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
  }).then(rsp => console.log(rsp))
}

document.addEventListener('DOMContentLoaded', function () {
  var calendarEl = document.getElementById('calendar');
  var calendar = new FullCalendar.Calendar(calendarEl, {
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
        calendar.addEvent({
          title: title,
          start: arg.start,
          end: arg.end,
          allDay: false
        })
      }
      console.log("Clicked on event")
      addSession(title, formattedTime, arg.end)
      calendar.unselect()
    },
    eventClick: function (arg) {
      if (confirm('Are you sure you want to delete this event?')) {
        const formattedTime = arg.event.start.toString().slice(0, 24)
        removeSession(arg.event.title, formattedTime, arg.event.end)
        arg.event.remove()
      }
    },
  });
  calendar.render();
});


