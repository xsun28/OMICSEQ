<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/tags.jsp"%>
<!DOCTYPE html>
	<html>
	<head>
		<title>Omics Search Engine </title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<link type="text/css" rel="stylesheet" href="${ctx}/static/bootstrap/css/bootstrap.min.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/custom.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/jquery-ui.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/reset.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/register.css">
		<script type="text/javascript" src="${ctx}/static/js/jquery.js"></script>
		<script type="text/javascript" src="${ctx}/static/bootstrap/js/bootstrap.js"></script>
		<script type="text/javascript" src="${ctx}/static/bootstrap/js/bootstrap-multiselect.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/jquery-ui.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/validation.js"></script>
		<script type="text/javascript" src="${ctx}/static/script/common.js"></script>
		<script type="text/javascript" src="${ctx}/static/script/register.js"></script>
		<script type="text/javascript">
			var rootPath = '';
			$(document).ready(function(){
				rootPath = '${ctx}';
			});
		</script>
		<script type="text/javascript">
			var _bdhmProtocol = (("https:" == document.location.protocol) ? " https://" : " http://");
			document.write(unescape("%3Cscript src='" + _bdhmProtocol + "hm.baidu.com/h.js%3Fa06bf362a4238726a57e308ed1075652' type='text/javascript'%3E%3C/script%3E"));
		</script>
	</head>
	<body>
	<div class="navbar navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">
				<form class="form-signin" role="form" id="loginForm" style="margin:0">
					  <div class="btn-group navbar-right">
					  	<label class="registerNav"><fmt:message key="label.alreadyregister"/></label>
					    <button type="button" class="btn btn-default" id="loginBtn2" onclick="showLoginDialog();" >
					      <fmt:message key="button.login"/>
					    </button>
					    <ul class="dropdown-menu loginDialog dialog" style="right: -50px;">
					      <li>
					      	<div class="loginGroup">
							  <span class="usernameImage"></span>
							  <input class="usernameInput" placeholder="<fmt:message key="placeholder.username"/>" name="userName" autofocus="true" id="userName"
								autocomplete="off" type="text" errorMessage="<fmt:message key="error.username.null"/>" value="${cookiename}"> 
							</div>
						  </li>
						  <li>
						  	<div class="loginGroup">
						  		<span class="passwordImage"></span>
						  	   <input class="usernameInput" placeholder="<fmt:message key="placeholder.password"/>" name="password" type="password" 
						  	   		id="password" errorMessage="<fmt:message key="error.password.null"/>" value="${cookiepassword}">
						  	</div>
	        			  </li>
	        			  <li class="loginLi">
	        			  	<span class="<c:if test="${isRemember == 'true'}">selectedSpan</c:if><c:if test="${isRemember == 'false'}">deselectedSpan</c:if> rememberSpan"></span>
	        			  	<label class="remembermeLabel"><fmt:message key="button.rememberme"/></label>
	        			  	<a href="${ctx}/user/toRegister.htm" id="registerLink"><fmt:message key="button.register"/></a>
	        			  </li>
	        			  <li id="errorEl"></li>
	        			  <li>
	        			  	<div type="button" id="login"><fmt:message key="button.loginbtn"/></div>
	        			  </li>
					    </ul>
					  </div>
				 </form>
			</div>
		</div>
	</div>
	<div class="wrapper">
		<div class="logo"><img src="${ctx}/static/images/banner.png" width="419" height="106" /></div>
		<div class="signin">
			<form class="form-signin" role="form" id="registerForm">
				<div class="signin_left6" id="registerError"></div>
				<div class="signin_left2">
					<div class="signin_left3">
						<img src="${ctx}/static/images/icon_1.png" width="40" height="50" />
					</div>
					<div class="signin_left4">
			  			<input name="userName" type="text" class="signin_inpnt" id="register_userName" placeholder="<fmt:message key="placeholder.username"/>" 
			  				errorMessage="<fmt:message key="error.username.null"/>" formatError="<fmt:message key="error.username.formatwrong"/>" value="" />
					</div>
					<div class="pwd-checklist-wrapper" id="usernameTip">
						<span class="pwd-checklist-arrow">
							<em class="arrowa">◆</em>
							<em class="arrowb">◆</em>
						</span>
						<ul class="pwd-checklist" id="pwdChecklist">
							<li class="pwd-checklist-item formatRule"><fmt:message key="label.userName.tip1"/></li>
							<li class="pwd-checklist-item nullRule"><fmt:message key="label.userName.tip2"/></li>
							<li class="pwd-checklist-item lenRule"><fmt:message key="label.userName.tip3"/></li>
						</ul>
					</div>
				</div>
				<div class="signin_left2">
					<div class="signin_left3"><img src="${ctx}/static/images/icon_4.png" width="40" height="50" /></div>
					<div class="signin_left4">
					  	<input name="company" type="text" class="signin_inpnt" id="company" placeholder="<fmt:message key="placeholder.company"/>" 
					  		errorMessage="<fmt:message key="error.company.null"/>" value="" />
					</div>
				</div>
				<div class="signin_left2">
					<div class="signin_left3"><img src="${ctx}/static/images/icon_2.png" width="40" height="50" /></div>
					<div class="signin_left4">
						  <input name="password" type="password" class="signin_inpnt" id="register_password" placeholder="<fmt:message key="placeholder.password"/>" 
						  	errorMessage="<fmt:message key="error.password.null"/>"  formatError="<fmt:message key="error.password.formatwrong"/>" value="" />
					</div>
					<div class="pwd-checklist-wrapper" id="pwdTip">
						<span class="pwd-checklist-arrow">
							<em class="arrowa">◆</em>
							<em class="arrowb">◆</em>
						</span>
						<ul class="pwd-checklist" id="pwdChecklist">
							<li class="pwd-checklist-item lenRule"><fmt:message key="label.password.tip1"/></li>
							<li class="pwd-checklist-item formatRule"><fmt:message key="label.password.tip2"/></li>
							<li class="pwd-checklist-item nullRule"><fmt:message key="label.password.tip3"/></li>
						</ul>
					</div>
				</div>
				<div class="signin_left2">
					<div class="signin_left3"><img src="${ctx}/static/images/icon_5.png" width="40" height="50" /></div>
					<div class="signin_left4">
					  	<input name="department" type="text" class="signin_inpnt" id="department" placeholder="<fmt:message key="placeholder.department"/>" value="" />
					</div>
				</div>
				<div class="signin_left2">
					<div class="signin_left3"><img src="${ctx}/static/images/icon_2.png" width="40" height="50" /></div>
					<div class="signin_left4">
						  <input name="confirmPassword" type="password" class="signin_inpnt" id="confirmPassword" placeholder="<fmt:message key="placeholder.confirmPassword"/>" 
						  	errorMessage="<fmt:message key="error.confirmpassword.null"/>" errorMacth="<fmt:message key="error.password.notmatch"/>" value="" />
					</div>
				</div>
				<div class="signin_left2">
					<div class="signin_left3"><img src="${ctx}/static/images/icon_6.png" width="40" height="50" /></div>
					<div class="signin_left4">
						  <input name="lab" type="text" class="signin_inpnt" id="lab" placeholder="<fmt:message key="placeholder.lab"/>" value="" />
					</div>
				</div>
				<div class="signin_left2">
					<div class="signin_left3"><img src="${ctx}/static/images/icon_3.png" width="40" height="50" /></div>
					<div class="signin_left4">
						  <input name="email" type="text" class="signin_inpnt" id="email" placeholder="<fmt:message key="placeholder.email"/>" 
						  	errorMessage="<fmt:message key="error.email.null"/>" formatError="<fmt:message key="error.email.formatwrong"/>" value="" />
					</div>
				</div>
				<div class="signin_left2 registerselectbg">
					<div class="signin_left3"><img src="${ctx}/static/images/icon_7.png" width="40" height="50" /></div>
					<div class="signin_left4 " style="position:relative">
						<span class="companyType" id="companyType" typeId=""><fmt:message key="placeholder.companytype"/></span>
						<ul id="companyTypeUl" class="dialog">
							<li id="1"><fmt:message key="select.universities"/></li>
							<li id="2"><fmt:message key="select.research"/></li>
							<li id="3"><fmt:message key="select.biomedical"/></li>
							<li id="4"><fmt:message key="select.government"/></li>
							<li id="5"><fmt:message key="select.others"/></li>
						</ul>
					</div>
				</div>
				<div class="signin_left5">
					<a href="javascript:;" id="regiterBtn" faildMessage="<fmt:message key="failed.register"/>"><fmt:message key="button.register"/></a>
				</div>
			</form>
		</div>
	</div>
</div>
<div id="backgroundDiv"></div>
</body>
</html>