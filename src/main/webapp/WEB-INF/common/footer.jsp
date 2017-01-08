<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/tags.jsp"%>

<div id="footer">
	<a href="http://www.emory.edu/home/about-this-site/copyright.html" title="Copyright">Copyright</a> © 2014			
	<a href="http://www.emory.edu/home/index.html" title="OMICSEQ, Inc." target="_blank"><em>Emory University</em></a> - All Rights Reserved | 1518 Clifton Road. Atlanta, Georgia 30322. USA   
	<!-- <span style="padding-left: 100px;">Accredited Testing Organizations: <img style="width: 60px;height: 60px;" alt="Michigan University" title="Michigan University" src="${ctx}/static/images/michigan.jpg"> <img style="width: 60px;height: 60px;" title="Emory University" alt="Emory University" src="${ctx}/static/images/emory.jpg"></span> -->      
</div>
<div id="toolBar">
<!--	<c:if test="${not empty user}">
		<div class="historybar">
			<div class="div1"><img src="${ctx}/static/images/historyicon.png" /><fmt:message key="label.searchhistory"/></div>
			<div class="div2 expand" id="gethistory"></div>
			<div class="div3-pop">
				<div>每页显示记录数</div>
				<ul>
					<li ><span class="<c:if test="${pageSize == 10 }">active</c:if> imagespan"></span><span class="pageSize">10</span></li>
					<li ><span class="<c:if test="${pageSize == 15 }">active</c:if> imagespan"></span><span class="pageSize">15</span></li>
					<li ><span class="<c:if test="${pageSize == 20 }">active</c:if> imagespan"></span><span class="pageSize">20</span></li>
					<li ><span class="<c:if test="${pageSize == 50 }">active</c:if> imagespan"></span><span class="pageSize">50</span></li>
				</ul>
			</div>
			<div class="div3" id="pageSetting"></div>
		</div>
		<div id="history">
			<div class="h_top">
				<input type="text" id="searchkey"/><a class="searchhistory" onclick="searchHistory();">GO</a>
			</div>
			
			<div class="h_content">
				
			</div>
			<div id="historyLoading"></div>
		</div>
	</c:if> -->
</div>
