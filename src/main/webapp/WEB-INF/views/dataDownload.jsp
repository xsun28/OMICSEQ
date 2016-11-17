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
				$("#dataDownload").attr("class","choice active");
				$("#pathway").attr("class","disabled");
				$("#genomicregion").attr("class","disabled");
				$("#gene").attr("class","disabled");
				$("#multigene").attr("class","disabled");
				$("#tab_content_genomicregion").attr("style","display: none");
				$("#tab_content_multigene").attr("style","display: none");
				$("#tab_content_pathway").attr("style","display: none");
				$("#tab_content_gene").attr("style","display: none");
				
				function split( val ) {
					return val.split("\n");
				}
				function extractLast( term ) {
					var t = split( term ).pop();
					if(t==""){
						return false;
					}
					return t;
				}
				$("#searchForm_geneSymbols").bind("keydown",function(event){
					if(event.keyCode.TAB &&
							$( this ).autocomplete( "instance" ).menu.active ) {
				    	event.preventDefault();
				        }
				}).autocomplete({
					source: function(request,response){
						$.getJSON("autocomplete_multigene.json",{
							term: extractLast(request.term)
						},response);
					},
					focus: function(){
						return false;
					},
					select: function( event, ui ) {
				    	var terms = split( this.value );
				        terms.pop();
				        terms.push( ui.item.value );
				        terms.push( "" );
						this.value = terms.join("\n");
				        return false;
				    }
				});
				
			});
			
		</script>
	</head>
	<body>
		<!-- Wrap all page content here -->
    <div id="wrap">
      <!-- Fixed navbar -->
      <%@ include file="../common/header.jsp"%>	

      <!-- Begin page content -->
      <div class="container" style="width: 90%;">
        	<div class="row" style="margin-top:60px">
				<%@ include file="/WEB-INF/views/search.jsp"%>
			</div>
			<!-- data download tab start -->
		<div class="tab-content" id="tab-content_dataDownload" style="height: 800px;" >
			<div class="control-group" style="margin:auto 250px auto 250px;">
			<div style="float:left; margin-left: 100px;">
				<h3>
					<label for="geneSymbols"><fmt:message key="label.gene"></fmt:message><span style="color: red;size: 8px;">*</span>:</label>
				</h3>
					<span style="font-size:14px"><textarea cols="20" rows="7" id="searchForm_geneSymbols" style="resize:none;">${multigene }</textarea></span>
				</div><div style="float:left;margin-left: 100px;"><h3>
					<label for="cell" class="control-label"><fmt:message key="label.cell"/>:</label>
				</h3>
					<input type="text" id="cell">
				<h3>
					<label for="factor" class="control-label" style="margin-top:30px;"><fmt:message key="label.factor"/>:</label>
				</h3>
					<input type="text" id="factor"></div>
					<br><div style="float:left;">
				<h3>
					<label for="experiment" class="control-label"><fmt:message key="label.Experiments"/>:</label>
				</h3>
				<div class="controls" style="width: 100%;">
					<c:forEach items="${settingDTO.experimentsMap}" var="experiment">
						<div class="selectItem">
							<input type="checkbox" value="${experiment.key}" name="dExperiments" checked="checked"/>
							<label>${experiment.key}</label>
						</div>
					</c:forEach>
				</div><br>
				<h3>
					<label for="source" class="control-label"><fmt:message key="label.source"/>:</label>
				</h3>
					<div class="controls" style="width: 100%;">
					<c:forEach items="${settingDTO.sourcesMap}" var="source">
						<div class="selectItem">
							<input type="checkbox" value="${source.key}" name="dSources" checked="checked"/>
							<label>${source.key}</label>
						</div>
					</c:forEach>
					</div>
				<br>
				<h3>
					<label for="downloadColumn" class="control-label" ><fmt:message key="label.downloadCol"></fmt:message>:</label>
				</h3>
				<div class="controls" style="width: 100%; margin-right:600px;">
					<c:forEach	items="${columns }" var="column">
						<div class="selectItem">
							<input type="checkbox" name="dColumn" value="${column}" checked="checked"/>
							<label>${column}</label>
						</div>
					</c:forEach>
					<button class="btn btn-success" onclick="downloadData();" style="float: right;"><fmt:message key="label.download"></fmt:message></button>
					</div>
					
				</div>
				</div>
		  	</div>
		</div>			
	<!-- data download tab end -->
      
    </div>
    <%@ include file="../common/footer.jsp"%>
    
	<div id="backgroundDiv"></div>
	<div id="loading"></div>
	<%@ include file="/WEB-INF/common/dialog.jsp"%>
	</body>
</html>