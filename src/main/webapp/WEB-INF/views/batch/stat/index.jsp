<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/tags.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Batch Statistic</title>
<link type="text/css" rel="stylesheet" href="${ctx}/static/css/batch.css">
<script type="text/javascript" src="${ctx}/static/js/jquery.js"></script>
</head>
<body>

<div class="query_container">
<form action="${ctx}/batch/stat/index.htm">
	<dl>
		<dt>Source:</dt>
		<dd><select name="source" >
			<c:forEach items="${sources}" var="item">
				<option value="${item.value}">${item.desc}</option>
			</c:forEach>
		</select> 
		</dd>
	</dl>
	<dl>
	<dt>State:</dt>
	<dd><select name="state">
		<option value="">--ALL--</option>
	</select>
	</dd>
	</dl>
	<dl>
	<dt>FileName:</dt>
	<dd><input name="filename"></dd>
	</dl>
	
	<dl>
	<dt>sort:</dts>
	<dd><input name="sort" value="priority"></dd>
	</dl>
	<dl>
	<dt>sortType:</dt>
	<dd><input name="dir" value="desc"></dd>
	</dl>
	<button type="submit">查询</button>
</form>
</div>
<div class="list_container">
	<table>
	<tr>
	<th>SampleId</th>
	<th>Path</th>
	<th>State</th>
	<th></th>
	</tr>
	<c:forEach var="obj" items="${data}">
	<tr>
	<td>${obj.sampleId}</td>
	<td>${obj.path}</td>
	<td>${obj.state}</td>
	<td></td>
	</tr>
	</c:forEach>
	</table>
</div>
</body>
</html>