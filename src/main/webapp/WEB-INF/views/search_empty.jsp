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
			var genome = $("#genome").val();
			if(genome == 'Human'){
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
			}
			else if(genome == 'Mouse'){
				$.ajax({
					url : rootPath + "/autocomplete_Mouse.json",
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
			}
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
			var genome = $("#genome").val().trim();
			var geneSymbol = ui.item.value;
			if(geneSymbol == '') {
				 $('#searchForm').focus();
				 return;
			}
			var sortType = $('#sortType').val();
			if(!!!sortType) {
				sortType = "ASC";
			}
			var url = rootPath + "/result.htm?page=1&geneSymbol=" + geneSymbol + "&sort=" + sortType + "&genome=" + genome + "&isLink=false&isHistory=false";
			$('#currentGeneSymbol').val(geneSymbol);
			window.location.href = url;
		}
	}); 
	
	$("#searchForm_miRNA").autocomplete({ 
		source: rootPath + "/autocomplete_miRNA.json"
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
			},
			error:function(){
				//alert("some errror");
			}
		});	
}
function hiddenNames() {
	$("#ad_hengfu_temp").css('display','none');
}
</script>
<div class="tabbable" id="searcharea">
	<input type="hidden" id="isLink" value="${isLink}"/> 
	<input type="hidden" id="isHistory" value="${isHistory}"/> 
	<input type="hidden" id="isHistoryResult" value="${isHistoryResult}"/>
	<p style="text-align:center;"><img id="search_logo" src="${ctx}/static/images/banner.png" style="width: 240px;height: 60px;"></p>
  <ul class="nav nav-tabs" style="margin: auto auto 20px;width: 70%;">
		<li class="choice active" id="gene"><a onclick="highlightTab('gene',false)"><fmt:message key="submenu.gene"/></a></li>
		<!-- <li class="disabled" id="miRNA"><a onclick="highlightTab('miRNA',false)">miRNA</a></li> -->
		<li class="disabled" id="pathway"><a onclick="highlightTab('pathway',false)"><fmt:message key="submenu.pathway"></fmt:message></a></li>
		<%-- <li class="disabled" id="multigene"><a onclick="highlightTab('multigene',false)"><fmt:message key="submenu.multigene"></fmt:message></a></li>
		<li class="disabled" id="genomicregion"><a onclick="highlightTab('genomicregion',false)"><fmt:message key="submenu.genomic"></fmt:message></a></li>
		<li class="disabled" id="datasetSearch"><a onclick="highlightTab('datasetSearch',false)"><fmt:message key="submenu.datasetSearch"></fmt:message></a></li>
		<li class="disabled" id="diseasesRank"><a onclick="highlightTab('diseasesRank',false)"><fmt:message key="submenu.diseasesRank"></fmt:message></a></li>
		<li class="disabled" id="variation"><a onclick="highlightTab('variation',false)"><fmt:message key="submenu.variation"></fmt:message></a></li>
		<li class="disabled" id="multiVariants"><a onclick="highlightTab('multiVariants',false)"><fmt:message key="submenu.multiVariants"></fmt:message></a></li>
	
		<li class="disabled" id="dataDownload"><a onclick="highlightTab('dataDownload')"><fmt:message key="submenu.dataDownload"></fmt:message></a></li>
		  --%>
	</ul> 		
<!--  Gene start -->
	<div class="tab-content" id="tab_content_gene">
		<div class="tab-pane active text-center row-fluid">
			<div class="control-group">
				<div class="input-prepend input-append" id="searchbar">
					<!-- <span class="input-group-addon">
				        <input name="version" id="version18" value="18" type="radio"><span>hg18</span>
				        <input value="19" checked="checked" type="radio"><span>hg19</span>
				    </span>-->
					<input type="hidden" id="currentGeneSymbol" value="">
					<input type="hidden" id="currentGeneLink" value="">
					<select id="genome" class="smallSelect">
						<option value="Human">Human</option>
						<option value="Mouse">Mouse</option>
					</select>
					<input id="searchForm" value="" class="input-large search-query ac_input" autocomplete="off" placeholder="<fmt:message key="placeholder.keywords"/>" required="" type="text">
					<div class="btn-group">
						<a class="btn btn-success" id="geneSearch" onclick="goSearch();"><i class="icon-search icon-white"></i>&nbsp;<fmt:message key="button.search"/></a>
						<a class="btn btn-primary" href="#setting" data-toggle="modal"><i class="icon-cog icon-white"></i>&nbsp;<fmt:message key="button.setting"/></a>
					</div>
				</div>
				<span id="diedai" ></span>
			</div>
		</div>
	</div>	
