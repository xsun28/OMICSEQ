<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/tags.jsp"%>
<script type="text/javascript">
$(document).ready(function(){
	var rootPath = '${pageContext.request.contextPath}';
	$("#searchForm").autocomplete({ 
		source: function(request,response){
			$("#ui-id-1").css("left", $("#searchForm").position().left - 3);
        	$("#ui-id-1").css("top", $("#searchForm").position().top + $("#searchForm").outerHeight(true));
        	$("#ui-id-1 li").remove();
        	$("#ui-id-1").append("<li><p><center><br>one moment, searching......</center></p></li>");
        	$("#ui-id-1").css("display", "block");
			$("#ui-id-1").mouseleave(function(e){
				$("#select_info").css("display", "none");
			});
			$.ajax({
				url : rootPath + "/autocomplete.json",
				type : "post",
				data : {
					term:request.term
				},
				dataType : "json",
				success : function(data){
					$("#wait_div").css("display", "none");
					var hasData = false;
					response( $.map( data, function( item ) {
						hasData = true;
                        return {
                            value: item.show,
                            geneSymbol:item.geneSymbol,
                            start:item.start,
                            refseq:item.refseq,
                            end:item.end,
                            strand: item.strand,
                            seqName: item.seqName
                        };
                    }));
				if(!hasData){
					$("#ui-id-1 li").remove();
					$("#ui-id-1").append("<li><p><center>No Result</center></p></li>");
					$("#ui-id-1").css("display", "block");
				} 
				$("#ui-id-1").css("left", $("#searchForm").position().left - 3); 
				$("#ui-id-1").css("width", 239);
				}
			});
		},
		focus:function(event,ui){
			var top = event.pageY;
			var left =  parseInt($("#ui-id-1").css("left").split("px")[0]) + parseInt($("#ui-id-1").css("width").split("px")[0]) + 5;
			$("#select_info").css("display", "block");
			$("#select_info").css("top", top);
			$("#select_info").css("left", left);
			$("#select_info").css("width", 200);
			$("#select_info").css("background-color", "#ffffff");
			$("#select_info").html("");
			$("#select_info").append("<br><br><center>");
			$("#select_info").append("<p><centet>one moment, loading......</center></p>");
			$("#select_info").mouseover(function(){
				$("#select_info").css("display", "block");
			});
			$("#select_info").mouseout(function(){
				$("#select_info").css("display", "none");
			});
			$("#select_info").html("");
			$("#select_info").append("<div id=\"gene_info_name\"><b>"+ui.item.geneSymbol+"</b></div>");
			$("#select_info").append("<table id=\"gene_info_table\"></table>");
    		$("#gene_info_table").append("<tr><td align=\"left\">"+"Refseq: </td><td align=\"left\">"+ui.item.refseq+"</td></tr>");
    		$("#gene_info_table").append("<tr><td align=\"left\">"+"SeqName: </td><td align=\"left\">"+ui.item.seqName+"</td></tr>");
    		$("#gene_info_table").append("<tr><td align=\"left\">"+"Start: </td><td align=\"left\">"+ui.item.start+"</td></tr>");
    		$("#gene_info_table").append("<tr><td align=\"left\">"+"End: </td><td align=\"left\">"+ui.item.end+"</td></tr>");
    		$("#gene_info_table").append("<tr><td align=\"left\">"+"Strand: </td><td align=\"left\">"+ui.item.strand+"</td></tr>");
			return false;
		},
		select:function(event,ui){
			$("#searchForm").value = ui.item.value;
			var geneSymbol = ui.item.value;
			if(geneSymbol == '') {
				 $('#searchForm').focus();
				 return;
			}
			var sortType = $('#sortType').val();
			if(!!!sortType) {
				sortType = "ASC";
			}
			var url = rootPath + "/testpages/result.htm?page=1&geneSymbol=" + geneSymbol + "&sort=" + sortType + "&isLink=false&isHistory=false";
			$('#currentGeneSymbol').val(geneSymbol);
			window.location.href = url;
		}
	}); 
	$("#searchForm_pathWay").autocomplete({ 
		source: rootPath + "/autocomplete_pathWay.json"
	}); 
	
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
	$("#searchForm_multigene").bind("keydown",function(event){
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

function highlightTab(id)
{
	
}
</script>
<div class="tabbable" id="searcharea">
	<input type="hidden" id="isLink" value="${isLink}"/> 
	<input type="hidden" id="isHistory" value="${isHistory}"/> 
	<input type="hidden" id="isHistoryResult" value="${isHistoryResult}"/>
	<p style="text-align:center;"><img id="search_logo" src="${ctx}/static/images/banner.png" style="width: 240px;height: 60px;"></p>
	<ul class="nav nav-tabs" style="margin: auto auto 20px;width: 70%;">
		<li class="choice active" id="gene"><a onclick="highlightTab('gene')"><fmt:message key="submenu.gene"/></a></li>
		<li class="disabled"><a onclick="highlightTab('multigene')"><fmt:message key="submenu.pathway"/></a></li>
		<li class="disabled"><a onclick="highlightTab('multigene')"><fmt:message key="submenu.multigene"></fmt:message></a></li>
		<li class="disabled"><a onclick="highlightTab('genomicregion')"><fmt:message key="submenu.genomic"></fmt:message></a></li>
		<li class="disabled"><a onclick="highlightTab('dataDownload')"><fmt:message key="submenu.dataDownload"></fmt:message></a></li>
	</ul>				 		
	<div class="tab-content" id="tab_content_gene">
		<div class="tab-pane active text-center row-fluid">
			<div class="control-group">
				<div class="input-prepend input-append" id="searchbar">
					<span class="input-group-addon">
				        <!-- <input name="version" id="version18" value="18" type="radio"><span>hg18</span> -->
				        <input name="version" id="version19" value="19" checked="checked" type="radio"><span>hg19</span>
				    </span>
					<input type="hidden" id="currentGeneSymbol" value="">
					<input type="hidden" id="currentGeneLink" value="">
					<input id="searchForm" value="" class="input-large search-query ac_input" autocomplete="off" placeholder="<fmt:message key="placeholder.keywords"/>" required="" type="text">
					<div class="btn-group">
						<!-- <a class="btn btn-success" id="geneSearch" onclick="goSearch_test();"><i class="icon-search icon-white"></i>&nbsp;<fmt:message key="button.search"/></a> -->
						<a class="btn btn-success" id="geneSearch"  href="${ctx}/static/html/geneDemo.html" target="_blank"><i class="icon-search icon-white"></i>&nbsp;<fmt:message key="button.search"/></a>
						<a class="btn btn-primary" href="#setting" data-toggle="modal"><i class="icon-cog icon-white"></i>&nbsp;<fmt:message key="button.setting"/></a>
					</div>
				</div>
				<span id="diedai" ></span>
			</div>
		</div>
	</div>	
<!-- pathway tab start-->
	<div class="tab-content" id="tab_content_pathway" style="display: none">
		<div class="tab-pane active text-center row-fluid">
			<div class="control-group">
				<div class="input-prepend input-append" id="searchbar">
					<span class="input-group-addon">
				        <!-- <input name="version" id="version18" value="18" type="radio"><span>hg18</span> -->
				        <input value="19" checked="checked" type="radio"><span>hg19</span>
				    </span>
				    <input type="hidden" id="currentPathwayName" value="">
					<input id="searchForm_pathWay" value="" class="input-large search-query ac_input" autocomplete="off" placeholder="<fmt:message key="placeholder.keywords"/>" required="" type="text">
					<div class="btn-group">
						<!-- <a class="btn btn-success" id="geneSearch_pathWay" onclick="goSearchPathWay();"><i class="icon-search icon-white"></i>&nbsp;<fmt:message key="button.search"/></a> -->
						<a class="btn btn-success" id="geneSearch_pathWay" href="${ctx}/static/html/test.html" target="_blank"><i class="icon-search icon-white"></i>&nbsp;<fmt:message key="button.search"/></a>
						<a class="btn btn-primary" href="#setting" data-toggle="modal"><i class="icon-cog icon-white"></i>&nbsp;<fmt:message key="button.setting"/></a>
					</div>
				</div>
			</div>
		</div>
	</div>	
	<!-- pathway tab end-->
<!-- multigene tab start-->
	<div class="tab-content" id="tab_content_multigene" style="display: none">
		<div class="tab-pane active text-center row-fluid">
			<div class="control-group">
				<div class="input-prepend input-append" id="searchbar">
					<span class="input-group-addon">
				        <!-- <input name="version" id="version18" value="18" type="radio"><span>hg18</span> -->
				        <input value="19" checked="checked" type="radio"/><span>hg19</span>
				    </span>
					<input type="hidden" id="currentMultigene" value="">
					<!--  <input id="searchForm_multigene" value="${multigene }" class="input-large search-query ac_input" autocomplete="off" placeholder="<fmt:message key="placeholder.keywords"/>" required="" type="text">-->
					<span style="font-size:14px"><textarea cols="30" rows="6" id="searchForm_multigene" style="resize:none;"></textarea></span>
					<div class="btn-group">
						<a class="btn btn-success" id="geneSearch_multigene" onclick="goSearchMultigene();"><i class="icon-search icon-white"></i>&nbsp;<fmt:message key="button.search"/></a>
						<a class="btn btn-primary" href="#setting" data-toggle="modal"><i class="icon-cog icon-white"></i>&nbsp;<fmt:message key="button.setting"/></a>
					</div>
				</div>
				<span id="diedai" ></span>
			</div>
		</div>
	</div>	
<!-- multigene tab end-->
	
<!-- Genomic Region tab start-->
	<div class="tab-content" id="tab_content_genomicregion" style="display: none;">
		<div class="tab-pane active text-center row-fluid">
			<div class="control-group">
				<div class="input-prepend input-append" id="searchbar">
					<span class="input-group-addon">
				        <!-- <input name="version" id="version18" value="18" type="radio"><span>hg18</span> -->
				        <input value="19" checked="checked" type="radio"><span>hg19</span>
				    </span>
					<input type="hidden" id="currentMultigene" value="">
					<span style="font-size: 14px;">chr:</span><input id="searchForm_chr" class="input-small search-query ac_input"  placeholder="<fmt:message key="placeholder.keywords"/>" required="required" type="text">
					<span style="font-size: 14px;">start:</span><input id="searchForm_start" class="input-small search-query ac_input"  placeholder="<fmt:message key="placeholder.keywords"/>" required="required" type="text">
					<span style="font-size: 14px;">end:</span><input id="searchForm_end" class="input-small search-query ac_input"  placeholder="<fmt:message key="placeholder.keywords"/>" required="required" type="text">
					<div class="btn-group">
						<a class="btn btn-success" id="geneSearch_genomicregion" onclick="goSearchGenomicRegion();"><i class="icon-search icon-white"></i>&nbsp;<fmt:message key="button.search"/></a>
						<a class="btn btn-primary" href="#setting" data-toggle="modal"><i class="icon-cog icon-white"></i>&nbsp;<fmt:message key="button.setting"/></a>
					</div>
				</div>
				<span id="diedai" ></span>
			</div>
		</div>
	</div>	
	<!-- Genomic Region tab end-->
	
	<!-- data download tab start -->
	<div class="tab-content" id="tab-content_dataDownload" style="display: none;height: auto;">
		<div class="control-group" style="margin:auto 250px auto 250px;">
			<div style="float:left; margin-left: 100px;">
				<h3>
					<label for="geneSymbols"><fmt:message key="label.gene"></fmt:message><span style="color: red;size: 8px;">*</span>:</label>
				</h3>
					<span style="font-size:14px"><textarea cols="20" rows="7" id="searchForm_geneSymbols" style="resize:none;">${multigene }</textarea></span>
				</div>
				<div style="float:left;margin-left: 100px;"><h3>
					<label for="cell" class="control-label"><fmt:message key="label.cell"></fmt:message>:</label>
				</h3>
					<input type="text" id="cell">
				<h3>
					<label for="factor" class="control-label" style="margin-top:30px;"><fmt:message key="label.factor"></fmt:message>:</label>
				</h3>
					<input type="text" id="factor"></div>
					<br>
				<div style="float:left;">
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
					<label for="downloadColumn" class="control-label" >downloadColumn:</label>
				</h3>
				<div class="controls" style="width: 100%; margin-right:600px;">
					<c:forEach	items="${columns }" var="column">
						<div class="selectItem">
							<input type="checkbox" name="dColumn" value="${column}" checked="checked"/>
							<label>${column}</label>
						</div>
					</c:forEach>
					<div style="text-align: center;">
						<button class="btn btn-success" onclick="downloadData();" ><fmt:message key="label.download"></fmt:message></button>
					</div>
				</div>
				</div>
			</div>
		  </div>
	<!-- data download tab end -->	
</div>

<!-- Setting Modal -->
<div id="setting" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabelaaa" aria-hidden="true">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3><fmt:message key="label.setting"/></h3>
	</div>
	<div class="modal-body">
		<div class="control-group" style="height:50px;">
			<label for="experiment" class="control-label"><fmt:message key="label.Experiments"/>:</label>
			<div class="controls" style="float: left;width: 100%;">
				<c:forEach items="${settingDTO.experimentsMap}" var="experiment">
					<div class="settingItem">
						<input type="checkbox" value="${experiment.key}" name="experiments" <c:if test="${experiment.value == 'true'}">checked="checked"</c:if>/>
						<label>${experiment.key}
							<c:forEach items="${sampleSumMap}" var="sampleSum">
								<c:if test="${sampleSum.key == experiment.key}">(${sampleSum.value})</c:if>
							</c:forEach>
						</label>
					</div>
				</c:forEach>
			</div>				 
	  	</div>	
		<div class="control-group">
			<label for="source" class="control-label"><fmt:message key="label.source"/>:</label>
			<div class="controls">
				<c:forEach items="${settingDTO.sourcesMap}" var="source">
					<div class="settingItem">
						<input type="checkbox" value="${source.key}" name="sources" <c:if test="${source.value == 'true'}">checked="checked"</c:if>/>
						<label>${source.key}
							<c:forEach items="${sampleSumMap}" var="sampleSum">
								<c:if test="${sampleSum.key == source.key}">(${sampleSum.value})</c:if>
							</c:forEach>
						</label>
					</div>
				</c:forEach>
			</div>				 
	  	</div>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true"><fmt:message key="button.close"/></button>
		<button class="btn btn-primary" data-dismiss="modal" aria-hidden="true" onclick="changeSettings()"><fmt:message key="button.save"/></button>
	</div>
</div>
	<div id="select_info" class="ui-front ui-corner-all" style="display:none; position: absolute; border: 1px #ccc solid"> </div>

