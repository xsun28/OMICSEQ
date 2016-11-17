<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/tags.jsp"%>
<c:set var="locale">${requestScope["org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE"]}</c:set>
<script type="text/javascript">
	app = {ctx:'${ctx}',lang:'${lang}',redirect:'${param.redirect}'};
</script>
<script type="text/javascript">
var _bdhmProtocol = (("https:" == document.location.protocol) ? " https://" : " http://");
document.write(unescape("%3Cscript src='" + _bdhmProtocol + "hm.baidu.com/h.js%3Fa06bf362a4238726a57e308ed1075652' type='text/javascript'%3E%3C/script%3E"));
</script>

<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container">
			<button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</button>
			<div class="navbar-right languageDiv" style="margin-top: 5px;position:relative">
				<c:if test="${locale == 'zh_CN'}">
					<div class="dropdown-toggle" onClick="showLanguageDialog();">
						<img src="${ctx}/static/images/chinese.png"/><span class="languageName">中文</span><span class="caret"></span>
					</div>
				</c:if>
				<c:if test="${locale == 'en' or locale == 'en_US'}">
					<div class="dropdown-toggle" onClick="showLanguageDialog();">
						<img src="${ctx}/static/images/english.png"/><span class="languageName">English</span><span class="caret"></span>
					</div>
				</c:if>
			    <ul class="dropdown-menu dialog languageDialog">
			      <li onclick="changeLanguage(false);">
					  <img src="${ctx}/static/images/english.png"/>
					  English
				  </li>
				  <li onclick="changeLanguage(true);">
				  	<img src="${ctx}/static/images/chinese.png"/>
					  中文
       			  </li>
			    </ul>
			  </div>
			<c:if test="${empty user}">
		        <form class="form-signin" role="form" id="loginForm" style="margin:0">
					  <div class="btn-group navbar-right">
					    <button type="button" class="btn btn-default" id="loginBtn" onclick="showLoginDialog();" >
					      <fmt:message key="button.login"/>
					    </button>
					    <button type="button" class="btn btn-default" id="registerBtn" onclick="showRegister();">
					      <fmt:message key="button.register"/>
					    </button>
					    <ul class="dropdown-menu loginDialog dialog">
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
						  	   <input class="usernameInput" errorMessage="<fmt:message key="error.password.null"/>" placeholder="<fmt:message key="placeholder.password"/>" name="password" value="${cookiepassword }" type="password" id="password">
						  	</div>
	        			  </li>
	        			  <li class="loginLi">
	        			  	<span class='<c:if test="${isRemember == 'true'}">selectedSpan</c:if><c:if test="${isRemember == 'false'}">deselectedSpan</c:if> rememberSpan'></span>
	        			  	<label class="remembermeLabel"><fmt:message key="button.rememberme"/></label>
	        			  	<span onclick="showRegister();" id="registerLink"><fmt:message key="button.register"/></span>
	        			  </li>
	        			  <li id="errorEl"></li>
	        			  <li>
	        			  	<div type="button" id="login"><fmt:message key="button.loginbtn"/></div>
	        			  </li>
					    </ul>
					  </div>
				 </form>
		    </c:if>
			<c:if test="${not empty user}">
				
			</c:if>
			<a class="brand" href="${ctx}/welcome.htm"><img id="search_logo" src="${ctx}/static/images/search_logo.png"></a>
			
			<div class="nav-collapse collapse">
				<ul class="nav">
					<li><a href="${ctx}/welcome.htm"><fmt:message key="menu.home"/></a></li>
					<li><a href="${ctx}/about.htm"><fmt:message key="menu.about"/></a></li>
					<li><a href="mailto:zhaohui.qin@emory.edu"><fmt:message key="menu.contact"/></a></li>
					<li><a href="${ctx}/news.htm"><fmt:message key="menu.news"/></a></li>
					<!-- <li><a href="http://112.25.20.155:8080/jForum">转到论坛</a></li> -->
				</ul>
			</div>
			<!-- <a href="/jForumTest">转到论坛</a>
			<a href="/GenoWiki">转到wiki</a> -->
		</div>
	</div>
	
	<div style="position: fixed; left:20px; top:45px;z-index:99;" xmlns="joy" id="joy-toobar" class="current_historybar">
		 <div class="div2 shrink" id="getCurrenthistory">
		 	<div class="div1"><img src="${ctx}/static/images/historyicon.png" /><fmt:message key="label.recentsearch"/></div>
		 </div>
	</div>
	<div id="current_history">
		<div class="c_h_content">
				
		</div>
	</div>	
</div>