<!-- Gene end -->
		 		
<!-- Gene Advanced start -->
	<div class="tab-content" id="tab_content_geneAdvanced" style="display: none;">
		<div class="tab-pane active row-fluid">
			<div class="control-group">
				<div class="input-prepend input-append" id="searchbar" style="margin: 0 30% ;">
					<input type="hidden" id="currentGeneSymbol" value="${geneSymbol}">
					<input type="hidden" id="currentGeneLink" value="${geneSymbol }">
					<input type="hidden" id="currentGeneCell" value="${geneCell }">
					<input type="hidden" id="currentGeneDetail" value="${geneDetail }">
					<table border="0px;">
						<tr>
							<td style="text-align: right; ">
								<span class="input-group-addon"><input value="19" checked="checked" type="radio"><span>hg19</span></span>
								<span style="font-size: 14px;"><fmt:message key="label.gene"></fmt:message>:</span>
							</td>
							<td>
								<input id="searchForm_advanced" value="${geneSymbol }" class="input-large search-query ac_input" autocomplete="off" placeholder="<fmt:message key="placeholder.keywords"/>" required="" type="text">
								<div class="btn-group">
									<a class="btn btn-success" id="geneSearchAdvanced" onclick="geneSearchAdvanced();"><i class="icon-search icon-white"></i>&nbsp;<fmt:message key="button.search"/></a>
								</div>
							</td>
						</tr>
						<tr style="margin-top: 5px; ">
							<td style="text-align: right; ">
								<span style="font-size: 14px;"><fmt:message key="label.cell"></fmt:message>:</span>
							</td>
							<td>
								<input id="searchForm_cell" value="${geneCell }"  class="input-large search-query ac_input" autocomplete="off" placeholder="<fmt:message key="placeholder.keywords"/>" required="" type="text">
							</td>
						</tr>
						<tr style="margin-top: 5px; "> 
							<td style="text-align: right; ">
								<span style="font-size: 14px;"><fmt:message key="label.factor"></fmt:message>:</span>
							</td>
							<td>
								<input id="searchForm_detail" value="${geneDetail }" class="input-large search-query ac_input" autocomplete="off" placeholder="<fmt:message key="placeholder.keywords"/>" required="" type="text">
							</td>
						</tr>
						<tr style="margin-top: 5px; display: none" id="se_img">
							<td colspan="2" style="text-align: center;"><a target="_self" href="javascript:void(0);" onclick="hiddenNames();"><img id="expandImg" title="Expand" src="${ctx}/static/images/shrink.png"/></a></td>
						</tr>
						<tr style="margin-top: 5px; display: _block" id="se_etype">
							<td style="text-align: right;" valign="top">
								<label for="experiment" class="control-label"><fmt:message key="label.Experiments"/>:</label>
							</td>
							<td>
								<div class="controls">
									<div class="settingItem">
									<input type="checkbox" id="exp_checkbox" checked="checked"/><label style="font-weight:bold"><fmt:message key="label.checkAll"/></label>
									</div>
									<div class="settingItem">&nbsp;</div>
								<c:forEach items="${experimentsMap}" var="experiment">
									<div class="settingItem">
										<input type="checkbox" value="${experiment.key}" name="experiments_advanced" <c:if test="${experiment.value == 'true' }">checked="checked"</c:if>/>
										<label>${experiment.key}
										<c:forEach items="${sampleSumMap}" var="sampleSum">
											<c:if test="${sampleSum.key == experiment.key}">(${sampleSum.value})</c:if>
										</c:forEach>
										</label>
									</div>
								</c:forEach>
							</div>
							</td>
						</tr>
						<tr style="margin-top: 5px; display: _block" id="se_source">
							<td style="text-align: right;" valign="top">
								<label for="source" class="control-label"><fmt:message key="label.source"/>:</label>
							</td>
							<td style="margin-top: 5px;">
								<div class="settingItem">
									<input type="checkbox" id="source_checkbox" checked="checked"/><label style="font-weight:bold"><fmt:message key="label.checkAll"/></label>
									</div>
								<div class="settingItem">&nbsp;</div>
								<c:forEach items="${sourcesMap}" var="source">
									<div class="settingItem">
										<input type="checkbox" value="${source.key}" name="sources_advanced" <c:if test="${source.value == 'true' }">checked="checked"</c:if>/>
										<label>${source.key}
											<c:forEach items="${sampleSumMap}" var="sampleSum">
												<c:if test="${sampleSum.key == source.key}">(${sampleSum.value})</c:if>
											</c:forEach>
										</label>
									</div>
								</c:forEach>
							</td>
						</tr>
					</table>
				</div>
			</div>
		</div>
	</div>	
