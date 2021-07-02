function getTaskId() {
  var pathParamList = window.location.pathname.split('/');
  var lastParam = pathParamList[pathParamList.length - 1];
  return parseInt(lastParam);
}

var taskId = getTaskId();

var vm = new Vue({
  el: '#main',
  data: {
    taskId: taskId,
    taskInfo: {}
  },
  methods: {
    formatTime: function (time) {
      return new Date(time).toLocaleString();
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
        location.href = "/homework";
      })
      .catch(function () {
        console.log("error");
        // TODO
      });
    },
  },
});

function refreshTask() {
  fetch('/taskinfo?task_id=' + taskId)
  .then(response => response.json())
  .then(task => {
    Vue.set(vm.taskInfo, "task", task);
  })
  .catch(function () {
    console.log("error");
    // TODO
  });
}

function refreshTaskFiles() {
  fetch('/taskfiles?task_id=' + taskId)
  .then(response => response.json())
  .then(files => {
    Vue.set(vm.taskInfo, "files", files);
  })
  .catch(function () {
    console.log("error");
    // TODO
  });
}

console.log(getTaskId());
refreshTask();
setInterval(refreshTask, 5000);
refreshTaskFiles();
setInterval(refreshTaskFiles, 5000);