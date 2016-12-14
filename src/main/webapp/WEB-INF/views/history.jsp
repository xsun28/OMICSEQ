<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/tags.jsp"%>
<table id="historytable">
	<c:if test="${histories == null || histories[0] == null}">
		<tr><td colspan="3"><fmt:message key="failed.nosearchdata"/></td></tr>
	</c:if>
	<c:if test="${histories != null || histories[0] != null}">
		<c:forEach items="${histories }" var="history">
			<tr>
				<td class="searchword"  width="100px"><a onclick="searchByHistory(this);">${history.keyword }</a></td>
				<td  width="100px"><fmt:formatDate value="${history.createDate }" pattern="yyyy/MM/dd"/></td>
				<td><a class="resultbutton" userId="${history.userId }"></a></td>
			</tr>
		</c:forEach>
	</c:if>
</table>