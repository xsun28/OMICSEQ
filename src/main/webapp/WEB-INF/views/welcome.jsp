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
			var rootPath = '';
			$(document).ready(function(){
				rootPath = '${ctx}';
			});
		</script>
	</head>
	<body>
    <div id="wrap">
      <!-- Fixed navbar -->
      <%@ include file="../common/header.jsp"%>	
      <!-- Begin page content -->
      <div class="container" style="width: 90%;">
        	<div class="row" style="margin-top:160px">
				<%@ include file="/WEB-INF/views/search_empty.jsp"%>
				<div style="margin: auto; width:30%;height: 200px;" id="demo">
					<div>
						<span style="font-size: 24px; color: #1C86EE;">Genes:</span><br>
						<span style="font-size: 18px; color: #1C86EE; padding-left: 1em;">EGFR,KRAS, ERBB2, POU5F1, FOXA1…</span><br>
					</div>
					<div style="padding-top: 15px;">
						<span style="font-size: 24px; color: #1C86EE;">miRNA:</span><br>
						<span style="font-size: 18px; color: #1C86EE; padding-left: 1em;">has-let-7b,has-mir-100… </span><br>
					</div>
					<div style="padding-top: 15px;">
						<span style="font-size: 24px; color: #1C86EE;">Pathway:</span><br>
						<span style="font-size: 18px; color: #1C86EE; padding-left: 1em;">Apoptosis-GO,RNA elongation…</span><br>
					</div>
					<div style="padding-top: 15px;">
						<span style="font-size: 24px; color: #1C86EE;">Multigene:</span><br>
						<span style="font-size: 18px; color: #1C86EE; padding-left: 1em;">HOXA1,HOXA2, HOXA3…</span><br>
					</div>
					<div style="padding-top: 15px;">
						<span style="font-size: 24px; color: #1C86EE;">Genomics regions:</span><br>
						<span style="font-size: 18px; color: #1C86EE; padding-left: 1em;">Chr2 start: 33805280 end: 33808250…</span><br>
					</div>
				</div>
			</div>
      </div>
    </div>
    <%@ include file="../common/footer.jsp"%>
    
	<div id="backgroundDiv"></div>
	<div id="loading"></div>
	<%@ include file="/WEB-INF/common/dialog.jsp"%>
	</body>
</html>