<!-- Gene Advanced end -->	
	
<!-- miRNA start -->
<div class="tab-content" id="tab_content_miRNA" style="display:none;">
		<div class="tab-pane active text-center row-fluid">
			<div class="control-group">
				<div class="input-prepend input-append" id="searchbar">
					<span class="input-group-addon">
				        <!-- <input name="version" id="version18" value="18" type="radio"><span>hg18</span> -->
				        <input value="18" checked="checked" type="radio"><span>hg18</span>
				    </span>
					<input type="hidden" id="currentmiRNA" value="${miRNA}">
					<input id="searchForm_miRNA" value="hsa-${miRNA }" class="input-large search-query ac_input" autocomplete="off" placeholder="<fmt:message key="placeholder.keywords"/>" required="" type="text">
					<div class="btn-group">
						<a class="btn btn-success" id="miRNASearch" onclick="goSearch_miRNA();"><i class="icon-search icon-white"></i>&nbsp;<fmt:message key="button.search"/></a>
						<!--  
						<a class="btn btn-primary" href="#setting" data-toggle="modal"><i class="icon-cog icon-white"></i>&nbsp;<fmt:message key="button.setting"/></a>
						-->
					</div>
				</div>
				<span id="diedai" ></span>
			</div>
		</div>
	</div>
<!-- miRNA end -->	
<!-- 三种查询方式 -->
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
						<a class="btn btn-success" id="geneSearch_pathWay" onclick="goSearchPathWay();"><i class="icon-search icon-white"></i>&nbsp;<fmt:message key="button.search"/></a>
						<a class="btn btn-primary" href="#setting" data-toggle="modal"><i class="icon-cog icon-white"></i>&nbsp;<fmt:message key="button.setting"/></a>
					</div>
				</div>
			</div>
		</div>
		<div class="find fn-clear" id="topFixedNav-letter">
                        <div class="find-letter  fn-left">
                            <span class="fn-left">Find By <span class="red">Name's </span>First Character：</span>
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
                                        
                                        <li><a target="_self" href="javascript:void(0);" onclick="hiddenNames();"><img title="Hidden" src="${ctx}/static/images/expand.png"/></a></li>
                                    </ul>
                        </div>
                    </div>
                    <div id="ad_hengfu_temp">
                        	<ul class="rank-list-ul" id="pathwaysOfA">
                        		
                        	</ul>
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
				        <input value="19" checked="checked" type="radio"><span>hg19</span>
				    </span>
					<input type="hidden" id="currentMultigene" value="${multigene}">
					<!--  <input id="searchForm_multigene" value="${multigene }" class="input-large search-query ac_input" autocomplete="off" placeholder="<fmt:message key="placeholder.keywords"/>" required="" type="text">-->
					<span style="font-size:14px"><textarea cols="30" rows="6" id="searchForm_multigene" style="resize:none;">${multigene }</textarea></span>
					<div class="btn-group">
						<a class="btn btn-success" id="geneSearch_multigene" onclick="goSearchMultigene();"><i class="icon-search icon-white"></i>&nbsp;<fmt:message key="button.search"/></a>
						<a class="btn btn-primary" href="#setting" data-toggle="modal"><i class="icon-cog icon-white"></i>&nbsp;<fmt:message key="button.setting"/></a>
					</div>
				</div>
			</div>
		</div>
	</div>	
<!-- multigene tab end-->

