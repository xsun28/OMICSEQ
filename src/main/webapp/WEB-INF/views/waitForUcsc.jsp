<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/tags.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title>wait for a monment</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet" href="${ctx}/static/bootstrap/css/bootstrap.min.css">
<link type="text/css" rel="stylesheet" href="${ctx}/static/css/custom.min.css">
<link type="text/css" rel="stylesheet" href="${ctx}/static/css/reset.min.css">
<link type="text/css" rel="stylesheet" href="${ctx}/static/css/result.min.css">
<link type="text/css" rel="stylesheet" href="${ctx}/static/css/jquery-ui.min.css">
<link type="text/css" rel="stylesheet" href="${ctx}/static/css/dialog.css">
<link type="text/css" rel="stylesheet" href="${ctx}/static/css/allinone.css">
<script type="text/javascript" src="${ctx}/static/js/jquery.js"></script>
<script type="text/javascript" src="${ctx}/static/bootstrap/js/bootstrap.min.js"></script>
<script type="text/javascript" src="${ctx}/static/bootstrap/js/bootstrap-multiselect.js"></script>
<script type="text/javascript" src="${ctx}/static/js/validation.js"></script>
<script type="text/javascript" src="${ctx}/static/js/jquery-ui.js"></script>
<script type="text/javascript" src="${ctx}/static/script/common.js"></script>
<script type="text/javascript" src="${ctx}/static/script/main.js"></script>
<script type="text/javascript" src="${ctx}/static/script/dialog.js"></script>
<script type="text/javascript">
	$(document).ready(function(){
		var ucscUrl = '${ucscUrl }';
		//alert(ucscUrl);
		window.location.href = ucscUrl;

	});
</script>
</head>

<body>
<div style="width：90px;height:90px;position: absolute;left: 50%;top:50%;margin-left: -45px;margin-top: -45px;">
	<img src="${ctx }/static/images/loading3.gif"/><br><strong><fmt:message key="label.waitForUcsc"/></strong>
	</div>
</body>
</html>