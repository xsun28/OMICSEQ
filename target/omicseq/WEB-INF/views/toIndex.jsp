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
		<script type="text/javascript" src="${ctx}/static/script/common.js"></script>
		<script type="text/javascript" src="${ctx}/static/script/register.js"></script>
		<style type="text/css">
			.signin_left2 {
			    background-image: url("${ctx}/static/images/bj_1.png");
			    margin: 30px auto auto;
			    float: none;
			    position:relative;
			}
			.signin_left6 {
			    margin: 20px 40px auto;
			    float: none;
			    position:relative;
			}
			.signin_left7 {
			    margin: 80px 0px auto;
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
				margin-top:20px;
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
				margin-bottom: -20px;
			}
		</style>
		<script type="text/javascript">
			var rootPath = '';
			$(document).ready(function(){
				rootPath = '${ctx}';
				$('#loginBtn').click(function(e){
					var box = $('#loginForm');
					var userName = $('#userName', box).val();
					var password = $('#password', box).val();
					var errorEl = $('#errorEl');
					var isRemember = $('.rememberSpan').hasClass('selectedSpan') ? 'true' : 'false';
					if(userName == '') {
						var errorMessage = $('#userName').attr('errorMessage');
						errorEl.html(errorMessage);
						return;
					}
					if(password == '') {
						var errorMessage = $('#password').attr('errorMessage');
						errorEl.html(errorMessage);
						return;
					}
					var url = rootPath + '/user/login.json';
					var callBack = function(result){
						if(result.result) {
							if(userName == 'huada'){
								window.location.href = rootPath + '/result_single.htm';
							}else{
								window.location.href = rootPath + '/welcome.htm';
							}
							
						}
					};
					
					var checkUserUrl = rootPath + '/user/checkStatus.json';
					
					var checkBack = function(result){
						if(result.result) {
							// 模态窗口高度和宽度   
							 var whparamObj = { width: 700, height: 600 };
							// 相对于浏览器的居中位置   
							 var bleft = ($(window).width() - whparamObj.width) / 2;   
							 var btop = ($(window).height() - whparamObj.height) / 2;   
							        
							 // 根据鼠标点击位置算出绝对位置   
							 var tleft = e.screenX - e.clientX;   
							 var ttop = e.screenY - e.clientY;   
							        
							 // 最终模态窗口的位置   
							 var left = bleft + tleft;   
							 var top = btop + ttop;   
							        
							 // 参数   
							 var p = "help:no;status:no;center:yes;";   
							     p += 'dialogWidth:'+(whparamObj.width)+'px;';   
							     p += 'dialogHeight:'+(whparamObj.height)+'px;';   
							     p += 'dialogLeft:' + left + 'px;';   
							     p += 'dialogTop:' + top + 'px;';
							     
							var returnvalue;    
					     	if(navigator.userAgent.indexOf("Chrome") >0 ){
				    	 		var winOption = "height="+whparamObj.height+"px,width="+whparamObj.width+"px,top="+top+"px,left="+left+"px,toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,fullscreen=0";
				    	 		returnvalue = window.open(rootPath + '/static/html/agree.html','newwindow', winOption);
					    	} else {
					    		returnvalue=window.showModalDialog(rootPath + '/static/html/agree.html','newwindow', p);
					    	}
							
							if(returnvalue != undefined && returnvalue != ",")
							{
								var params = {"userName":userName, "password":password, "isRemember":isRemember, "idCardAndPhone":returnvalue};
								sendRequest(url, params, "json", callBack, "");
							}
						} else {
							var params = {"userName":userName, "password":password, "isRemember":isRemember};
							sendRequest(url, params, "json", callBack, "");
						}
					};
					
					var params = {"userName":userName, "password":password, "isRemember":isRemember};
					sendRequest(checkUserUrl, params, "json", checkBack, "");
					
				});
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
			</div>
		</div> 
	</div>
	<div class="wrapper">
		<div class="logo"><img src="${ctx}/static/images/banner.png" width="419" height="106" /></div>
		<div class="signin">
			<form class="form-signin" role="form" id="loginForm">
				<!-- <div class="signin_left6" style="float: none;">Beta Edition, please input your password. <a href="${ctx}/testpages/welcome.htm">Guest login in here. </a>
 For full access, please contact steve qin by zhaohui.qin@emory.edu.
				</div> -->
				<div class="signin_left6" style="float: none;">Beta Edition. For registered users, please login. For others, Plesae click the<a href="${ctx}/testpages/welcome.htm"> demo page </a>for more information.  
 <br>An introductory video can be <a href="http://youtu.be/z6c7Akq5ILM" target="_blank">found here</a>. For full access, please contact steve qin by <a href="mailto:zhaohui.qin@emory.edu">zhaohui.qin@emory.edu</a>.
				</div>
				<!-- http://v.youku.com/v_show/id_XNzkwOTUyNTYw.html -->
				<div class="signin_left6" id="errorEl" style="margin-top: 25px;"></div>
				<div class="signin_left2">
					<div class="signin_left3">
						<img src="${ctx}/static/images/icon_1.png" width="40" height="50" />
					</div>
					<div class="signin_left4">
			  			<input name="userName" type="text" class="signin_inpnt" id="userName" placeholder="<fmt:message key="placeholder.username"/>" 
			  				errorMessage="<fmt:message key="error.username.null"/>" formatError="<fmt:message key="error.login.userwrong"/>" value="" />
					</div>
				</div>
				<div class="signin_left2">
					<div class="signin_left3"><img src="${ctx}/static/images/icon_2.png" width="40" height="50" /></div>
					<div class="signin_left4">
						  <input name="password" type="password" class="signin_inpnt" id="password" placeholder="<fmt:message key="placeholder.password"/>" 
						  	errorMessage="<fmt:message key="error.password.null"/>"  formatError="<fmt:message key="error.login.userwrong"/>" value="" />
					</div>
				</div>
				<!-- 添加记住用户 -->
				<div class="signin_left6">
					<div class="signin_left4">
					<span class="deselectedSpan rememberSpan"></span>
	        		<label class="remembermeLabel"><fmt:message key="button.rememberme"/></label>
	        		</div>
				</div>
				<div class="signin_left5">
					<a href="javascript:;" id="loginBtn" faildMessage="<fmt:message key="failed.register"/>"><fmt:message key="button.confirm"/></a>
				</div>
			</form>
		</div>
	</div>
</div>
<div id="backgroundDiv"></div>
</body>
</html>s