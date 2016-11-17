function validateRequired(value, alerttxt, errorEl){
  if (value==null || value=="") {
	  errorEl.html(alerttxt);
  	  return false;
  }else {
	  return true;
  }
}

function validateMatch(field1, field2, alerttxt, errorEl){
  if (field1 != field2) {
	  errorEl.html(alerttxt);
  	  return false;
  }else {
	  return true;
  }
}

function validateEmail(field, alerttxt, errorEl) {
	var reg = /^([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/;
	if(reg.test(field)) {
		return true;
	}else{
		errorEl.html(alerttxt);
		return false;
	}
}