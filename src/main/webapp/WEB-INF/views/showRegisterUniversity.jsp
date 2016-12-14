<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/tags.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>大学名单</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<link type="text/css" rel="stylesheet" href="${ctx}/static/bootstrap/css/bootstrap.min.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/custom.min.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/reset.min.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/result.min.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/jquery-ui.min.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/dialog.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/allinone.css">
</head>
<body>
	<table class="table table-striped table-bordered table-hover table-condensed">
		<c:if test="${users.size() == 0 }">
			<tr><td>目前没有使用的大学</td></tr>
		</c:if>
		<c:if test="${users.size() != 0 }">
			<tr style="font-weight: bold;">
				<td>大学</td>	
				<td>用户名</td>
				<td>是否验证</td>
				<td>验证信息</td>		
			</tr>
			<c:forEach items="${users }" var="user">
				<tr>
					<td>${user.company }</td>	
					<td>${user.name }</td>
					<td>
						<c:if test="${empty user.idCardAndPhone }">否</c:if>
						<c:if test="${!empty user.idCardAndPhone }">是</c:if>
					</td>
					<td>${user.idCardAndPhone }</td>	
				</tr>
			</c:forEach>
		</c:if>
	</table>
</body>
</html>