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
				showTab('pathway');
				rootPath = '${ctx}';
				
				if($("#searchForm_pathWay").val().trim()!=''){
					$("#output").attr("style","display: _block");
				}
				var el = $('.linkWord').last();
				var value = el.html();
				if(!!value) {
					value = value.replace(",","");
					el.html(value);
				}
				
				/*
				var isHistoryResult = "${isHistoryResult}";
				if (!!!isHistoryResult || isHistoryResult == "false") {
					//初始化两个图片弹出框
					var ucscDrag = document.getElementById("ucscDrag");
					initDragDialog(ucscDrag);
					var mainDrag = document.getElementById("mainDrag");
					initDragDialog(mainDrag);
					//显示USUC
					var type = $('#currentSeqName').val();
					var start = $('#currentStart').val();
					var end = $('#currentEnd').val();
					var imageUrl = "http://112.25.20.156/ucsc/" + type + "_" + start + "_" + end + ".png";
					var html = '<img src="'+imageUrl+'" onerror="showErrorImageTip(this);"/>';
					$('#ucscDrag .content a').html('').append(html);
					//显示统计图
					var geneId = $('#currentGeneId').val();
					var mainUrl = "http://112.25.20.156/images/" + '${ucscUrl}';
					//mainUrl = "http://112.25.20.156/images/9.png";
					var html1 = '<img src="'+mainUrl+'" onerror="showErrorImageTip(this);" style="cursor: auto;"/>';
					$('#mainDrag .content a').html('').append(html1);
					
				}
				*/
			});
			function showMoreSingleGene(){
				if($("#moreSingleGene").css("display")=="block"){
					$("#moreSingleGene").css("display","none");
					$("#moreImage").attr("src","${ctx }/static/images/more1.png");
				}else{
					$("#moreSingleGene").css("display","block");
					$("#moreImage").attr("src","${ctx }/static/images/more.png");
				}
			}
			
			//PubMed,Wikipedia,Google链接去下划线
			function PubMedLink(){
				var pathwayName = "${pathwayName }";
				pathwayName = pathwayName.replace("_", " ");
				window.open("http://www.ncbi.nlm.nih.gov/pubmed/?term="+pathwayName);
			}
			function WikipediaLink(){
				var pathwayName = "${pathwayName }";
				pathwayName = pathwayName.replace("_", " ");
				window.open("http://en.wikipedia.org/wiki/"+pathwayName);
			}
			function GoogleLink(){
				var pathwayName = "${pathwayName }";
				pathwayName = pathwayName.replace("_", " ");
				window.open("http://www.google.com.hk/#newwindow=1&q="+pathwayName+"&safe=strict") ;
			}
			
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
								 list+="<li style='margin-right: 8px;' title='"+data[i].pathwayName+"'><a href='${ctx }/result_pathway.htm?page=1&pathWayName="+ data[i].pathwayName +"&isLink=false&isHistory=false&sort=ASC' target='_blank'>"+name+"</a></li>";	
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
		<!-- Wrap all page content here -->
    <div id="wrap">
      <!-- Fixed navbar -->
      <%@ include file="../common/header.jsp"%>	

      <!-- Begin page content -->
      <div class="container" style="width: 90%;">
        	<div class="row" style="margin-top:60px">
				<%@ include file="/WEB-INF/views/search.jsp"%>
			</div>
			<div class="find fn-clear" id="topFixedNav-letter">
                        <div class="find-letter  fn-left">
                            <span class="fn-left">Find By <span class="red">Name's </span>First Character：</span>
                            <input type="hidden" value="1" id="isExpand">
                            <ul class="find-letter-list">
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('A');" data-type="0" data-meto="A">A</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('B');" data-type="0" data-meto="B">B</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('C');" data-type="0" data-meto="C">C</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('D');" data-type="0" data-meto="D">D</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('E');" data-type="0" data-meto="E">E</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('F');" data-type="0" data-meto="F">F</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('G');" data-type="0" data-meto="G">G</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('H');" data-type="0" data-meto="H">H</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('I');" data-type="0" data-meto="I">I</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('J');" data-type="0" data-meto="J">J</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('K');" data-type="0" data-meto="K">K</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('L');" data-type="0" data-meto="L">L</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('M');" data-type="0" data-meto="M">M</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('N');" data-type="0" data-meto="N">N</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('O');" data-type="0" data-meto="O">O</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('P');" data-type="0" data-meto="P">P</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('Q');" data-type="0" data-meto="Q">Q</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('R');" data-type="0" data-meto="R">R</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('S');" data-type="0" data-meto="S">S</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('T');" data-type="0" data-meto="T">T</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('U');" data-type="0" data-meto="U">U</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('V');" data-type="0" data-meto="V">V</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('W');" data-type="0" data-meto="W">W</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('X');" data-type="0" data-meto="X">X</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('Y');" data-type="0" data-meto="Y">Y</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="showNames('Z');" data-type="0" data-meto="Z">Z</a></li>
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="hiddenNames();"><img id="expandImg" title="Hidden" src="${ctx}/static/images/expand.png"/></a></li>
                                        
                                    </ul>
                        </div>
                    </div>
                    <div id="ad_hengfu_temp">
                        	<ul class="rank-list-ul" id="pathwaysOfA">
                        		
                        	</ul>
                    </div>
			<div id="output" class="row" style="display: none;">
				<input type="hidden" value="${result.current.seqName }" id="currentSeqName"/>
				<input type="hidden" value="${result.current.start }" id="currentStart"/>
				<input type="hidden" value="${result.current.end }" id="currentEnd"/>
				<input type="hidden" value="${result.current.geneId }" id="currentGeneId" />
				<div id="relateinfo" class="span10 text-center">
					<c:if test="${!isHistoryResult}">
						<div class="row" id="topinfo">
							<fmt:message key="button.pathwaysearch"/> "<strong>${pathwayName }</strong>"<fmt:message key="label.find"/> ${totalRecords} <fmt:message key="label.result"/> 
							<c:if test="${totalRecords gt 0}">(${result.usedTime } <fmt:message key="label.seconds"/>) <fmt:message key="label.about"/> 
									<strong>${result.current.txName }</strong>(hg19)</c:if>
						</div>
						<c:if test="${geneItemList[1] != null}"> 
							<div class="row linkRow">
								<fmt:message key="label.wildfind.pathway"/>
									<c:forEach items="${geneItemList}" var="geneitem" varStatus="status" begin="0" end="9">
										<c:if test="${geneitem.txName != result.current.txName }">
											<span class="linkWord_pathway">${geneitem.txName }<c:if test="${status.count lt geneSize}">,</c:if></span>
										</c:if>
										</c:forEach> 
								<c:if test="${geneItemList.size() > 10}">
									<img  src="${ctx }/static/images/more1.png" onclick="showMoreSingleGene();" title="more.." id="moreImage">
									<div id="moreSingleGene" style="display: none;">
										<c:forEach items="${geneItemList}" var="geneitem" varStatus="status" begin="10" end="${geneItemList.size()-1 }">
											<c:if test="${geneitem.txName != result.current.txName }">
												<span class="linkWord_pathway">${geneitem.txName }<c:if test="${status.count lt geneSize-10}">,</c:if></span>
											</c:if>
										</c:forEach> 
									</div>
								</c:if>
							</div>					
						</c:if> 
						<div class="row infoRow">
							<fmt:message key="label.searchmore1"/> <a href="javaScript:PubMedLink()">PubMed</a>,
							<a href="javaScript:WikipediaLink()">Wikipedia</a>,
							<a href="javaScript:GoogleLink()">Google,</a>
							<a target="_blank" href="${pathwayUrl }">MsigDB</a><fmt:message key="label.searchmore2"/>
						</div>
						<hr>
					</c:if>
					<c:if test="${isHistoryResult}">
						<div class="row infoRow" style="text-align:center">
							<fmt:message key="label.historyresult1"/><fmt:message key="label.historyresult2"/> ‘${searchGene }’<fmt:message key="label.historyresult3"/> ${date}.
						</div>
						<hr>
					</c:if>
					<div class="row paginateWrap">
						<c:if test="${page == 1}"><span href="javascript:;" class="inactive paginatebtn"><fmt:message key="button.previous"/></span></c:if>
						<c:if test="${page gt 1}"><a href="javascript:;" class="paginatebtn" onclick="prevPage_pathWay();"><fmt:message key="button.previous"/></a></c:if>
						<c:if test="${totalPage == 0 }">
							<a href="javascript:;" class="inactive">1</a> 
						</c:if>
						<c:forEach var="aPage" begin="${beginPage}"  end="${endPage}">
							<span href="javascript:;" <c:if test="${page eq aPage}">class="current paginate"</c:if> <c:if test="${page != aPage}">class="paginate pathWay"</c:if>
								title="Go to page ${aPage } of ${totalPage }">${aPage }</span> 	
						</c:forEach>
						<c:if test="${page == totalPage || totalPage == 0}"><span href="javascript:;" class="inactive paginatebtn"><fmt:message key="button.next"/></span></c:if>
						<c:if test="${page lt totalPage}"><a href="javascript:;" class="paginatebtn" onclick="nextPage_pathWay();"><fmt:message key="button.next"/></a></c:if>
					</div>
					<div class="row">
						<input type="hidden" id="sortType" value="${sortType}"/>
						<table id="result_detail" class="table table-striped table-bordered table-hover table-condensed">
							<tr>
								<th width="4%">
									<c:if test="${!isHistoryResult}"><input class="rankcheckbox" type="checkbox" onclick="selectAll(this)" title="" rel="tooltip" id="selectAll" data-original-title="Select All"></c:if>
									<fmt:message key="label.rank"/>  &nbsp;
									<c:if test="${!isHistoryResult}">
										<a style="cursor:pointer;" onclick="list_PathWay_BySort(this);">
											<i <c:if test="${sortType == 'ASC'}">class="icon-arrow-up"</c:if>
												<c:if test="${sortType == 'DESC'}">class="icon-arrow-down"</c:if> ></i>
										</a>
									</c:if>
								</th>
								<th width="6%"><fmt:message key="label.datesetid"/></th>
								<th width="7%"><fmt:message key="label.dataType"/></th>
								<th width="9%"><fmt:message key="label.cell"/></th>
								<th width="9%"><fmt:message key="label.factor"/></th>
								<th width="8%"><fmt:message key="label.average"/></th>
								<th width="10%"><fmt:message key="label.comulativeValue"/></th>
								<th width="10%"><fmt:message key="label.comulativeRank"/></th>
								<th width="0%">tss5kCount</th>
								<th width="0%">tssTesCount</th>
								<th width="6%"><fmt:message key="label.study"/></th>
								<th width="7%"><fmt:message key="label.lab"/></th>
								<!-- <th width="8%">TimeStamp</th> -->
								<th width="26%"><fmt:message key="label.moreinfo"/></th>
							</tr>
							<c:if test="${result.sampleItemList[0] == null}">
								<tr><td colspan="11"><fmt:message key="label.noTop1Pathway"></fmt:message></td></tr>
							</c:if>
							<c:forEach items="${result.sampleItemList}" var="item" varStatus="status">
								<tr class="sampleRow">
									<td>
										<c:if test="${!isHistoryResult}"><input type="hidden" value="${item.sampleId }"/><input class="rankcheckbox" type="checkbox" value="${status.count + (page-1)*pages}" name="rowNum" class="rank"/></c:if>
										${status.count + (page-1)*pageSize}</td>
									<td class="sampleId">${item.sampleId }</td>
									<td class="dataType" title="${item.dataType }" nowrap="nowrap"
										style="background:<c:if test="${item.dataType == 'RNA-seq'}">#fdc7c7</c:if>
											<c:if test="${fn:containsIgnoreCase(item.dataType, 'ChIP-seq')}">#fcfeb8</c:if>
											<c:if test="${item.dataType == 'CNV' }">#B0E0E6</c:if>
											<c:if test="${item.dataType == 'MethyLation' }">#858EFA</c:if>
											<c:if test="${item.dataType == 'Microarray' }">#8C8D8A</c:if>
											<c:if test="${item.dataType == 'Dnase-seq'}">#A6D377</c:if>
											<c:if test="${item.dataType == 'Summary Track'}">#AEE640</c:if>
											<c:if test="${item.dataType == 'Somatic Mutations'}">#4292D1</c:if>
											"><c:if test="${item.dataType == 'Summary Track' && item.study == 'TCGA'}">${item.settype}</c:if>${item.dataType }<c:if test="${item.dataType == 'Summary Track' && item.study == 'TCGA Firebrowse'}">${item.settype}</c:if></td>
									<td class="cell" title="${item.cell_desc }" nowrap="nowrap">${item.cell }</td>
									<td class="factor" title="${item.factor_desc}" nowrap="nowrap">${item.detail}</td>
									<td class="orderTotal" title="${item.mixturePerc}%" nowrap="nowrap">${item.mixturePerc}%</td>
									<td class="tescount" title="${item.tssTesCount}" nowrap="nowrap">${item.tssTesCount}</td>
									<td class="percent" title="${item.percentileFormat}" nowrap="nowrap">${item.percentileFormat}</td>
									<td class="count">${item.tssCount}</td>
									<td class="tescount">${item.tssTesCount}</td>
									<td class="study" title="${item.study }" nowrap="nowrap"
										style="background:<c:if test="${item.study == 'TCGA'}">#fdc7c7</c:if>
											<c:if test="${item.study == 'ENCODE'}">#fcfeb8</c:if>
											<c:if test="${item.study == 'ICGC'}">#caef90</c:if>	
											<c:if test="${item.study == 'SRA'}">#eedded</c:if>
											<c:if test="${item.study == 'Epigenome Roadmap'}">#d7eaf8</c:if>
											<c:if test="${item.study == 'GEO'}">#B2B2C2</c:if>
											<c:if test="${item.study == 'CCLE'}">#9F80A0</c:if>"	
									>${item.study }</td>
									<td class="lab" title="${item.lab}">${item.lab}</td>
									<%-- <td class="submitTile" title="${item.timeStamp }">${item.timeStamp }</td> --%>
									<td align="left">
										<ul class="inline">
											<li>
												<input type="hidden" class="data1" value="${item.metaData[0]}"/>
												<input type="hidden" class="data2" value="${item.metaData[1]}"/>
												<a onclick="showMetaData(this);" class="btn btn-mini">MetaData</a>
											</li>
											<li><a class="btn btn-mini" target="_blank" href="${item.pubMedUrl }">PubMed</a></li>
											<li><a class="btn btn-mini" target="_blank" href="${item.geoUrl }">GEO</a></li>
									<!--		<li><a class="btn btn-mini" href="${item.url }"><i class="icon-download-alt"></i>Download</a></li> -->
										</ul>
									</td>
								</tr>
							</c:forEach>
						</table>
					</div>
