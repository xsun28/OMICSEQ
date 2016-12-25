
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
				showTab('genomicregion');
				if($("#currentGenomicRegion").val()!=''){
					$("#output").attr("style","display: _block");
				}
				
				var value = '${genomicRegion}';
				$("#selectedGenomicRegion").find("option[value='"+value+"']").attr("selected",true);
				
				rootPath = '${ctx}';
				var el = $('.linkWord').last();
				var value = el.html();
				if(!!value) {
					value = value.replace(",","");
					el.html(value);
				}
				
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
			
			function showTop5Genes(sampleId,isFirst){
				rootPath = '${ctx}';
				var size = '10';
				if(!isFirst){
					size = $('#showGeneSize_'+sampleId).val().trim();
				}
				$.ajax({
					url : rootPath + "/showTopGenes.json",
					data : {
						sampleId : sampleId,
						ctx : rootPath,
						size : size
					},
					dataType : 'html',
					type : "post",
					success : function(data){
						$("#content_"+sampleId).html(data);
					}
				});
				var currentDrag = document.getElementById(sampleId+"_Drag");
				initDragDialog(currentDrag, 3);
				$('.drag').not($(currentDrag)).css("z-index","1050");
				$(currentDrag).css("z-index","1060");
				currentDrag.style.display = "block";
			}
			
			function showMoreBtn(target,sampleId){
				var study = $("#study_"+sampleId).val();
				var address = $("#address_"+sampleId).val();
				var $this = $(target);
				var ucscUrl = $("#ucscUrl_"+sampleId).val();
				rootPath = '${ctx}';
				if($this.hasClass('activeMetaData')) {
					//hidden
					//$this.parents('tr').next().remove();
					$("#btnAppend_"+sampleId).remove();
					$this.removeClass('activeMetaData');
					$("#moreBtnDiv_"+sampleId).css("display", "none");
				}else{
					var html = [];
					html.push('<tr style="height:30px;" id="btnAppend_'+sampleId+'">');
					html.push('<td colspan="11"></td>');
					html.push('<td colspan="1">');
					html.push('<ul class="inline">');
					html.push('<li><a class="btn btn-mini" href="javascript:;" onclick="javascript:showComment(' + sampleId + ');">Comments</a></li>');
					html.push('<li><a class="btn btn-mini" id="showTop5Gene'+sampleId+' href="javascript:;" onclick="showTop5Genes('+sampleId+',true)" title="" targetDrag="${item.sampleId }_Drag" style="display: block;">Top 10 Gene</a></li>');
					html.push('</ul>');
					html.push('</td></tr>');
					
					
					var el = $(html.join(''));
					$this.parents('tr').after(el);
					$this.addClass('activeMetaData');
					el.show(200);
					var left = $("#Btntd_"+sampleId).position().left;
					var height = $("#Btntd_"+sampleId).position().top ;
					var tdWid = $("#Btntd_"+sampleId).width();
					var tdHei = $("#Btntd_"+sampleId).height();
					$("#moreBtnDiv_"+sampleId).css("left",left);
					$("#moreBtnDiv_"+sampleId).css("top",height+tdHei+15);
					$("#moreBtnDiv_"+sampleId).css("width",tdWid);
					$("#moreBtnDiv_"+sampleId).css("height",tdHei);
					//$("#moreBtnDiv_"+sampleId).css("display", "block");
				}
			}
			function showRegionList(bedPath){
				rootPath = '${ctx}';
				var url = rootPath + "/showRegionList.json?bedPath=" + bedPath;
				window.open(url);
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
        	<!-- 搜索页面 -->
				<%@ include file="/WEB-INF/views/search.jsp"%>
			</div>
			<div id="output" class="row" style="display: none;">
				<input type="hidden" value="${result.current.seqName }" id="currentSeqName"/>
				<input type="hidden" value="${result.current.start }" id="currentStart"/>
				<input type="hidden" value="${result.current.end }" id="currentEnd"/>
				<input type="hidden" value="${result.current.geneId }" id="currentGeneId" />
				<div id="relateinfo" class="span10 text-center">
					<c:if test="${!isHistoryResult}">
						<div class="row" id="topinfo">
							<fmt:message key="button.search"/> "<strong>
								${genomicRegion }
							</strong>"<fmt:message key="label.find"/> ${totalRecords}
							<fmt:message key="label.result"/>(${result.usedTime } <fmt:message key="label.seconds"/>)
							<div class="row infoRow">
							<a href="javascript:;" onclick="showRegionList('${bedPath}')">Coordinates of regions</a>
						</div>
						</div>
						<c:if test="${geneItemList[1] != null}"> 
							<div class="row linkRow">
								<fmt:message key="label.wildfind"/> 
								<c:forEach items="${geneItemList}" var="geneitem" varStatus="status" begin="0" end="10">
									<c:if test="${geneitem.txName != result.current.txName }">
										<span class="linkWord">${geneitem.txName }<c:if test="${status.count lt geneSize}">,</c:if></span>
									</c:if>
								</c:forEach> 
								<c:if test="${geneItemList.size() > 10}">
									<img  src="${ctx }/static/images/more1.png" onclick="showMoreSingleGene();" title="more.." id="moreImage">
									<div id="moreSingleGene" style="display: none;">
										<c:forEach items="${geneItemList}" var="geneitem" varStatus="status" begin="10" end="${geneItemList.size()-1 }">
											<c:if test="${geneitem.txName != result.current.txName }">
												<span class="linkWord">${geneitem.txName }<c:if test="${status.count lt geneSize}">,</c:if></span>
											</c:if>
										</c:forEach> 
									</div>
								</c:if>
							</div>					
						</c:if> 
						<hr>
					</c:if>
					<div class="row paginateWrap">
						<c:if test="${page == 1}"><span href="javascript:;" class="inactive paginatebtn"><fmt:message key="button.previous"/></span></c:if>
						<c:if test="${page gt 1}"><a href="javascript:;" class="paginatebtn" onclick="prevPage_genomicRegion();"><fmt:message key="button.previous"/></a></c:if>
						<c:if test="${totalPage == 0 }">
							<a href="javascript:;" class="inactive">1</a> 
						</c:if>
						<c:forEach var="aPage" begin="${beginPage}"  end="${endPage}">
							<span href="javascript:;" <c:if test="${page eq aPage}">class="current paginate"</c:if> <c:if test="${page != aPage}">class="paginate genomicRegion"</c:if>
								title="Go to page ${aPage } of ${totalPage }">${aPage }</span> 
						</c:forEach>
						<c:if test="${page == totalPage || totalPage == 0}"><span href="javascript:;" class="inactive paginatebtn"><fmt:message key="button.next"/></span></c:if>
						<c:if test="${page lt totalPage}"><a href="javascript:;" class="paginatebtn" onclick="nextPage_genomicRegion();"><fmt:message key="button.next"/></a></c:if>
					</div>
					
					<div class="row">
						<input type="hidden" id="sortType" value="${sortType}"/>
						<table id="result_detail" class="table table-striped table-bordered table-hover table-condensed">
							<tr>
								<th width="5%">
									<c:if test="${!isHistoryResult}"><input class="rankcheckbox" type="checkbox" onclick="selectAll(this)" title="" rel="tooltip" id="selectAll" data-original-title="Select All"></c:if>
									<fmt:message key="label.rank"/>  &nbsp;
									<c:if test="${!isHistoryResult}">
										<a style="cursor:pointer;" onclick="listBySort(this);">
											<i <c:if test="${sortType == 'ASC'}">class="icon-arrow-up"</c:if>
												<c:if test="${sortType == 'DESC'}">class="icon-arrow-down"</c:if> ></i>
										</a>
									</c:if>
								</th>

								<th width="7%"><fmt:message key="label.datesetid"/></th>
								<th width="9%"><fmt:message key="label.dataType"/></th>
								<th width="10%"><fmt:message key="label.cell"/></th>
								<th width="12%"><fmt:message key="label.factor"/></th>
								<th width="8%"><fmt:message key ="label.decimal1"/></th>
								<th width="8%"><fmt:message key="label.enrichment"/></th>

								<th width="0%">tss5kCount</th>
								<th width="0%">tssTesCount</th>

								<th width="8%"><fmt:message key="label.study"/></th>
								<th width="8%"><fmt:message key="label.lab"/></th>

								<!-- <th width="8%">TimeStamp</th> -->
								<th width="25%"><fmt:message key="label.moreinfo"/></th>
							</tr>
							
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
									<td class="orderTotal" title="${item.total }" nowrap="nowrap">${item.rank}/${item.total }</td>
									<td class="percent" title="${item.percentileFormat }" nowrap="nowrap">${item.percentileFormat}</td>
									<td class="count">${item.tssCount}</td>
									<td class="tescount">${item.tssTesCount}</td>
									<td class="study" title="${item.study }" nowrap="nowrap"
										style="background:<c:if test="${item.study == 'TCGA'}">#fdc7c7</c:if>
											<c:if test="${item.study == 'ENCODE'}">#fcfeb8</c:if>
											<c:if test="${item.study == 'ICGC'}">#caef90</c:if>	
											<c:if test="${item.study == 'SRA'}">#eedded</c:if>
											<c:if test="${item.study == 'Epigenome Roadmap'}">#d7eaf8</c:if>
											<c:if test="${item.study == 'GEO'}">#B2B2C2</c:if>
											<c:if test="${item.study == 'CCLE'}">#9F80A0</c:if>
											<c:if test="${item.study == 'SUMMARY'}">#47D892</c:if>
											"	
									>${item.study }</td>
									<td class="lab" title="${item.lab}">${item.lab}</td>
									<%-- <td class="submitTile" title="${item.timeStamp }">${item.timeStamp }</td> --%>
									<td align="left" id="Btntd_${item.sampleId }">
										<ul class="inline">
											<li>
												<input type="hidden" class="data1" value="${item.metaData[0]}"/>
												<input type="hidden" class="data2" value="${item.metaData[1]}"/>
												<a onclick="showMetaData(this,${item.sampleId});" class="btn btn-mini">MetaData</a>
											</li>
											<li><a class="btn btn-mini" <c:if test="${item.pubMedUrl != '#'}">target="_blank"</c:if> href="${item.pubMedUrl }">PubMed</a></li>
											<li><a class="btn btn-mini" target="_blank" href="${item.geoUrl }">GEO</a></li>
											<li><a class="btn btn-mini" href="${item.url }" target="_blank"><i class="icon-download-alt"></i>Download</a></li>
											<li><a class="btn btn-mini" id="showBtn${item.sampleId }" href="javascript:;" onclick="showMoreBtn(this,${item.sampleId})" title="" targetDrag="${item.sampleId }_Drag" style="display: block;">more.</a></li>
										</ul>
									</td>
								</tr>
								<input type="hidden" value="${item.address }" id="address_${item.sampleId}"/>
								<input type="hidden" value="${item.study }" id="study_${item.sampleId}"/>
								<div id="${item.sampleId }_Drag" errorMessage="<fmt:message key="error.noimage"/>" style="display:none;width:600px;" class="drag" >
		    						<div class="title">
		        						<h2 style="text-align: left; padding-left: 6px; "><strong>Top ${showGeneSize} Gene</strong></h2>
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
					<div class="row" style="margin-bottom:20px;">
				      	<div class="span5" id="downloadCurTable" error="<fmt:message key="failed.select"/>">
				      		<a onclick="downloadSelected(this)" class="btn btn-large"><fmt:message key="button.downloadselect"/></a>
				      	</div>
				      	<div class="span5" id="downloadCurTable">
				      		<a class="btn btn-large" href="${ctx}/export/xlsx.htm?geneId=${result.current.geneId}&term=${geneSymbol}"><fmt:message key="button.downloadall"/></a>
				      	</div>
				      	<div class="span5" id="collect">
				      		<a onclick="saveHistory();" class="btn btn-large"><fmt:message key="button.collection"/></a>
				      	</div>
				    </div>
					</c:if>
				</div>
			</div>
      </div>
    </div>
    <%@ include file="../common/footer.jsp"%>
    <c:if test="${!isHistoryResult}">
	    <div id="ucscDrag" errorMessage="<fmt:message key="error.noimage"/>" style="display:none;width:800px;" class="drag" minWidth="800" minHeight="400">
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
		<div id="mainDrag" errorMessage="<fmt:message key="error.noimage"/>" style="display:none;width:800px;" class="drag" minWidth="800" minHeight="400">
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
		<div id="dialog"></div>
		<div id="diagramDrag" errorMessage="<fmt:message key="error.noimage"/>" style="display:none;width:600px;" class="drag">
			<div class="title">
			 <h2><strong>${result.current.geneSymbol}</strong>,${result.current.txName }(hg19): Chr: ${result.current.seqNameShort }, Start: ${result.current.start }, End: ${result.current.end }, Strand: ${result.current.strand }</h2>
				<div>
		            <a class="min" href="javascript:;" title="<fmt:message key="placeholder.min"/>" targetoA="dialogbutton3"></a>
		            <a class="max" href="javascript:;" title="<fmt:message key="placeholder.max"/>"></a>
		            <a class="revert" href="javascript:;" title="<fmt:message key="placeholder.revert"/>"></a>
		            <a class="closeDialog" href="javascript:;" title="<fmt:message key="button.close" />" targetoA="dialogbutton3"></a>
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
		    	 <img id="chart" alt="" src="" width="420" height="306" border="0" >
		    </div>    
		</div>
		<div id="WordCloudDrag_Factor" errorMessage="<fmt:message key="error.noimage"/>" style="display:none;width:600px;" class="drag">
		    <div class="title">
		        <h2><strong>${result.current.geneSymbol}</strong>,${result.current.txName }(hg19): Chr: ${result.current.seqNameShort }, Start: ${result.current.start }, End: ${result.current.end }, Strand: ${result.current.strand }</h2>
		        <div>
		            <a class="min" href="javascript:;" title="<fmt:message key="placeholder.min"/>" targetoA="dialogbutton4"></a>
		            <a class="max" href="javascript:;" title="<fmt:message key="placeholder.max"/>"></a>
		            <a class="revert" href="javascript:;" title="<fmt:message key="placeholder.revert"/>"></a>
		            <a class="closeDialog" href="javascript:;" title="<fmt:message key="button.close"/>" targetoA="dialogbutton4"></a>
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
		<div id="WordCloudDrag_CellType" errorMessage="<fmt:message key="error.noimage"/>" style="display:none;width:600px;" class="drag">
		    <div class="title">
		        <h2><strong>${result.current.geneSymbol}</strong>,${result.current.txName }(hg19): Chr: ${result.current.seqNameShort }, Start: ${result.current.start }, End: ${result.current.end }, Strand: ${result.current.strand }</h2>
		        <div>
		            <a class="min" href="javascript:;" title="<fmt:message key="placeholder.min"/>" targetoA="dialogbutton5"></a>
		            <a class="max" href="javascript:;" title="<fmt:message key="placeholder.max"/>"></a>
		            <a class="revert" href="javascript:;" title="<fmt:message key="placeholder.revert"/>"></a>
		            <a class="closeDialog" href="javascript:;" title="<fmt:message key="button.close"/>" targetoA="dialogbutton5"></a>
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
	</body>
</html>