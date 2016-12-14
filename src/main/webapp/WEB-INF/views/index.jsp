<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/tags.jsp"%>
<!DOCTYPE html>
	<html>
	<head>
		<title>Omics Search Engine </title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<link type="text/css" rel="stylesheet" href="${ctx}/static/bootstrap/css/bootstrap.min.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/custom.min.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/reset.min.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/jquery-ui.min.css">
		<script type="text/javascript" src="${ctx}/static/js/jquery.js"></script>
		<script type="text/javascript" src="${ctx}/static/bootstrap/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="${ctx}/static/bootstrap/js/bootstrap-multiselect.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/validation.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/jquery-ui.js"></script>
		<script type="text/javascript" src="${ctx}/static/script/common.js"></script>
		<script type="text/javascript" src="${ctx}/static/script/main.js"></script>
		<script type="text/javascript">
			var rootPath = '';
			$(document).ready(function(){
				rootPath = '${ctx}';
			});
		</script>
		<style type="text/css">
			html {
				overflow:hidden;
			}
		</style>
	</head>
	<body>
	
		<%@ include file="../common/header.jsp"%>
		<div style="width:100%;height:50px;"></div>
		<div class="container-fluid">	
			<div class="jumbotron">
				<img src="${ctx}/static/images/banner.png">
				<br/>
			</div>
			
			<%@ include file="/WEB-INF/views/search.jsp"%>
			
		</div>
		<%@ include file="/WEB-INF/common/dialog.jsp"%>
		<%@ include file="../common/footer.jsp"%>
		<div id="backgroundDiv"></div>
	</body>
</html>