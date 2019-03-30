// date.js
function formatDate(date, fmt) {
  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length));
  }
  let o = {
    'M+': date.getMonth() + 1,
    'd+': date.getDate(),
    'h+': date.getHours(),
    'm+': date.getMinutes(),
    's+': date.getSeconds()
  };
  for (let k in o) {
    if (new RegExp(`(${k})`).test(fmt)) {
      let str = o[k] + '';
      fmt = fmt.replace(RegExp.$1, (RegExp.$1.length === 1) ? str : padLeftZero(str));
    }
  }
  return fmt;
}

function padLeftZero(str) {
  return ('00' + str).substr(str.length);
}

function str2Date(dateStr, separator) {
  if (!separator) {
    separator = "-";
  }
  let dateArr = dateStr.split(separator);
  let year = parseInt(dateArr[0]);
  let month;
  //处理月份为04这样的情况
  if (dateArr[1].indexOf("0") == 0) {
    month = parseInt(dateArr[1].substring(1));
  } else {
    month = parseInt(dateArr[1]);
  }
  let day = parseInt(dateArr[2]);
  let date = new Date(year, month - 1, day);
  return date;
}
// 遍历日期
function getDate(datestr){
  var temp = datestr.split("-");
  var date = new Date(temp[0],temp[1],temp[2]);
  return date;
}

// 注意时间格式 yyyy-MM-dd 没有时分秒
function eachTime(start,end){
	//alert(start+ "-" + end);
	// 数组的下标
	var i=0;
	// 将切分的每一天的日期，存放到数组中
	var timeList = [];
	
	// 得到时间戳
	var startTime = getDate(start);
	var endTime = getDate(end);
	
	while((endTime.getTime()-startTime.getTime())>=0){
	  var year = startTime.getFullYear();
	  var month = startTime.getMonth().toString().length==1?"0"+startTime.getMonth().toString():startTime.getMonth();
	  var day = startTime.getDate().toString().length==1?"0"+startTime.getDate():startTime.getDate();
	  
	  // alert(year+"-"+month+"-"+day);
	  timeList[i++] = year+"-"+month+"-"+day;
	  startTime.setDate(startTime.getDate()+1);
	}	
	
	return timeList;
}

