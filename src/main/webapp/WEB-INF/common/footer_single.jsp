<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/tags.jsp"%>

<div id="toolBar">
	<c:if test="${not empty user}">
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
	</c:if>
</div>