<!-- datasetSearch  tab start-->
<div class="tab-content" id="tab_content_datasetSearch" style="display: none;">
		<div class="tab-pane active text-center row-fluid">
			<div class="control-group">
				<div class="input-prepend input-append" id="searchbar">
					<input type="hidden" id="currentCell" value="${cell}">
					<input type="hidden" id="currentFactor" value="${factor }">
					
					<!-- 搜索框 -->
					<span class="input-group-addon"><fmt:message key="label.cell"></fmt:message>:</span>
					<input id="searchForm_datasetSearch_cell" value="${cell }" class="input-large search-query ac_input" autocomplete="off" placeholder="<fmt:message key="placeholder.keywords"/>" required="" type="text">
					<span class="input-group-addon" style="font-size: 14px;"><fmt:message key="label.factor"></fmt:message>:</span>
					<input id="searchForm_datasetSearch_factor" value="${factor }" class="input-large search-query ac_input" autocomplete="off" placeholder="<fmt:message key="placeholder.keywords"/>" required="" type="text">
					<div class="btn-group">
						<a class="btn btn-success" id="geneSearch_datasetSearch" onclick="goSearch_datasetSearch();"><i class="icon-search icon-white"></i>&nbsp;<fmt:message key="button.search"/></a>
						<a class="btn btn-primary" href="#setting" data-toggle="modal"><i class="icon-cog icon-white"></i>&nbsp;<fmt:message key="button.setting"/></a>
					</div>
				</div>
				<span id="diedai" ></span>
			</div>
		</div>
	</div>
<!-- datasetSearch  tab end -->
	
