var vm = new Vue({
  el: '#main',
  data: {
    taskId: getTaskId(),
  },
  methods: {

  },
});

function getTaskId() {
  var pathParamList = window.location.pathname.split('/');
  var lastParam = pathParamList[pathParamList.length - 1];
  return parseInt(lastParam);
}

console.log(getTaskId());
