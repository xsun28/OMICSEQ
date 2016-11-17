
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/tags.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title>Omics Search Engine</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet"
	href="${ctx}/static/bootstrap/css/bootstrap.min.css">
<link type="text/css" rel="stylesheet"
	href="${ctx}/static/css/custom.min.css">
<link type="text/css" rel="stylesheet"
	href="${ctx}/static/css/reset.min.css">
<link type="text/css" rel="stylesheet"
	href="${ctx}/static/css/result.min.css">
<link type="text/css" rel="stylesheet"
	href="${ctx}/static/css/jquery-ui.min.css">
<link type="text/css" rel="stylesheet"
	href="${ctx}/static/css/dialog.css">
<script type="text/javascript" src="${ctx}/static/js/jquery.js"></script>
<script type="text/javascript"
	src="${ctx}/static/bootstrap/js/bootstrap.min.js"></script>
<script type="text/javascript"
	src="${ctx}/static/bootstrap/js/bootstrap-multiselect.js"></script>
<script type="text/javascript" src="${ctx}/static/js/validation.js"></script>
<script type="text/javascript" src="${ctx}/static/js/jquery-ui.js"></script>
<script type="text/javascript" src="${ctx}/static/script/common.js"></script>
<script type="text/javascript" src="${ctx}/static/script/main.js"></script>
<script type="text/javascript" src="${ctx}/static/script/dialog.js"></script>
<script type="text/javascript">
	var rootPath = '';
	$(document)
			.ready(
					function() {
						showTab('miRNA');
						rootPath = '${ctx}';
						if ($("#searchForm_miRNA").val().trim() != ''
								&& $("#searchForm_miRNA").val().trim() != 'hsa-') {
							$("#output").attr("style", "display: _block");
						}
						var el = $('.linkWord').last();
						var value = el.html();
						if (!!value) {
							value = value.replace(",", "");
							el.html(value);
						}

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
							var imageUrl = "http://112.25.20.156/ucsc/" + type
									+ "_" + start + "_" + end + ".png";
							//imageUrl = "http://112.25.20.156/ucsc/chr9_125377017_125377961.png";
							var html = '<img src="' + imageUrl
									+ '" onerror="showErrorImageTip(this);"/>';
							$('#ucscDrag .content a').html('').append(html);
							//显示统计图
							var geneId = $('#currentGeneId').val();
							var mainUrl = "http://112.25.20.156/images/"
									+ '${ucscUrl}';
							//mainUrl = "http://112.25.20.156/images/9.png";
							var html1 = '<img src="'
									+ mainUrl
									+ '" onerror="showErrorImageTip(this);" style="cursor: auto;"/>';
							$('#mainDrag .content a').html('').append(html1);

						}
					});
	function showMoreSingleGene() {
		if ($("#moreSingleGene").css("display") == "block") {
			$("#moreSingleGene").css("display", "none");
			$("#moreImage").attr("src", "${ctx }/static/images/more1.png");
		} else {
			$("#moreSingleGene").css("display", "block");
			$("#moreImage").attr("src", "${ctx }/static/images/more.png");
		}
	}
	
	function showTop10Genes(miRNASampleId,isFirst){
		rootPath = '${ctx}';
		var size = '10';
		if(!isFirst){
			size = $("#showGeneSize_"+miRNASampleId).val().trim();
		}
		$.ajax({
			url : rootPath + "/showTopMiRNAGenes.json",
			data : {
				miRNASampleId : miRNASampleId,
				ctx : rootPath,
				size : size
			},
			dataType : 'html',
			type : "post",
			success : function(data){
				$("#content_"+miRNASampleId).html(data);
			}
		});
		var currentDrag = document.getElementById(miRNASampleId+"_Drag");
		initDragDialog(currentDrag, 3);
		$('.drag').not($(currentDrag)).css("z-index","1050");
		$(currentDrag).css("z-index","1060");
		currentDrag.style.display = "block";
	}

	//PubMed,Wikipedia,Google链接去下划线
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
				<input type="hidden" value="${result.current.seqName }"
					id="currentSeqName" /> <input type="hidden"
					value="${result.current.start }" id="currentStart" /> <input
					type="hidden" value="${result.current.end }" id="currentEnd" /> <input
					type="hidden" value="${result.current.geneId }" id="currentGeneId" />
				<div id="relateinfo" class="span10 text-center">
					<c:if test="${!isHistoryResult}">
						<div class="row" id="topinfo">
							<fmt:message key="button.search" />
							"<strong>hsa-${miRNA }</strong>"
							<fmt:message key="label.find" />${totalRecords}(<fmt:message key="lable.topone"></fmt:message><strong>${result.total_all}</strong>)
							<fmt:message key="label.result" />
							<c:if test="${totalRecords gt 0}">(${result.usedTime } <fmt:message
									key="label.seconds" />) <fmt:message key="label.about" />
								<strong>${result.current.txName }</strong>(hg19)</c:if>
						</div>
						<c:if test="${geneItemList[1] != null}">
							<div class="row linkRow">
								<fmt:message key="label.wildfind.pathway" />
								<c:forEach items="${geneItemList}" var="geneitem"
									varStatus="status" begin="0" end="9">
									<c:if test="${geneitem.txName != result.current.txName }">
										<span class="linkWord_pathway">${geneitem.txName }<c:if
												test="${status.count lt geneSize}">,</c:if></span>
									</c:if>
								</c:forEach>
								<c:if test="${geneItemList.size() > 10}">
									<img src="${ctx }/static/images/more1.png"
										onclick="showMoreSingleGene();" title="more.." id="moreImage">
									<div id="moreSingleGene" style="display: none;">
										<c:forEach items="${geneItemList}" var="geneitem"
											varStatus="status" begin="10" end="${geneItemList.size()-1 }">
											<c:if test="${geneitem.txName != result.current.txName }">
												<span class="linkWord_pathway">${geneitem.txName }<c:if
														test="${status.count lt geneSize-10}">,</c:if></span>
											</c:if>
										</c:forEach>
									</div>
								</c:if>
							</div>
						</c:if>
						<div class="row infoRow">
							<fmt:message key="label.searchmore1" />
							<a target="_blank"
								href="http://www.ncbi.nlm.nih.gov/pubmed/?term=hsa-${miRNA }">PubMed</a>,
							<a target="_blank"
								href="http://en.wikipedia.org/wiki/hsa-${miRNA }">Wikipedia</a>,
							<a target="_blank"
								href="http://www.google.com.hk/#newwindow=1&q=hsa-${miRNA }&safe=strict">Google,</a>
							<a target="_blank"
								href="http://www.wikigenes.org/?search=hsa-${miRNA }&db=_any&cat=&type=&field=&org=&action=go&ftype=0">WikiGenes</a>,
							<a target="_blank"
								href="http://www.genecards.org/index.php?path=/Search/keyword/hsa-${miRNA }">GeneCards</a>,
							<a target="_blank"
								href="http://www.genenames.org/cgi-bin/search?search_type=symbols&search=hsa-${miRNA }&submit=Submit">HGNC</a>,
							<a target="_blank"
								href="http://www.mirbase.org/cgi-bin/query.pl?terms=hsa-${miRNA }">miBASE</a>
							<fmt:message key="label.searchmore2" />
						</div>
						<hr>
					</c:if>
					<c:if test="${isHistoryResult}">
						<div class="row infoRow" style="text-align: center">
							<fmt:message key="label.historyresult1" />
							<fmt:message key="label.historyresult2" />
							‘${searchGene }’
							<fmt:message key="label.historyresult3" />
							${date}.
						</div>
						<hr>
					</c:if>
					<div class="row paginateWrap">
						<c:if test="${page == 1}">
							<span href="javascript:;" class="inactive paginatebtn"><fmt:message
									key="button.previous" /></span>
						</c:if>
						<c:if test="${page gt 1}">
							<a href="javascript:;" class="paginatebtn"
								onclick="prevPage_miRNA();"><fmt:message
									key="button.previous" /></a>
						</c:if>
						<c:if test="${totalPage == 0 }">
							<a href="javascript:;" class="inactive">1</a>
						</c:if>
						<c:forEach var="aPage" begin="${beginPage}" end="${endPage}">
							<span href="javascript:;"
								<c:if test="${page eq aPage}">class="current paginate"</c:if>
								<c:if test="${page != aPage}">class="paginate miRNA"</c:if>
								title="Go to page ${aPage } of ${totalPage }">${aPage }</span>
						</c:forEach>
						<c:if test="${page == totalPage || totalPage == 0}">
							<span href="javascript:;" class="inactive paginatebtn"><fmt:message
									key="button.next" /></span>
						</c:if>
						<c:if test="${page lt totalPage}">
							<a href="javascript:;" class="paginatebtn"
								onclick="nextPage_miRNA();"><fmt:message key="button.next" /></a>
						</c:if>
					</div>
					<div class="row">
						<input type="hidden" id="sortType" value="${sortType}" />
						<table id="result_detail"
							class="table table-striped table-bordered table-hover table-condensed">
							<tr>
								<th width="8%"><c:if test="${!isHistoryResult}">
										<input class="rankcheckbox" type="checkbox"
											onclick="selectAll(this)" title="" rel="tooltip"
											id="selectAll" data-original-title="Select All">
									</c:if> <fmt:message key="label.rank"/>  &nbsp; <c:if test="${!isHistoryResult}">
										<a style="cursor: pointer;"
											onclick="list_miRNA_BySort(this);"> <i
											<c:if test="${sortType == 'ASC'}">class="icon-arrow-up"</c:if>
											<c:if test="${sortType == 'DESC'}">class="icon-arrow-down"</c:if>></i>
										</a>
									</c:if></th>
								<th width="7%"><fmt:message key="label.datesetid"/></th>
								<th width="10%"><fmt:message key="label.dataType"/></th>
								<th width="11%"><fmt:message key="label.cell"/></th>
								<!--  <th width="8%"><fmt:message key="label.factor"/></th> -->
								<th width="8%"><fmt:message key ="label.decimal"/></th>
								<th width="9%"><fmt:message key="label.percentile"/></th>
								<th width="7%"><fmt:message key="label.study"/></th>
								<th width="7%"><fmt:message key="label.lab"/></th>
								<!-- <th width="8%">TimeStamp</th> -->
								<th width="28%"><fmt:message key="label.moreinfo"/></th>
							</tr>
							<c:if test="${result.sampleItemList[0] == null}">
								<tr>
									<td colspan="11"><fmt:message key="label.noTop1MiRNA"></fmt:message></td>
								</tr>
							</c:if>
							<c:forEach items="${result.sampleItemList}" var="item"
								varStatus="status">
								<tr class="sampleRow">
									<td><c:if test="${!isHistoryResult}">
											<input type="hidden" value="${item.sampleId }" />
											<input class="rankcheckbox" type="checkbox"
												value="${status.count + (page-1)*pages}" name="rowNum"
												class="rank" />
										</c:if> ${status.count + (page-1)*pageSize}</td>
									<td class="sampleId">${item.sampleId }</td>
									<td class="dataType" title="${item.dataType }" nowrap="nowrap"
										style="background:<c:if test="${item.dataType == 'RNA-seq'}">#fdc7c7</c:if>
											<c:if test="${fn:containsIgnoreCase(item.dataType, 'ChIP-seq')}">#fcfeb8</c:if>											<c:if test="${item.dataType == 'CNV' }">#B0E0E6</c:if>
											<c:if test="${item.dataType == 'MethyLation' }">#858EFA</c:if>
											<c:if test="${item.dataType == 'Microarray' }">#8C8D8A</c:if>
											<c:if test="${item.dataType == 'Dnase-seq'}">#A6D377</c:if>
											<c:if test="${item.dataType == 'Somatic Mutations frequency'}">#4292D1</c:if>
											<c:if test="${item.dataType == 'Somatic Mutations normalized frequency'}">#81ADD7</c:if>"><c:if test="${item.dataType == 'Summary Track'}">MIRNA-seq </c:if>${item.dataType }</td>
									<td class="cell" title="${item.cell_desc }" nowrap="nowrap">${item.cell }</td>
									<!-- <td class="factor" title="${item.factor_desc}" nowrap="nowrap">${item.factor}</td> -->
									<td class="orderTotal" title="${item.total }" nowrap="nowrap">${item.rank}/${item.total }</td>
									<td class="percent" title="${item.percentileFormat }"
										nowrap="nowrap">${item.percentileFormat}</td>
									<td class="study" title="${item.study }" nowrap="nowrap"
										style="background:<c:if test="${item.study == 'TCGA'}">#fdc7c7</c:if>
											<c:if test="${item.study == 'ENCODE'}">#fcfeb8</c:if>
											<c:if test="${item.study == 'ICGC'}">#caef90</c:if>	
											<c:if test="${item.study == 'SRA'}">#eedded</c:if>
											<c:if test="${item.study == 'Epigenome Roadmap'}">#d7eaf8</c:if>
											<c:if test="${item.study == 'GEO'}">#B2B2C2</c:if>
											<c:if test="${item.study == 'CCLE'}">#9F80A0</c:if>
											">${item.study }</td>
									<td class="lab" title="${item.lab}">${item.lab}</td>
									<%-- <td class="submitTile" title="${item.timeStamp }">${item.timeStamp }</td> --%>
									<td align="left">
										<ul class="inline">
											<li><input type="hidden" class="data1"
												value="${item.metaData[0]}" /> <input type="hidden"
												class="data2" value="${item.metaData[1]}" /> <a
												onclick="showMetaData(this);" class="btn btn-mini">MetaData</a>
											</li>
											<li><a class="btn btn-mini"
												<c:if test="${item.pubMedUrl != '#'}">target="_blank"</c:if>
												href="${item.pubMedUrl }">PubMed</a></li>
											<li><a class="btn btn-mini" target="_blank"
												href="${item.geoUrl }">GEO</a></li>
											<li><a class="btn btn-mini" href="${item.url }" target="_blank"><i
													class="icon-download-alt"></i>Download</a></li>
											<!--  <li><a class="btn btn-mini" href="#" onclick="javascript:showComment(${item.sampleId });">Comments</a></li>-->
											<c:if test="${item.address != null }">
												<c:if
													test="${item.study == 'ENCODE' || item.study == 'Epigenome Roadmap'}">
													<li><a class="btn btn-mini" target="_blank"
														href="${ctx }/waitForUcsc.htm?url=http://${item.address }:8080/omicseq_ucsc/rest/tracks?chrom=${result.current.seqNameShort }$start=${result.current.start-5000 }$end=${result.current.start+5000 }$serverIp=${item.address }$geneId=${result.geneItemList[0].geneId}${item.ucscUrl }">UCSC</a></li>
												</c:if>
											</c:if>
											<li><a class="btn btn-mini" id="showTop10Gene_${item.sampleId }" href="javascript:;" onclick="showTop10Genes(${item.sampleId },true)" title="" targetDrag="${item.sampleId }_Drag" style="display: block;">Top 10 Gene</a></li>
										</ul>
									</td>
								</tr>
								<div id="${item.sampleId }_Drag" errorMessage="<fmt:message key="error.noimage"/>" style="display:none;width:380px;" class="drag" >
		    						<div class="title">
		        						<h2 style="text-align: left; padding-left: 6px; "><strong>Top 10 Gene</strong></h2>
		        						<div>
		            						<a class="min" href="javascript:;" title="<fmt:message key="placeholder.min"/>" targetoA="showTop5Gene${item.sampleId }"></a>
								            <a class="max" href="javascript:;" title="<fmt:message key="placeholder.max"/>"></a>
								            <a class="revert" href="javascript:;" title="<fmt:message key="placeholder.revert"/>"></a>
								            <a class="closeDialog" href="javascript:;" title="<fmt:message key="button.close"/>" targetoA="showTop5Gene${item.sampleId }"></a>
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
		    						<div class="content" id="content_${item.sampleId }" style="text-align: left;">
		        					</div>
								</div>
							</c:forEach>
						</table>
					</div>
					<c:if test="${not empty result.current && !isHistoryResult}">
						<div class="row" style="margin-bottom: 20px;">
							<div class="span5" id="downloadCurTable"
								error="<fmt:message key="failed.select"/>">
								<a onclick="downloadSelectedMiRNA(this)" class="btn btn-large"><fmt:message
										key="button.downloadselect" /></a>
							</div>
							<div class="span5" id="downloadCurTable">
								<a class="btn btn-large"
									href="${ctx}/export/miRNA_xlsx.htm?miRNAId=${result.current.geneId}&term=${miRNA}"><fmt:message
										key="button.downloadall" /></a>
							</div>
							<div class="span5" id="collect">
								<a onclick="saveHistory();" class="btn btn-large"><fmt:message
										key="button.collection" /></a>
							</div>
						</div>
					</c:if>
				</div>
			</div>
		</div>
	</div>
	<div id="dialog"></div>
	<%@ include file="../common/footer.jsp"%>
	<c:if test="${!isHistoryResult}">
		<div id="ucscDrag" errorMessage="<fmt:message key="error.noimage"/>"
			style="display: none" class="drag" minWidth="800" minHeight="400">
			<div class="title">
				<h2>
					<strong>${result.current.geneSymbol}</strong>,${result.current.txName }(hg19):
					Chr: ${result.current.seqNameShort }, Start: ${result.current.start },
					End: ${result.current.end }, Strand: ${result.current.strand }
				</h2>
				<div>
					<a class="min" href="javascript:;"
						title="<fmt:message key="placeholder.min"/>"
						targetoA="dialogbutton2"></a> <a class="max" href="javascript:;"
						title="<fmt:message key="placeholder.max"/>"></a> <a
						class="revert" href="javascript:;"
						title="<fmt:message key="placeholder.revert"/>"></a> <a
						class="closeDialog" href="javascript:;"
						title="<fmt:message key="button.close"/>" targetoA="dialogbutton2"></a>
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
				<a
					href="http://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position=${result.current.seqName }:${result.current.start }-${result.current.end}"
					target="_blank"> </a>
			</div>
		</div>
		<div id="mainDrag" errorMessage="<fmt:message key="error.noimage"/>"
			style="display: none" class="drag" minWidth="800" minHeight="400">
			<div class="title">
				<h2>
					<strong>${result.current.geneSymbol}</strong>,${result.current.txName }(hg19):
					Chr: ${result.current.seqNameShort }, Start: ${result.current.start },
					End: ${result.current.end }, Strand: ${result.current.strand }
				</h2>
				<div>
					<a class="min" href="javascript:;"
						title="<fmt:message key="placeholder.min"/>"
						targetoA="dialogbutton1"></a> <a class="max" href="javascript:;"
						title="<fmt:message key="placeholder.max"/>"></a> <a
						class="revert" href="javascript:;"
						title="<fmt:message key="placeholder.revert"/>"></a> <a
						class="closeDialog" href="javascript:;"
						title="<fmt:message key="button.close" />"
						targetoA="dialogbutton1"></a>
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
				<a href="javascript:;"> </a>
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