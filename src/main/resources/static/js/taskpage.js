var vm = new Vue({
  el: '#wrapper',
  data: {
    user: {},
    tasks: {},
  },
  methods: {
    formatTime: function (time) {
      return new Date(time).toLocaleString();
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
        .catch(function() {
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
        .catch(function() {
          console.log("error");
          // TODO
        });
      break;
    default:
  }
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

refreshTasks();
setInterval(refreshTasks, 5000);
