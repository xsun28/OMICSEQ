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
				$("#diseasesRank").attr("class","choice active");
				$("#tab_content_diseasesRank").attr("style", "display: _block");
				$("#pathway").attr("class","disabled");
				$("#genomicregion").attr("class","disabled");
				$("#gene").attr("class","disabled");
				$("#multigene").attr("class","disabled");
				$("#tab_content_genomicregion").attr("style","display: none");
				$("#tab_content_multigene").attr("style","display: none");
				$("#tab_content_pathway").attr("style","display: none");
				$("#tab_content_gene").attr("style","display: none");
				if($("#currentDiseasesType").val()!=''){
					$("#output").attr("style","display: _block");
				}
				
				var value = '${diseasesType}';
				$("#diseasesType").find("option[value='"+value+"']").attr("selected",true);
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
			<div class="row" style="margin-top: 60px">
				<%@ include file="/WEB-INF/views/search.jsp"%>
			</div>
			<div id="output" class="row" style="display: none;">
				<div style="padding-left:100px;">
				<b>RNA-seq top genes:</b><br>
					<table style="width: 80%">
						<tr>
							<td align="center" style="width: 50%">unMatched:<br></td>
							<td align="center" style="width: 50%">Matched:<br></td>
						</tr>
						<tr>
							<td>
							<table style="width: 100%">
								<c:forEach items="${RNAseq_UnMatched_top10}" var="item" varStatus="status">
									<tr><td align="center"><a href="${ctx}/result.htm?page=1&geneSymbol=${item.seqName}&genome=Human&isLink=false&isHistory=false" target="_blank">${item.seqName}</a></td></tr>
								</c:forEach>
							</table>
							</td>
							<td>
							<table style="width: 100%">
								<c:forEach items="${RNAseq_Matched_top10}" var="item" varStatus="status">
									<tr><td align="center"><a href="${ctx}/result.htm?page=1&geneSymbol=${item.seqName}&genome=Human&isLink=false&isHistory=false" target="_blank">${item.seqName}</a></td></tr>
								</c:forEach>
							</table>
							</td>
						</tr>
					</table>

				<b>CNV &nbsp;&nbsp;top &nbsp;&nbsp; genes:</b><br>
					<table style="width: 80%">
						<tr>
							<td align="center" style="width: 50%">unMatched:<br></td>
							<td align="center" style="width: 50%">Matched:<br></td>
						</tr>
						<tr>
							<td>
							<table style="width: 100%">
								<c:forEach items="${CNV_UnMatched_top10}" var="item" varStatus="status">
									<tr><td align="center"><a href="${ctx}/result.htm?page=1&geneSymbol=${item.seqName}&genome=Human&isLink=false&isHistory=false" target="_blank">${item.seqName}</a></td></tr>
								</c:forEach>
							</table>
							</td>
							<td>
							<table style="width: 100%">
								<c:forEach items="${CNV_Matched_top10}" var="item" varStatus="status">
									<tr><td align="center"><a href="${ctx}/result.htm?page=1&geneSymbol=${item.seqName}&genome=Human&isLink=false&isHistory=false" target="_blank">${item.seqName}</a></td></tr>
								</c:forEach>
							</table>
							</td>
						</tr>
					</table>
					
				<b>Methylation top genes:</b><br>
					<table style="width: 80%">
						<tr>
							<td align="center" style="width: 50%">unMatched:<br></td>
							<td align="center" style="width: 50%">Matched:<br></td>
						</tr>
						<tr>
							<td>
							<table style="width: 100%">
								<c:forEach items="${Methylation_UnMatched_top10}" var="item" varStatus="status">
									<tr><td align="center"><a href="${ctx}/result.htm?page=1&geneSymbol=${item.seqName}&genome=Human&isLink=false&isHistory=false" target="_blank">${item.seqName}</a></td></tr>
								</c:forEach>
							</table>
							</td>
							<td>
							<table style="width: 100%">
								<c:forEach items="${Methylation_Matched_top10}" var="item" varStatus="status">
									<tr><td align="center"><a href="${ctx}/result.htm?page=1&geneSymbol=${item.seqName}&genome=Human&isLink=false&isHistory=false" target="_blank">${item.seqName}</a></td></tr>
								</c:forEach>
							</table>
							</td>
						</tr>
					</table>
					
					<!-- <b>somatic mutation top genes:</b><br>
					<table style="width: 80%">
						<tr>
							<td align="center" style="width: 50%">original:<br></td>
							<td align="center" style="width: 50%">normalized frequency:<br></td>
						</tr>
						<tr>
							<td>
							<table style="width: 100%">
								<c:forEach items="${somatic_mutation_top10}" var="item" varStatus="status">
									<tr><td align="center"><a href="${ctx}/result.htm?page=1&geneSymbol=${item.seqName}&isLink=false&isHistory=false" target="_blank">${item.seqName}</a></td></tr>
								</c:forEach>
							</table>
							</td>
							<td>
							<table style="width: 100%">
								<c:forEach items="${somatic_mutation_frequency_top10}" var="item" varStatus="status">
									<tr><td align="center"><a href="${ctx}/result.htm?page=1&geneSymbol=${item.seqName}&isLink=false&isHistory=false" target="_blank">${item.seqName}</a></td></tr>
								</c:forEach>
							</table>
							</td>
						</tr>
					</table> -->					
					
					<b>miRNA top:</b><br>
					<table style="width: 80%">
						<tr>
							<td align="center" style="width: 50%">unMatched:<br></td>
							<td align="center" style="width: 50%">Matched:<br></td>
						</tr>
						<tr>
							<td>
							<table style="width: 100%">
								<c:forEach items="${miRNA_top10}" var="item" varStatus="status">
									<tr><td align="center"><a href="${ctx}/result_miRNA.htm?page=1&miRNA=${item}&sort=ASC&isLink=false&isHistory=false" target="_blank">${item}</a></td></tr>
								</c:forEach>
							</table>
							</td>
							<td>
							<table style="width: 100%">
								<c:forEach items="${miRNA_matched_top10}" var="item" varStatus="status">
									<tr><td align="center"><a href="${ctx}/result_miRNA.htm?page=1&miRNA=${item}&sort=ASC&isLink=false&isHistory=false" target="_blank">${item}</a></td></tr>
								</c:forEach>
							</table>
							</td>
						</tr>
					</table>
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