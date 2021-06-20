var vm = new Vue({
  el: '#main',
  data: {
    user: {},
    tasks: {},

  },
  methods: {
    getTodayString: function () {
      const today = new Date();
      const dd = String(today.getDate()).padStart(2, '0');
      const mm = String(today.getMonth() + 1).padStart(2, '0');
      const yyyy = today.getFullYear();
      return yyyy + "-" + mm + "-" + dd + "T00:00";
    },
    getTomorrowString: function () {
      const today = new Date();
      today.setDate(today.getDate() + 1);
      const tomorrow = today;
      const dd = String(tomorrow.getDate()).padStart(2, '0');
      const mm = String(tomorrow.getMonth() + 1).padStart(2, '0');
      const yyyy = tomorrow.getFullYear();
      return yyyy + "-" + mm + "-" + dd + "T00:00";
    },
    formatTime: function (time) {
      return new Date(time).toLocaleString();
    },
    addTask: function (formId) {
      var data = new URLSearchParams(
          new FormData(document.getElementById(formId)));
      fetch('/addtask', {
        method: 'POST',
        body: data
      })
      .then(response => response.json())
      .then(rsp => {
        console.log(rsp);
        refreshTasks();
      })
      .catch(function () {
        console.log("error");
        // TODO
      });
    },
    deleteTask: function (formId) {
      var data = new URLSearchParams(
          new FormData(document.getElementById(formId)));
      fetch('/deletetask', {
        method: 'POST',
        body: data
      })
      .then(response => response.json())
      .then(rsp => {
        console.log(rsp);
        refreshTasks();
      })
      .catch(function () {
        console.log("error");
        // TODO
      });
    },
    doneTask: function (formId) {
      var data = new URLSearchParams(
          new FormData(document.getElementById(formId)));
      fetch('/donetask', {
        method: 'POST',
        body: data
      })
      .then(response => response.json())
      .then(rsp => {
        console.log(rsp);
        refreshTasks();
      })
      .catch(function () {
        console.log("error");
        // TODO
      });
    },
    addTutee: function (formId) {
      var data = new URLSearchParams(
          new FormData(document.getElementById(formId)));
      fetch('/addtutee', {
        method: 'POST',
        body: data
      })
      .then(response => response.json())
      .then(rsp => {
        console.log(rsp);
        refreshTasks();
      })
      .catch(function () {
        console.log("error");
        // TODO
      });
    },
    removeMyTutee: function (formId) {
      var data = new URLSearchParams(
          new FormData(document.getElementById(formId)));
      fetch('/removemytutee', {
        method: 'POST',
        body: data
      })
      .then(response => response.json())
      .then(rsp => {
        console.log(rsp);
        refreshTasks();
      })
      .catch(function () {
        console.log("error");
        // TODO
      });
    }
  },
});

function refreshTasks() {
  switch (getCookie("user_type")) {
    case "tutor":
      fetch('/tutortasks')
      .then(response => response.json())
      .then(tuteeMap => {
        Vue.set(vm.user, "userType", "tutor");
        Vue.set(vm.tasks, "tuteeMap", tuteeMap);
      })
      .catch(function () {
        console.log("error");
        // TODO
      });
      break;
    case "tutee":
      fetch('/tuteetasks')
      .then(response => response.json())
      .then(list => {
        Vue.set(vm.user, "userType", "tutee");
        Vue.set(vm.tasks, "list", list);
      })
      .catch(function () {
        console.log("error");
        // TODO
      });
      break;
    default:
  }
}

function refreshTitle() {
  fetch('/userinfo')
  .then(response => response.json())
  .then(person => {
    Vue.set(vm.user, "name", person.name);
  })
  .catch(function () {
    Vue.set(vm.user, "name", getCookie("user_type"));
  });
}

function getCookie(cname) {
  var name = cname + "=";
  var decodedCookie = decodeURIComponent(document.cookie);
  var ca = decodedCookie.split(';');
  for (var i = 0; i < ca.length; i++) {
    var c = ca[i];
    while (c.charAt(0) == ' ') {
      c = c.substring(1);
    }
    if (c.indexOf(name) == 0) {
      return c.substring(name.length, c.length);
    }
  }
  return "";
}

window.onload = function () {
  const id = getCookie("user_id");
  if (id === "") {
    document.getElementById('loginModel').style.display = 'block'
  }
};

refreshTitle();
refreshTasks();
setInterval(refreshTasks, 20000);