<div class="row paginateWrap">
						<c:if test="${page == 1}"><span href="javascript:;" class="inactive paginatebtn"><fmt:message key="button.previous"/></span></c:if>
						<c:if test="${page gt 1}"><a href="javascript:;" class="paginatebtn" onclick="prevPage_pathWay();"><fmt:message key="button.previous"/></a></c:if>
						<c:if test="${totalPage == 0 }">
							<a href="javascript:;" class="inactive">1</a> 
						</c:if>
						<c:forEach var="aPage" begin="${beginPage}"  end="${endPage}">
							<span href="javascript:;" <c:if test="${page eq aPage}">class="current paginate"</c:if> <c:if test="${page != aPage}">class="paginate pathWay"</c:if>
								title="Go to page ${aPage } of ${totalPage }">${aPage }</span> 	
						</c:forEach>
						<c:if test="${page == totalPage || totalPage == 0}"><span href="javascript:;" class="inactive paginatebtn"><fmt:message key="button.next"/></span></c:if>
						<c:if test="${page lt totalPage}"><a href="javascript:;" class="paginatebtn" onclick="nextPage_pathWay();"><fmt:message key="button.next"/></a></c:if>
					</div>
			<!--		<c:if test="${not empty result.current && !isHistoryResult}">
					<div class="row" style="margin-bottom:20px;">
				      	<div class="span5" id="downloadCurTable" error="<fmt:message key="failed.select"/>">
				      		<a onclick="downloadSelected(this)" class="btn btn-large"><fmt:message key="button.downloadselect"/></a>
				      	</div>
				      	<div class="span5" id="downloadCurTable">
				      		<a class="btn btn-large" href="${ctx}/export/xlsx.htm?geneId=${result.current.geneId}&term=${searchGene}"><fmt:message key="button.downloadall"/></a>
				      	</div>
				      	<div class="span5" id="collect">
				      		<a onclick="saveHistory();" class="btn btn-large"><fmt:message key="button.collection"/></a>
				      	</div>
				    </div>
					</c:if>  -->
				</div>
			</div>
      </div>
    </div>
    <div id="dialog"></div>
    <%@ include file="../common/footer.jsp"%>
    <c:if test="${!isHistoryResult}">
	    <div id="ucscDrag" errorMessage="<fmt:message key="error.noimage"/>" style="display:none" class="drag" minWidth="800" minHeight="400">
		    <div class="title">
		        <h2><strong>${result.current.geneSymbol}</strong>,${result.current.txName }(hg19): Chr: ${result.current.seqNameShort }, Start: ${result.current.start }, End: ${result.current.end }, Strand: ${result.current.strand }</h2>
		        <div>
		            <a class="min" href="javascript:;" title="<fmt:message key="placeholder.min"/>" targetoA="dialogbutton2"></a>
		            <a class="max" href="javascript:;" title="<fmt:message key="placeholder.max"/>"></a>
		            <a class="revert" href="javascript:;" title="<fmt:message key="placeholder.revert"/>"></a>
		            <a class="closeDialog" href="javascript:;" title="<fmt:message key="button.close"/>" targetoA="dialogbutton2"></a>
		        </div>
		    </div>
		    <div class="resizeL"></div>
		    <div class="resizeT"></div>
		    <div class="resizeR"></div>
		    <div class="resizeB"></div>
		    <div class="resizeLT"></div>
		    <div class="resizeTR"></div>
		    <div class="resizeBR"></div>
		    <div class="resizeLB"></div>
		    <div class="content">
		    	<a href="http://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position=${result.current.seqName }:${result.current.start }-${result.current.end}" target="_blank">
		    	
		    	</a>
		    </div>    
		</div>
		<div id="mainDrag" errorMessage="<fmt:message key="error.noimage"/>" style="display:none" class="drag" minWidth="800" minHeight="400">
		    <div class="title">
		        <h2><strong>${result.current.geneSymbol}</strong>,${result.current.txName }(hg19): Chr: ${result.current.seqNameShort }, Start: ${result.current.start }, End: ${result.current.end }, Strand: ${result.current.strand }</h2>
		        <div>
		            <a class="min" href="javascript:;" title="<fmt:message key="placeholder.min"/>" targetoA="dialogbutton1"></a>
		            <a class="max" href="javascript:;" title="<fmt:message key="placeholder.max"/>"></a>
		            <a class="revert" href="javascript:;" title="<fmt:message key="placeholder.revert"/>"></a>
		            <a class="closeDialog" href="javascript:;" title="<fmt:message key="button.close" />" targetoA="dialogbutton1"></a>
		        </div>
		    </div>
		    <div class="resizeL"></div>
		    <div class="resizeT"></div>
		    <div class="resizeR"></div>
		    <div class="resizeB"></div>
		    <div class="resizeLT"></div>
		    <div class="resizeTR"></div>
		    <div class="resizeBR"></div>
		    <div class="resizeLB"></div>
		    <div class="content">
		    	<a href="javascript:;">
		    		
		    	</a>
		    </div>    
		</div>
	</c:if>
	<div id="backgroundDiv"></div>
	<div id="loading"></div>
	<%@ include file="/WEB-INF/common/dialog.jsp"%>
	<!--  
	<c:if test="${!isHistoryResult}">
		<a class="open btn btn-primary dialogbutton1" id="dialogbutton1" href="javascript:;" onclick="showDialog(this);" title="MAIN" targetDrag="mainDrag" style="display:block">M</a>
		<a class="open btn btn-primary dialogbutton2" id="dialogbutton2" href="javascript:;" onclick="showDialog(this);" title="UCSC" targetDrag="ucscDrag" style="display:block">G</a>
	</c:if>
	-->
	</body>
</html>
