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
		<style type="text/css">
			.signin_left2 {
			    background-image: url("${ctx}/static/images/bj_1.png");
			    margin: 30px auto auto;
			    float: none;
			    position:relative;
			}
			.signin_left3 {
				position:absolute;
				top:0px;
			}
			.signin_left4 { 
				margin-left: 60px;
	    		position: absolute;
			}
			.signin_left5 {
				margin-top:50px;
			}
			#userName, #password {
				height:50px;
			}
			#loginBtn {
			    background: none repeat scroll 0 0 #2A2569;
			    border-radius: 5px;
			    box-shadow: none;
			    color: #FFFFFF;
			    display: block;
			    font-size: 24px;
			    height: 50px;
			    line-height: 50px;
			    margin: 0;
			    padding: 0;
			    text-align: center;
			    text-decoration: none;
			    width: 340px;
			}
			#errorEl {
				margin-top: -30px;
			}
		</style>
		<script type="text/javascript">
			var rootPath = '';
			$(document).ready(function(){
				rootPath = '${ctx}';
				
				$('#loginBtn').click(function(){
					var box = $('#loginForm');
					var userName = $('#userName', box).val();
					var password = $('#password', box).val();
					var errorEl = $('#errorEl');
					if (validateRequired(userName, $('#userName', box).attr('errorMessage'), errorEl) == false) {
						$('#userName').focus();
						return;
					}
					if (validateRequired(password, $('#password', box).attr('errorMessage'), errorEl) == false) {
						$('#password').focus();
						return;
					}
					var isRemember = $('.rememberSpan').hasClass('selectedSpan') ? 'true' : 'false';
					var url = rootPath + '/user/login.json';
					var params = {"userName":userName, "password":password, "isRemember":isRemember};
					var callBack = function(result){
						if(result.result) {
							var href = window.location.href;
							var urlArray = href.split("?");
							var url = "";
							if(urlArray.length > 1) {
								var temp = urlArray[1].split("=");
								url = temp.length > 1 ? temp[1] : "";
							}
							if(url == "") {
								return;
							}else{
								if(userName == 'huada'){
									window.location.href = rootPath + '/result_single.htm';
								}else{
									window.location.href = rootPath + '/welcome.htm';
								}
							}
						}else{
							errorEl.html(result.message);
							errorEl.show();
						}
					};
					sendRequest(url, params, "json", callBack, "");
				});
			});
		</script>
	</head>
	<body>
	<div class="navbar navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container">
			</div>
		</div>
	</div>
	<div class="wrapper">
		<div class="logo"><img src="${ctx}/static/images/banner.png" width="419" height="106" /></div>
		<div class="signin">
			<form class="form-signin" role="form" id="loginForm">
				<div class="signin_left6" id="errorEl"></div>
				<div class="signin_left2">
					<div class="signin_left3">
						<img src="${ctx}/static/images/icon_1.png" width="40" height="50" />
					</div>
					<div class="signin_left4">
			  			<input name="userName" type="text" class="signin_inpnt" id="userName" placeholder="<fmt:message key="placeholder.username"/>" 
			  				errorMessage="<fmt:message key="error.username.null"/>" formatError="<fmt:message key="error.username.formatwrong"/>" value="" />
					</div>
				</div>
				<div class="signin_left2">
					<div class="signin_left3"><img src="${ctx}/static/images/icon_2.png" width="40" height="50" /></div>
					<div class="signin_left4">
						  <input name="password" type="password" class="signin_inpnt" id="password" placeholder="<fmt:message key="placeholder.password"/>" 
						  	errorMessage="<fmt:message key="error.password.null"/>"  formatError="<fmt:message key="error.password.formatwrong"/>" value="" />
					</div>
				</div>
				<div class="signin_left5">
					<a href="javascript:;" id="loginBtn" faildMessage="<fmt:message key="failed.register"/>"><fmt:message key="button.login"/></a>
				</div>
			</form>
		</div>
	</div>
</div>
<div id="backgroundDiv"></div>
</body>
</html>