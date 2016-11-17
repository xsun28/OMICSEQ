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
		<script type="text/javascript" src="${ctx}/static/js/jquery.js"></script>
		<script type="text/javascript" src="${ctx}/static/bootstrap/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/validation.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/jquery-ui.js"></script>
		<script type="text/javascript" src="${ctx}/static/script/common.js"></script>
		<script type="text/javascript" src="${ctx}/static/script/main.js"></script>
		<script type="text/javascript">
			var rootPath = '';
			$(document).ready(function(){
				rootPath = '${ctx}';
			});
			
			function showNames(charactor)
			{
				$.ajax({
						url : rootPath + "/findPathwayByCharactor.json",
						type : "post",
						data : {
							term:charactor
						},
						dataType : "json",
						success : function(data){
							var list="";
							for(var i=0; i<data.length; i++)
							{
								 var name = data[i].pathwayName;
								 if(name.length > 20)
								 {
								 	name = name.substr(0,20) + "...";
								 }
								 list+="<li style='margin-right: 40px;' title='"+data[i].pathwayName+"'><a href='${ctx }/result_pathway.htm?page=1&pathWayName="+ data[i].pathwayName +"&isLink=false&isHistory=false&sort=ASC' target='_blank'>"+name+"</a></li>";	
							}
							$("#pathwaysOfA").html(list);
							$("#ad_hengfu_temp").css('display','block');
							$("#expandImg").attr('src', '${ctx}/static/images/expand.png');
							$("#isExpand").val(1);
							$("#expandImg").attr('title', 'Hidden');
						},
						error:function(){
							//alert("some errror");
						}
					});	
			}
			
			function hiddenNames() {
				var isExpand = $("#isExpand").val();
				if(isExpand == 1)
				{
					$("#ad_hengfu_temp").css('display','none');
					$("#expandImg").attr('src', '${ctx}/static/images/shrink.png');
					$("#expandImg").attr('title', 'Expand');
					$("#isExpand").val(0);
				} else {
					$("#ad_hengfu_temp").css('display','block');
					$("#expandImg").attr('src', '${ctx}/static/images/expand.png');
					$("#expandImg").attr('title', 'Hidden');
					$("#isExpand").val(1);
				}
				
			}
		</script>
	</head>
	<body>                                                     		
		<%@ include file="../common/header.jsp"%>
		
		<div  class="container" style="width: 90%;padding-bottom:20px;">
			<div class="row" style="margin-top:50px;">
				<h1>11月份更新情况：</h1>
				<div class="tabbable" style="padding-left:36px;">
					<h4>一、数据更新：本月新增数据集总数为 39175 个</h4>
					<table class="table table-striped table-bordered table-hover table-condensed" style="width:40%;margin-left: 120px;">
						<tbody>
							<tr>
								<th>DataType</th>
								<th>Num</th>
							</tr>
							<tr>
								<td>CNV</td>
								<td>79</td>
							</tr>
							<tr>
								<td>Methylation</td>
								<td>417</td>
							</tr>
							<tr>
								<td>Illumina BodyMap</td>
								<td>16</td>
							</tr>
							<tr>
								<td>GEO</td>
								<td>8346</td>
							</tr>
							<tr>
								<td>ArrayExpress</td>
								<td>30317</td>
							</tr>
						</tbody>
					</table>
					
					<h4>二、服务功能更新：</h4>
					<div style="margin-left: 120px;">
						<h5>1. Gene Advanced Search</h5>
						<div style="padding-left:20px;">
							新增基因高级搜索功能，可以按查询条件：sample、tissue/status/factor 查询想要的结果，过滤不关心的实验数据<br>
							<a href="http://www.omicseq.org:8080/result_geneSearchAdvanced.htm" target="_blank">advanced</a>
						</div>
						
						<h5>2. Gene search结果体现方向</h5>
						<div style="padding-left:20px;">
							CNV等类型的实验数据中+-方向体现在结果页面中<br>
						</div>
						<h5>3. UCSC图文件根据基因的strand（+/-）使用start/end值画图</h5>
						<div style="padding-left:20px;">
							考虑到基因的strand有正有负，原来生成ucsc文件的时候全部取基因的start-500,start+500,改为如果strand为负时，取end-500,end+500范围。<br>
						</div>
						
						<h5>4. PathWay 查询页面，根据通路首字母展开所有通路列表功能</h5>
						<div style="padding-left:20px;">
								<div class="find-letter  fn-left" style="padding: 0 449px 0 0;">
	                            <span class="fn-left">Find By <span class="red">Name's </span>First Character：</span>
	                            <input type="hidden" id="isExpand" value="1">
	                            <ul class="find-letter-list">
	                                        <li><a data-meto="A" data-type="0" onclick="showNames('A');" href="javascript:void(0);" target="_self">A</a></li>
	                                        <li><a data-meto="B" data-type="0" onclick="showNames('B');" href="javascript:void(0);" target="_self">B</a></li>
	                                        <li><a data-meto="C" data-type="0" onclick="showNames('C');" href="javascript:void(0);" target="_self">C</a></li>
	                                        <li><a data-meto="D" data-type="0" onclick="showNames('D');" href="javascript:void(0);" target="_self">D</a></li>
	                                        <li><a data-meto="E" data-type="0" onclick="showNames('E');" href="javascript:void(0);" target="_self">E</a></li>
	                                        <li><a data-meto="F" data-type="0" onclick="showNames('F');" href="javascript:void(0);" target="_self">F</a></li>
	                                        <li><a data-meto="G" data-type="0" onclick="showNames('G');" href="javascript:void(0);" target="_self">G</a></li>
	                                        <li><a data-meto="H" data-type="0" onclick="showNames('H');" href="javascript:void(0);" target="_self">H</a></li>
	                                        <li><a data-meto="I" data-type="0" onclick="showNames('I');" href="javascript:void(0);" target="_self">I</a></li>
	                                        <li><a data-meto="J" data-type="0" onclick="showNames('J');" href="javascript:void(0);" target="_self">J</a></li>
	                                        <li><a data-meto="K" data-type="0" onclick="showNames('K');" href="javascript:void(0);" target="_self">K</a></li>
	                                        <li><a data-meto="L" data-type="0" onclick="showNames('L');" href="javascript:void(0);" target="_self">L</a></li>
	                                        <li><a data-meto="M" data-type="0" onclick="showNames('M');" href="javascript:void(0);" target="_self">M</a></li>
	                                        <li><a data-meto="N" data-type="0" onclick="showNames('N');" href="javascript:void(0);" target="_self">N</a></li>
	                                        <li><a data-meto="O" data-type="0" onclick="showNames('O');" href="javascript:void(0);" target="_self">O</a></li>
	                                        <li><a data-meto="P" data-type="0" onclick="showNames('P');" href="javascript:void(0);" target="_self">P</a></li>
	                                        <li><a data-meto="Q" data-type="0" onclick="showNames('Q');" href="javascript:void(0);" target="_self">Q</a></li>
	                                        <li><a data-meto="R" data-type="0" onclick="showNames('R');" href="javascript:void(0);" target="_self">R</a></li>
	                                        <li><a data-meto="S" data-type="0" onclick="showNames('S');" href="javascript:void(0);" target="_self">S</a></li>
	                                        <li><a data-meto="T" data-type="0" onclick="showNames('T');" href="javascript:void(0);" target="_self">T</a></li>
	                                        <li><a data-meto="U" data-type="0" onclick="showNames('U');" href="javascript:void(0);" target="_self">U</a></li>
	                                        <li><a data-meto="V" data-type="0" onclick="showNames('V');" href="javascript:void(0);" target="_self">V</a></li>
	                                        <li><a data-meto="W" data-type="0" onclick="showNames('W');" href="javascript:void(0);" target="_self">W</a></li>
	                                        <li><a data-meto="X" data-type="0" onclick="showNames('X');" href="javascript:void(0);" target="_self">X</a></li>
	                                        <li><a data-meto="Y" data-type="0" onclick="showNames('Y');" href="javascript:void(0);" target="_self">Y</a></li>
	                                        <li><a data-meto="Z" data-type="0" onclick="showNames('Z');" href="javascript:void(0);" target="_self">Z</a></li>
	                                        <li><a onclick="hiddenNames();" href="javascript:void(0);" target="_self"><img src="/static/images/expand.png" title="Hidden" id="expandImg"></a></li>
	                                    </ul>
	                        </div>
	                        <div id="ad_hengfu_temp">
	                        	<ul class="rank-list-ul" id="pathwaysOfA" style="width: 78%;">
	                        		
	                        	</ul>
                    		</div>
						</div>
				</div>
			</div>
			
			<h1>10月份更新情况：</h1>
				<div class="tabbable" style="padding-left:36px;">
				<h4>一、数据更新：本月新增数据集总数为 2421 个</h4>
				<table class="table table-striped table-bordered table-hover table-condensed" style="width:40%;margin-left: 120px;">
					<tbody>
						<tr>
							<th>DataType</th>
							<th>Num</th>
						</tr>
						<tr>
							<td>Summary Track（RNAseq,CNV,Methylation）</td>
							<td>162</td>
						</tr>
						<tr>
							<td>gEUVADIS RNA-seq</td>
							<td>660</td>
						</tr>
						<tr>
							<td>GEO RNA-seq</td>
							<td>1591</td>
						</tr>
						<tr>
							<td>MiRNA</td>
							<td>1500</td>
						</tr>
					</tbody>
				</table>
				
				<h4>二、服务功能更新：</h4>
				<div style="margin-left: 120px;">
					<h5>1. Genomic Region Search</h5>
					<div style="padding-left:20px;">
						根据基因范围查询并排名dataset，提供了12个范围列表<br>
					</div>
					
					<h5>2. DiseasesRank 查询</h5>
					<div style="padding-left:20px;">
						根据cancer type 查询tumor and normal matched unmatched排名前10的基因，查询发生变异比较显著的基因。<br>
						提供了26中常见的cancer type<br>
						<br>
					</div>
					<h5>3. Gene search dataset “Top 10 Gene“</h5>
					<div style="padding-left:20px;">
						在Gene search的结果集列表中，添加了每个数据集中排名前10的基因列表。<br>
						允许用户输入top number，动态显示结果数量。<br>
					</div>
					<h5>4.	miRNA Tab 增加数据导出功能</h5>
					<div style="padding-left:20px;">
						查询结果集可以导出为Excel文档。<br>
					</div>
				</div>
		</div>
		<div id="backgroundDiv"></div>	
		<%@ include file="../common/footer.jsp"%>
	</body>
</html>