<!-- Genomic Region tab start-->   
	<div class="tab-content" id="tab_content_genomicregion" style="display: none;">
		<div class="tab-pane active text-center row-fluid">
			<div class="control-group">
				<div class="input-prepend input-append" id="searchbar">
					<span class="input-group-addon">
				        <input value="19" checked="checked" type="radio"><span>hg19</span>
				    </span>
					<input type="hidden" id="currentGenomicRegion" value="${genomicRegion}">
					 <select id="selectedGenomicRegion" name="selectedGenomicRegion" class="search-query ac_input">
					 	<option value="promoters">promoters</option>
					 	<option value="H1">RFECS predicted enhancers in H1 cells</option>
					 	<option value="IMR90">RFECS predicted enhancers in IMR90 cells</option>
					 	<option value="vista.pos">VISTA enhancers positive</option>
					 	<option value="vista.neg">VISTA enhancers negative</option>
					 	<option value="cpghg19">cpghg19_combined</option>
					 	<option value="H2171">H2171_Enhancers_combined</option>	
					 	<option value="MM1S">MM1S_Enhancers_combined</option>
					 	<option value="Super_H2171">Super_H2171_Enhancers_combined</option>
					 	<option value="Super_MM1S">Super_MM1S_Enhancers_combined</option>
					 	<option value="Super_U87">Super_U87_Enhancers_combined</option>
					 	<option value="U87">U87_Enhancers_combined</option>
					 </select>
					<div class="btn-group">
						<a class="btn btn-success" id="geneSearch_genomicregion" onclick="goSearchGenomicRegion();"><i class="icon-search icon-white"></i>&nbsp;<fmt:message key="button.search"/></a>
					</div>
				</div>
			</div>
		</div>
	</div>	

	<!-- Genomic Region tab end-->
	
	<!-- diseasesRank tab start-->
	<div class="tab-content" id="tab_content_diseasesRank" style="display: none">
		<div class="tab-pane active text-center row-fluid">
			<div class="control-group">
				<div class="input-prepend input-append" id="searchbar">
				    <input type="hidden" id="currentDiseasesType" value="${currentCancerType }">
				    <span class="input-group-addon">
				       Cancer Type:
				    </span>
				    <select id="diseasesType" name="diseasesType" class="search-query ac_input">
				    	<option value="PRAD"
	                            >PRAD
	                        - Prostate adenocarcinoma
	                    </option>
				    	<option value="ACC"
                            >ACC
	                        - Adrenocortical carcinoma
	                    </option>
	                    
	                    <option value="BLCA"
	                            >BLCA
	                        - Bladder Urothelial Carcinoma
	                    </option>
	                    
	                    <option value="BRCA"
	                            >BRCA
	                        - Breast invasive carcinoma
	                    </option>
	                    
	                    <option value="CESC"
	                            >CESC
	                        - Cervical squamous cell carcinoma and endocervical adenocarcinoma
	                    </option>       
	                    
	                    <option value="COAD"
	                            >COAD
	                        - Colon adenocarcinoma
	                    </option>
	                    
	                    <option value="DLBC"
	                            >DLBC
	                        - Lymphoid Neoplasm Diffuse Large B-cell Lymphoma
	                    </option>
	                    
	                    <option value="ESCA"
	                            >ESCA
	                        - Esophageal carcinoma 
	                    </option>

	                    <option value="GBM"
	                            selected="selected">GBM
	                        - Glioblastoma multiforme
	                    </option>
	                    
	                    <option value="HNSC"
	                            >HNSC
	                        - Head and Neck squamous cell carcinoma
	                    </option>
	                    
	                    <option value="KICH"
	                            >KICH
	                        - Kidney Chromophobe
	                    </option>
	                    
	                    <option value="KIRC"
	                            >KIRC
	                        - Kidney renal clear cell carcinoma
	                    </option>
	                    
	                    <option value="KIRP"
	                            >KIRP
	                        - Kidney renal papillary cell carcinoma
	                    </option>
	                    
	                    <option value="LAML"
	                            >LAML
	                        - Acute Myeloid Leukemia
	                    </option>
	                    
	                    <option value="LGG"
	                            >LGG
	                        - Brain Lower Grade Glioma
	                    </option>
	                    
	                    <option value="LIHC"
	                            >LIHC
	                        - Liver hepatocellular carcinoma
	                    </option>
	                    
	                    <option value="LUAD"
	                            >LUAD
	                        - Lung adenocarcinoma
	                    </option>
	                    
	                    <option value="LUSC"
	                            >LUSC
	                        - Lung squamous cell carcinoma
	                    </option>
	                    
	                    <option value="OV"
	                            >OV
	                        - Ovarian serous cystadenocarcinoma
	                    </option>
	                    
	                    <option value="PAAD"
	                            >PAAD
	                        - Pancreatic adenocarcinoma
	                    </option>
	                    
	                    <option value="READ"
	                            >READ
	                        - Rectum adenocarcinoma
	                    </option>
	                    
	                    <option value="SARC"
	                            >SARC
	                        - Sarcoma
	                    </option>
	                    
	                    <option value="SKCM"
	                            >SKCM
	                        - Skin Cutaneous Melanoma
	                    </option>
	                    
	                    <option value="STAD"
	                            >STAD
	                        - Stomach adenocarcinoma
	                    </option>
	                    
	                    <option value="THCA"
	                            >THCA
	                        - Thyroid carcinoma
	                    </option>
	                    
	                    <option value="UCEC"
	                            >UCEC
	                        - Uterine Corpus Endometrial Carcinoma
	                    </option>
	                    
	                    <option value="UCS"
	                            >UCS
	                        - Uterine Carcinosarcoma
	                    </option>
				    </select>
					<div class="btn-group">
						<a class="btn btn-success" id="geneSearch_diseasesRank" onclick="goSearchDiseasesRank();"><i class="icon-search icon-white"></i>&nbsp;<fmt:message key="button.search"/></a>
					</div>
				</div>
			</div>
		</div>
	</div>	
	<!-- diseasesRank tab end-->
	
	<!-- variation start -->
	<div class="tab-content" id="tab_content_variation" style="display: none;">
		<div class="tab-pane active text-center row-fluid">
			<div class="control-group">
				<div class="input-prepend input-append" id="searchbar">
					<span class="input-group-addon">
				        <!-- <input name="version" id="version18" value="18" type="radio"><span>hg18</span> -->
				        <input value="19" checked="checked" type="radio"><span>hg19</span>
				    </span>
					<input type="hidden" id="currentVariationGeneSymbol" value="${variationGene}">
					<input type="hidden" id="currentVariationGeneLink" value="${variationGene }">
					<!-- 搜索框 -->
					<input id="searchForm_variation" value="${variationGene }" class="input-large search-query ac_input" autocomplete="off" placeholder="<fmt:message key="placeholder.keywords"/>" required="" type="text">
					<div class="btn-group">
						<a class="btn btn-success" id="geneSearch" onclick="goSearchVariationGene();"><i class="icon-search icon-white"></i>&nbsp;<fmt:message key="button.search"/></a>
						<a class="btn btn-primary" href="#setting" data-toggle="modal"><i class="icon-cog icon-white"></i>&nbsp;<fmt:message key="button.setting"/></a>
					</div>
				</div>
				<span id="diedai" ></span>
			</div>
		</div>
	</div>	
	<!-- variation end -->
	
