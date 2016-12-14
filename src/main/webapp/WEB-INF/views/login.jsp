<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/tags.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title>Login</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet" href="${ctx}/static/bootstrap/css/bootstrap.css">
<link type="text/css" rel="stylesheet" href="${ctx}/static/css/login.css">
<script type="text/javascript" src="${ctx}/static/js/jquery.js"></script>
<script type="text/javascript" src="${ctx}/static/bootstrap/js/bootstrap.js"></script>
<script type="text/javascript" src="${ctx}/static/js/validation.js"></script>
<script type="text/javascript" src="${ctx}/static/script/common.js"></script>
<script type="text/javascript">
	function validateForm(thisform) {
		var $form = $(thisform);
		var userName = $('#userName', $form).val();
		var password = $('#password', $form).val();
		if (validateRequired(userName, "userName must be filled out!") == false) {
			$('#userName').focus();
			return false;
		}
		if (validateRequired(password, "password must be filled out!") == false) {
			$('#password').focus();
			return false;
		}
		return true;
	}

	$(document).ready(function() {
		$(this).keydown(function(event) {
			if (event.keyCode == 13) {
				$('#loginBtn').trigger('click');
			}
		});
		$('#loginBtn').click(function(){
			var $form = $("#loginForm");
			if (validateForm($form)) {
				var userName = $('#userName', $form).val();
				var password = $('#password', $form).val();
				login(userName, password);
			}
		});
	});
	
	
</script>
</head>
<body>
	<%@ include file="/WEB-INF/common/header.jsp"%>
	
	<div class="container">
      <form class="form-signin" role="form" id="loginForm">
        <h2 class="form-signin-heading">Please sign in</h2>
        <input class="form-control" placeholder="user name" name="userName" autofocus="true" id="userName"
					autocomplete="off" type="text">
        <input class="form-control" placeholder="Password" name="password" type="password" id="password">
        <label class="checkbox">
           <input value="remember-me" type="checkbox"> <fmt:message key="button.rememberme"/> 
           <a href="${ctx}/user/toRegister.htm" class="regist"><fmt:message key="button.register"/> </a>
        </label>
        <label id="loginError" class="redColor"></label>
        <button class="btn btn-lg btn-primary btn-block" type="button" id="loginBtn"><fmt:message key="button.login"/></button>
      </form>
      
    </div> 
</body>
</html>