<!-- multiVariants tab start-->
	<div class="tab-content" id="tab_content_multiVariants" style="display: none">
		<div class="tab-pane active text-center row-fluid">
			<div class="control-group">
				<div class="input-prepend input-append" id="searchbar">
					<span class="input-group-addon">
				        <!-- <input name="version" id="version18" value="18" type="radio"><span>hg18</span> -->
				        <input value="19" checked="checked" type="radio"><span>hg19</span>
				    </span>
					<input type="hidden" id="currentMultiVariants" value="${multiVariants}">
					<!--  <input id="searchForm_multigene" value="${multigene }" class="input-large search-query ac_input" autocomplete="off" placeholder="<fmt:message key="placeholder.keywords"/>" required="" type="text">-->
					<span style="font-size:14px"><textarea cols="30" rows="6" id="searchForm_multiVariants" style="resize:none;">${multiVariants }</textarea></span>
					<div class="btn-group">
						<a class="btn btn-success" id="geneSearch_multiVariants" onclick="geneSearch_multiVariants();"><i class="icon-search icon-white"></i>&nbsp;<fmt:message key="button.search"/></a>
						<a class="btn btn-primary" href="#setting" data-toggle="modal"><i class="icon-cog icon-white"></i>&nbsp;<fmt:message key="button.setting"/></a>
					</div>
				</div>
			</div>
		</div>
	</div>	
<!-- multiVariants tab end-->
</div>

<!-- Setting Modal -->
<div id="setting" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabelaaa" aria-hidden="true" >
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		<h3><fmt:message key="label.setting"/></h3>
	</div>
	<div class="modal-body">
		<div class="control-group" style="height:50px;">
			<label for="experiment" class="control-label"><fmt:message key="label.Experiments"/>:</label>
			<div class="settingItem">
				<input type="checkbox" id="expSetting_checkbox" checked="checked"/><label style="font-weight:bold"><fmt:message key="label.checkAll"/></label>
			</div>
			<div class="settingItem">&nbsp;</div>
			<div class="controls" style="float: left;width: 100%;">
				<c:forEach items="${settingDTO.experimentsMap}" var="experiment">
<c:if test="${experiment.key != 'Supplementary Track' && experiment.key != 'RIP-seq' && experiment.key != 'RNA-seq-diff'}">
					<div class="settingItem">
						<input type="checkbox" value="${experiment.key}" name="experiments" <c:if test="${experiment.value == 'true'}">checked="checked"</c:if>/>
						<label>${experiment.key}
							<c:forEach items="${sampleSumMap}" var="sampleSum">
								<c:if test="${sampleSum.key == experiment.key}">(${sampleSum.value})</c:if>
							</c:forEach>
						</label>
					</div>
					<c:if test="${experiment.key == 'Summary Track'}"><div class="settingItem">&nbsp;</div></c:if>
	</c:if>			
	</c:forEach>
			</div>				 
	  	</div>	
		<%-- <div class="control-group">
			<label class="control-label" for="celltype">Cell type:</label>
			<div class="controls">
				<label class="radio">
					<input name="celltype" id="celltypein" value="Opt_in" <c:if test="${settingDTO.cellType == 'Opt_in'}">checked="checked"</c:if> type="radio">
					Opt in
				</label>
				<label class="radio">
					<input name="celltype" id="celltypeout" value="Opt_out" <c:if test="${settingDTO.cellType == 'Opt_out'}">checked="checked"</c:if> type="radio">
					Opt out
				</label>
			</div>
		</div> --%>
		<div class="control-group">
			<label for="source" class="control-label"><fmt:message key="label.source"/>:</label>
			<div class="controls">
				<div class="settingItem">
					<input type="checkbox" id="sourceSetting_checkbox" checked="checked"/><label style="font-weight:bold"><fmt:message key="label.checkAll"/></label>
				</div>
				<div class="settingItem">&nbsp;</div>
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

