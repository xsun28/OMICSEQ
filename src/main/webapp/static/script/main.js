function initDragDialog(oDrag, tag) {
	var oTitle = get.byClass("title", oDrag)[0];
	var oL = get.byClass("resizeL", oDrag)[0];
	var oT = get.byClass("resizeT", oDrag)[0];
	var oR = get.byClass("resizeR", oDrag)[0];
	var oB = get.byClass("resizeB", oDrag)[0];
	var oLT = get.byClass("resizeLT", oDrag)[0];
	var oTR = get.byClass("resizeTR", oDrag)[0];
	var oBR = get.byClass("resizeBR", oDrag)[0];
	var oLB = get.byClass("resizeLB", oDrag)[0];
	drag(oDrag, oTitle);
	//四角
	resize(oDrag, oLT, true, true, false, false);
	resize(oDrag, oTR, false, true, false, false);
	resize(oDrag, oBR, false, false, false, false);
	resize(oDrag, oLB, true, false, false, false);
	//四边
	resize(oDrag, oL, true, false, false, true);
	resize(oDrag, oT, false, true, true, false);
	resize(oDrag, oR, false, false, false, true);
	resize(oDrag, oB, false, false, true, false);
	oDrag.style.left = (document.documentElement.clientWidth - 800) / 2 + "px";
	if(tag == 3 || tag == 4 || tag == 5 )
	{
		oDrag.style.left = (document.documentElement.clientWidth - 600) / 2 + "px";
	}
	if(tag ==10){
		oDrag.style.left = (document.documentElement.clientWidth - 380) / 2 + "px";
	}
	oDrag.style.top = (document.documentElement.clientHeight - 390) / 2 + "px"; 
}
//保存设置
function changeSettings() {
	var genome = $("#genome").val();
	var experimentsArray = [], sourcesArray = [];
	$("input[name=experiments]:checked:visible").each(function(){
		experimentsArray.push($(this).val());
	});
	var experiments = experimentsArray.join(",");
	
	/*var celltype = '';
	var cellTypeRadios = $('input[name="celltype"]');
	for(var i = 0; i < cellTypeRadios.length; i++){
		if(cellTypeRadios[i].checked){
			celltype = cellTypeRadios[i].value;
			break;
		}
	}*/
	
	$("input[name=sources]:checked:visible").each(function(){
		sourcesArray.push($(this).val());
	});
	var sources = sourcesArray.join(",");
	
	var url = app.ctx + '/user/update.json';
	var params ;
	if(genome == 'Human'){
		params = {"Experiments":experiments, "source":sources};
	}else{
		params = {"Experiments_mouse":experiments, "source_mouse":sources};
	}
	var callBack = function(result){
		if(!result.result) {
			alert(result.message);
		}
	};
	sendRequest(url, params, "json", callBack, "Save settings failed!");
}
//查询
function goSearch(){
	var searchForm = null;
	if($('#searchForm').length > 0){
		searchForm = $('#searchForm');
	}else if($('#searchForm_Mouse').length > 0){
		searchForm = $('#searchForm_Mouse');
	}
	var geneSymbol = searchForm.val().trim();
	var genome = $('#genome').val().trim();
	if(geneSymbol == '') {
		 $('#searchForm').focus();
	}
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	var url = rootPath + "/result.htm?page=1&geneSymbol=" + geneSymbol + "&sort=" + sortType + "&genome="+ genome + "&isLink=false&isHistory=false";
	$('#currentGeneSymbol').val(geneSymbol);
	setCookie("geneSymbol-"+geneSymbol, "<tr><td class='searchword' width='100px' style='text-align: center;'><a style='cursor: pointer;' href='"+url+"' target='_blank'>"+ geneSymbol+"</a></td></tr>");

	window.location.href = url;
}

function goSearch_single(){	
	var geneSymbol = $('#searchForm_single').val().trim();
	if(geneSymbol == '') {
		 $('#searchForm_single').focus();
	}
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	var url = rootPath + "/result_single.htm?page=1&geneSymbol=" + geneSymbol + "&sort=" + sortType + "&isLink=false&isHistory=false";
	$('#currentGeneSymbol').val(geneSymbol);
	setCookie("geneSymbol-"+geneSymbol, "<tr><td class='searchword' width='100px' style='text-align: center;'><a style='cursor: pointer;' href='"+url+"' target='_blank'>"+ geneSymbol+"</a></td></tr>");

	window.location.href = url;
}

function goSearch_test(){	
	var geneSymbol = $('#searchForm').val().trim();
	var genome = $('#genome').val().trim();
	if(geneSymbol == '') {
		 $('#searchForm').focus();
	}
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	var url = rootPath + "/testpages/result.htm?page=1&geneSymbol=" + geneSymbol + "&sort=" + sortType + "&genome="+ genome + "&isLink=false&isHistory=false";
	$('#currentGeneSymbol').val(geneSymbol);
	
	setCookie("geneSymbol-"+geneSymbol, "<tr><td class='searchword' width='100px' style='text-align: center;'><a style='cursor: pointer;' href='"+url+"' target='_blank'>"+ geneSymbol+"</a></td></tr>");
	window.location.href = url;
}

//回车查询
$(document).on('keydown', '#searchForm', function(event){
	if(event.keyCode == 13) {
		goSearch();
	}
});

$(document).on('keydown', '#searchForm_single', function(event){
	if(event.keyCode == 13) {
		goSearch_single();
	}
});
//分页
$(document).on('click', '.paginate', function(){
	var page = $(this).html();
	var genome = $('#genome').val().trim();
	var isLink = $('#isLink').val();
	var isHistory = !!$('#isHistory').val() ? $('#isHistory').val() : "false";
	var geneSymbol = isLink ? $('#currentGeneLink').val() : $('#currentGeneSymbol').val();  
	var sortType = $('#sortType').val();
	var isHistoryResult = !!$('#isHistoryResult').val() ? $('#isHistoryResult').val() : "false";
	var callBack = function(result){
		if(result.result){
			window.location.href = rootPath + "/result.htm?page=" + page + "&geneSymbol=" + geneSymbol 
				+ "&genome="+ genome + "&sort=" + sortType + "&isLink=" + isLink + "&isHistory=" + isHistory + "&isHistoryResult=" + isHistoryResult;		}else{
			//试用用户也可使用gene搜索  不提示错误
				$('#faildDialog .errorMessage').html(result.message);
				$('#faildDialog').modal({
				    backdrop:true,
				    keyboard:true,
				    show:true
				});
			}
		};
	var url = rootPath + "/user/trialAccount.json";
	sendRequest(url, null, "json", callBack, "");
	
});
//上一页
function prevPage() {
	var callBack = function(result){
	if(result.result){
		$('.paginate.current').prev().trigger('click');
	}else{
		//试用用户也可使用gene搜索  不提示错误
			$('#faildDialog .errorMessage').html(result.message);
			$('#faildDialog').modal({
			    backdrop:true,
			    keyboard:true,
			    show:true
			});
		}
	};
	var url = rootPath + "/user/trialAccount.json";
	sendRequest(url, null, "json", callBack, "");
	
}
//下一页
function nextPage() {
	var callBack = function(result){
		if(result.result){
			$('.paginate.current').next().trigger('click');
		}else{
			//试用用户也可使用gene搜索  不提示错误
				$('#faildDialog .errorMessage').html(result.message);
				$('#faildDialog').modal({
				    backdrop:true,
				    keyboard:true,
				    show:true
				});
			}
		};
		var url = rootPath + "/user/trialAccount.json";
		sendRequest(url, null, "json", callBack, "");
	
}

//分页_single
$(document).on('click', '.paginate_single', function(){
	var page = $(this).html();
	var isLink = $('#isLink').val();
	var isHistory = !!$('#isHistory').val() ? $('#isHistory').val() : "false";
	var geneSymbol = isLink ? $('#currentGeneLink').val() : $('#currentGeneSymbol').val();  
	var sortType = $('#sortType').val();
	var isHistoryResult = !!$('#isHistoryResult').val() ? $('#isHistoryResult').val() : "false";
	window.location.href = rootPath + "/result_single.htm?page=" + page + "&geneSymbol=" + geneSymbol 
	+ "&sort=" + sortType + "&isLink=" + isLink + "&isHistory=" + isHistory + "&isHistoryResult=" + isHistoryResult;
});
//上一页
function prevPage_single() {
	$('.paginate_single.current').prev().trigger('click');
}
//下一页
function nextPage_single() {
	$('.paginate_single.current').next().trigger('click');
}

//you can also search
$(document).on('click', '.linkRow .linkWord', function(){
	var key = $(this).html();
	if(key != '') {
		key = key.replace(',','');
		var sortType = $('#sortType').val();
		var genome = $('#genome').val().trim();
		$('#currentGeneLink').val(key);
		var isHistory = !!$('#isHistory').val() ? $('#isHistory').val() : "false";
		window.open( rootPath + "/result.htm?page=1&geneSymbol=" + key + "&sort=" + sortType 
				+ "&genome="+ genome + "&isLink=true&isHistory=" + isHistory);
	}
});
//华大gene_single搜索  不出现其他Tab
$(document).on('click', '.linkRow .linkWord_single', function(){
	var key = $(this).html();
	if(key != '') {
		key = key.replace(',','');
		var sortType = $('#sortType').val();
		$('#currentGeneLink').val(key);
		var isHistory = !!$('#isHistory').val() ? $('#isHistory').val() : "false";
		window.open( rootPath + "/result_single.htm?page=1&geneSymbol=" + key + "&sort=" + sortType 
		 	+ "&isLink=true&isHistory=" + isHistory);
	}
});

//pathway you can also search singleGene..
$(document).on('click', '.linkRow .linkWord_pathway', function(){
	var key = $(this).html();
	if(key != '') {
		key = key.replace(',','');
		var sortType = $('#sortType').val();
		var genome = $('#genome').val().trim();
		$('#currentGeneLink').val(key);
		var isHistory = !!$('#isHistory').val() ? $('#isHistory').val() : "false";
		window.open( rootPath + "/result.htm?page=1&geneSymbol=" + key + "&sort=" + sortType 
				+ "&genome="+ genome + "&isLink=false&isHistory=" + isHistory);
	}
});
//multigene you can also search singleGene..
$(document).on('click', '.linkRow .linkWord_multigene', function(){
	var key = $(this).html();
	if(key != '') {
		key = key.replace(',','');
		var sortType = $('#sortType').val();
		$('#currentGeneLink').val(key);
		var isHistory = !!$('#isHistory').val() ? $('#isHistory').val() : "false";
		window.open( rootPath + "/result.htm?page=1&geneSymbol=" + key + "&sort=" + sortType 
				+ "&genome="+ genome + "&isLink=false&isHistory=" + isHistory);
	}
});
//升序降序排列
function listBySort(target) {
	var $this = $(target);
	var genome = $('#genome').val().trim();
	var isLink = $('#isLink').val();
	var isHistory = !!$('#isHistory').val() ? $('#isHistory').val() : "false";
	var geneSymbol = isLink ? $('#currentGeneLink').val() : $('#currentGeneSymbol').val();  
	var sortType = $this.find('i').hasClass('icon-arrow-up') ? "DESC" : "ASC";
	window.location.href = rootPath + "/result.htm?page=1&geneSymbol=" + geneSymbol + "&sort=" + sortType 
		+ "&genome="+ genome + "&isLink=" + isLink + "&isHistory=" + isHistory;
	if(sortType == "DESC") {
		$this.find('i').removeClass('icon-arrow-up').addClass('icon-arrow-down');
	}else{
		$this.find('i').removeClass('icon-arrow-down').addClass('icon-arrow-up');
	}
}

//显示metaData
function showMetaData(target,sampleId) {
	var $this = $(target);
	var data1 = $this.parent().find('.data1').val().split(';');
	var data2 = $this.parent().find('.data2').val().split(';');
	if($this.hasClass('activeMetaData')) {
		//hidden
		for(var i=0;i<data1.length;i++){
			//$this.parents('tr').next().remove();
			$("#metaData_"+sampleId).remove();
			$this.removeClass('activeMetaData');
		}
	}else{
		//show
		var html = [];
		for(var i = 0; i<data1.length; i++){
			html.push('<tr style="display:none" class="info" id="metaData_'+sampleId+'">');
			html.push('<td colspan="6" title="'+data1[i]+'">',data1[i],'</td>');
			html.push('<td colspan="6" title="'+data2[i]+'">',data2[i],'</td></tr>');
		}
		var el = $(html.join(''));
		if($this.parents('tr').next().attr("id")=='btnAppend_'+sampleId){
			$this.parents('tr').next().after(el);
		}else{
			$this.parents('tr').after(el);
		}
		
		el.show(200);
		$this.addClass('activeMetaData');
	}
}

function downloadAll(geneId,geneSymbol){
	var user = getCookie("jforumSSOCookieNameUser");
	if(user == null) return;
	var userType = parseInt(user.split(",")[2]);
	var callBack = function(result){
		if(result.result) {
			window.open(rootPath+"/export/xlsx.htm?geneId="+geneId+"&term="+geneSymbol);
		}
		else{
			if(userType > 1){
				$('#faildDialog .errorMessage').html(result.message);
				$('#faildDialog').modal({
				    backdrop:true,
				    keyboard:true,
				    show:true
				});
			}
		}
	};
	var url1 = rootPath + "/user/trialAccount.json";
    sendRequest(url1, null, "json", callBack, "");
}

function downloadSelected(target) {
	var user = getCookie("jforumSSOCookieNameUser");
	if(user == null) return;
	var userType = parseInt(user.split(",")[2]);
	
	var callBack = function(result){
		if(result.result) {
			var $this = $(target);
			var iframe = document.createElement("iframe");
		    iframe.style.width = 0;
		    iframe.style.height = 0;
		    iframe.style.display = "none";
		    document.body.appendChild(iframe);
		    var headers = "Rank,DataSetID,DataType,Cell,Factor,Order/Total,Percentile,tssTesCount,tss5KCount,Study,Lab";
		    var term = $('#currentGeneSymbol').val();
		    var selected = [];
		    $("input:checkbox[name=rowNum]:checked").each(function(){
		    	var $this = $(this);
		    	var parentTr = $this.parent().parent();
		    	var rank = parentTr.find('.rankcheckbox').val().trim();
		    	var sampleId = parentTr.find('.sampleId').html().trim();
		    	var dataType = parentTr.find('.dataType').html().trim();
		    	var cell = parentTr.find('.cell').html().trim();
		    	var factor = parentTr.find('.factor').html().trim().replace(/\#/g,'$');
		    	var ot = parentTr.find('.orderTotal').html().trim();
		    	var per = parentTr.find('.percent').html().trim();
		    	var count = parentTr.find('.count').html().trim();
		    	var tescount = parentTr.find('.tescount').html().trim();
		    	var study = parentTr.find('.study').html().trim();
		    	var lab = parentTr.find('.lab').html().trim();
		    	var itemStr = rank + "@" + sampleId + "@" + dataType + "@" + cell + "@" 
				+ factor + "@" + ot + "@" + per + "@" + tescount + "@" + count + "@"  + study + "@" + lab;
		    	
		    	selected.push(itemStr);
		    });
		    if(selected.length == 0) {
		    	var error = $this.parent().attr("error");
		    	$('#faildDialog .errorMessage').html(error);
				$('#faildDialog').modal({
				    backdrop:true,
				    keyboard:true,
				    show:true
				});
		    	return;
		    }
		    var ucsc = $('#ucscDrag .content img').attr('src');
		    var url = rootPath + "/export/xlsx.htm?headers=" + headers + "&data=" + selected.join(";") + "&ucsc=" + ucsc + "&term=" + term;
		    
			iframe.src = url;
		}
		else{
			if(userType > 1){
				$('#faildDialog .errorMessage').html(result.message);
				$('#faildDialog').modal({
				    backdrop:true,
				    keyboard:true,
				    show:true
				});
			}
		}
	};
	
	var url1 = rootPath + "/user/trialAccount.json";
    sendRequest(url1, null, "json", callBack, "");
	
}

function downloadSelectedMiRNA(target) {
	var $this = $(target);
	var iframe = document.createElement("iframe");
    iframe.style.width = 0;
    iframe.style.height = 0;
    iframe.style.display = "none";
    document.body.appendChild(iframe);
    var headers = "Rank,DataSetID,DataType,Cell,Factor,Order/Total,Percentile,Study,Lab";
    var term = $('#currentmiRNA').val();
    var selected = [];
    $("input:checkbox[name=rowNum]:checked").each(function(){
    	var $this = $(this);
    	var parentTr = $this.parent().parent();
    	var rank = parentTr.find('.rankcheckbox').val().trim();
    	var sampleId = parentTr.find('.sampleId').html().trim();
    	var dataType = parentTr.find('.dataType').html().trim();
    	var cell = parentTr.find('.cell').html().trim();
    	var factor = parentTr.find('.factor').html().trim().replace(/\#/g,'$');
    	var ot = parentTr.find('.orderTotal').html().trim();
    	var per = parentTr.find('.percent').html().trim();
    	var study = parentTr.find('.study').html().trim();
    	var lab = parentTr.find('.lab').html().trim();
    	var itemStr = rank + "@" + sampleId + "@" + dataType + "@" + cell + "@" 
		+ factor + "@" + ot + "@" + per + "@" + study + "@" + lab;
    	selected.push(itemStr);
    });
    if(selected.length == 0) {
    	var error = $this.parent().attr("error");
    	$('#faildDialog .errorMessage').html(error);
		$('#faildDialog').modal({
		    backdrop:true,
		    keyboard:true,
		    show:true
		});
    	return;
    }
    
    //var ucsc = $('#ucscDrag .content img').attr('src');
    //var url = rootPath + "/export/miRNA_xlsx.htm?headers=" + headers + "&data=" + selected.join(";") + "&ucsc=" + ucsc + "&term=" + term;
    var url = rootPath + "/export/miRNA_xlsx.htm?headers=" + headers + "&data=" + selected.join(";") +  "&term=" + term;
    iframe.src = url;
}



function selectAll(obj) {
	var checkboxes = document.getElementsByName('rowNum');
	for(var i = 0; i < checkboxes.length; i++){
		checkboxes[i].checked = obj.checked;
	} 
}

function showErrorImageTip(target) {
	var error = $(target).attr("errorMessage");
	$(target).find('.content').html(error);
}

//显示历史记录
$(document).on('click', '#getCurrenthistory', function(e){
	var $this = $(this);
	var height = document.documentElement.clientHeight - 120;
	if($this.hasClass('expand')) {
		$("#current_history").animate( { height: "0px"}, 300 );
		$this.removeClass('expand');
		$this.addClass('shrink');
		$('#current_history .c_h_content').hide();
	}else{
		$("#current_history").animate( { height: height}, 300 );
		var contentHeight = height - 50 - 40 - 42;
		$('.c_h_content').height(contentHeight);
		$this.addClass('expand');
		$this.removeClass('shrink');
		
		var s = "<div class='h_title'><table><tr><th width='100%'>Keyword</th></tr></table></div><table width='100%'><tbody>";
		var acookie=document.cookie.split("; ");
		for(var i=0;i<acookie.length;i++) {
			var arr=acookie[i].split("=");
			if(arr[0].indexOf("geneSymbol-") != -1){
				if(arr.length>1)
					s += unescape(arr[1]);
				else
					s +="";
			}
		}
		s += "</tbody></table>";
		$('#current_history .c_h_content').html(s);
		$('#current_history .c_h_content').show();
	}
});


//显示历史记录
$(document).on('click', '#gethistory', function(e){
	var $this = $(this);
	var height = document.documentElement.clientHeight - 45;
	if($this.hasClass('shrink')) {
		$("#history").animate( { height: "0px"}, 300 );
		$this.removeClass('shrink');
	}else{
		$('#searchkey').val('');
		$("#history").animate( { height: height}, 300 );
		var contentHeight = height - 50 - 40 - 42;
		$('.h_content').height(contentHeight);
		$this.addClass('shrink');
		setTimeout(function() {
			$('#historyLoading').show();
			var url = rootPath + "/user/history.htm";
			var callBack = function(data){
				$('#history .h_content').html('').html(data);
				$('#historyLoading').hide();
			};
			sendRequest(url, {}, "html", callBack, "Load history failed!");
		}, 300);
	}
});
//搜索历史记录
function searchHistory() {
	$('#historyLoading').show();
	var keyword = $('#searchkey').val();
	var url = rootPath + "/user/history.htm";
	var callBack = function(data){
		$('#history .h_content').html('').html(data);
		$('#historyLoading').hide();
	};
	sendRequest(url, {keyword:keyword}, "html", callBack, "Load history failed!");
}
//点击历史记录搜索关键字
function searchByHistory(target) {
	var $target = $(target);
	var geneSymbol = $target.html();
	var genome = $('#genome').val().trim();
	var url = rootPath + "/result.htm?page=1&geneSymbol=" + geneSymbol + "&genome="+ genome + "&sort=ASC&isLink=false&isHistory=true";
	$('#currentGeneSymbol').val(geneSymbol);
	window.location.href = url;
}
//收藏
function saveHistory() {
	var url = rootPath + "/user/saveHistory.json";
	var keyword = $('#searchForm').val();
	var geneId = $('#currentGeneId').val();
	var callBack = function(result){
		if(result.result) {
			//保存成功
			$('#successDialog').modal({
			    backdrop:true,
			    keyboard:true,
			    show:true
			});
		}else{
			if(result.code == '1001') {
				$('#confirmDialog #confirmMessage').html(result.message);
				$('#confirmDialog').modal({
				    backdrop:true,
				    keyboard:true,
				    show:true
				});
			}else if(result.code == '1000') {
				$('#faildDialog .errorMessage').html(result.message);
				$('#faildDialog').modal({
				    backdrop:true,
				    keyboard:true,
				    show:true
				});
			}
			
		}
	};
	sendRequest(url, {keyword:keyword, geneId:geneId,genome:genome}, "json", callBack, "Save History failed!");
}
//更新历史记录
function updateHistory() {
	var url = rootPath + "/user/updateHistory.json";
	var keyword = $('#searchForm').val();
	var geneId = $('#currentGeneId').val();
	var callBack = function(result){
		if(result.result) {
			//更新成功
			$('#successDialog').modal({
			    backdrop:true,
			    keyboard:true,
			    show:true
			});
		}else{
			$('#faildDialog .errorMessage').html(result.message);
			$('#faildDialog').modal({
			    backdrop:true,
			    keyboard:true,
			    show:true
			});
		}
	};
	sendRequest(url, {keyword:keyword, geneId:geneId}, "json", callBack, "Update History failed!");
}

//点击搜索结果
$(document).on('click', '.resultbutton', function(){
	var geneSymbol = $(this).parent().parent().find('.searchword').find('a').html();
	var genome = $('#genome').val().trim();
	var url = rootPath + "/result.htm?page=1&geneSymbol=" + geneSymbol + "&genome="+ genome + "&sort=ASC&isLink=false&isHistoryResult=true";
	$('#currentGeneSymbol').val(geneSymbol);
	window.location.href = url;
});
//点击齿轮
$(document).on('click', '#pageSetting', function(){
	if($('.div3-pop').hasClass('show')) {
		$('.div3-pop').removeClass('show');
		$('.div3-pop ul').removeClass('show');
		$('.historybar .active').removeClass('active');
		$(this).removeClass('active');
	}else{
		$('.div3-pop').addClass('show');
		$(this).addClass('active');
	}
});
$(document).on('click', '.div3-pop', function(){
	$(this).addClass('active');
	$('.div3-pop ul').addClass('show');
});
$(document).on('mouseover', '.div3-pop', function(){
	$('.div3-pop ul').addClass('show');
});
$(document).on('mouseout', '.div3-pop', function(){
	$('.div3-pop ul').removeClass('show');
});
$(document).on('mouseover', '.div3-pop li', function(){
	$('.div3-pop ul').addClass('active');
});
$(document).on('click', '.div3-pop li', function(){
	var pageSize = $(this).find('.pageSize').html();
	//保存到数据库
	var url = rootPath + "/user/update.json";
	var callBack = function(result){
		$('#loading').hide();
		if(result.result) {
			var href = window.location.href;
			var hrefArray = href.split("?");
			if(href.indexOf('result.htm') > -1) {
				var paramArray = hrefArray[1].split("&");
				for(var i=0;i<paramArray.length;i++) {
					var temp = paramArray[i];
					if(temp.indexOf('page') > -1) {
						paramArray[i] = 'page=1';
					}
				}
				href = hrefArray[0] + "?" + paramArray.join('&');
			}
			window.location.href = href;
		}else{
			$('#faildDialog .errorMessage').html(result.message);
			$('#faildDialog').modal({
			    backdrop:true,
			    keyboard:true,
			    show:true
			});
		}
	};
	$('#loading').show();
	sendRequest(url, {pageSize:pageSize}, "json", callBack, "Update User failed!");
});


//基因组查询
function goSearchPathWay(){
	var pathWayName = $("#searchForm_pathWay").val().trim();
	if(pathWayName == '') {
		 $('#searchForm_pathWay').focus();
	}
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	var url = rootPath + "/result_pathway.htm?page=1&pathWayName=" + pathWayName  + "&isLink=false&isHistory=false&sort=" + sortType;
	window.location.href = url;
}
//基因组回车查询
$(document).on('keydown', '#searchForm_pathWay', function(event){
	if(event.keyCode == 13) {
		goSearchPathWay();
	}
});

//基因组分页
$(document).on('click', '.paginate.pathWay', function(){
	var page = $(this).html();
	var pathWayName = $("#searchForm_pathWay").val().trim();
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	window.location.href = rootPath + "/result_pathway.htm?page=" + page + "&pathWayName=" + pathWayName + "&isLink=false&isHistory=false&sort=" + sortType;
});

//基因组分页
$(document).on('click_pathWay', '.paginate', function(){
	var page = $(this).html();
	var pathWayName = $("#searchForm_pathWay").val().trim();
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	window.location.href = rootPath + "/result_pathway.htm?page=" + page + "&pathWayName=" + pathWayName + "&isLink=false&isHistory=false&sort=" + sortType;
});

//pathway排序
function list_PathWay_BySort(target){
	var $this = $(target);
	var page = $(this).html();
	var currentPathwayName = $('#currentPathwayName').val();  
	var sortType = $this.find('i').hasClass('icon-arrow-up') ? "DESC" : "ASC";
	window.location.href = rootPath + "/result_pathway.htm?page=" + page + "&pathWayName=" + currentPathwayName + "&isLink=false&isHistory=false&sort=" + sortType;
	if(sortType == "DESC") {
		$this.find('i').removeClass('icon-arrow-up').addClass('icon-arrow-down');
	}else{
		$this.find('i').removeClass('icon-arrow-down').addClass('icon-arrow-up');
	}
}

//基因组上一页
function prevPage_pathWay() {
	$('.paginate.current').prev().trigger('click_pathWay');
}
//基因组下一页
function nextPage_pathWay() {
	$('.paginate.current').next().trigger('click_pathWay');
}

//多基因查询
function goSearchMultigene(){
	var multigene = "";
	var multigene1 = $("#searchForm_multigene").val().trim();
	var genes = multigene1.split("\n");
	for(var i=0;i<genes.length;i++){
		multigene +=genes[i]+",";
	}
	if(multigene == ',') {
		 $('#searchForm_multigene').focus();
	}
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	var url = rootPath + "/result_multigene.htm?page=1&multigene=" + multigene  + "&isLink=false&isHistory=false&sort=" + sortType;
	window.location.href = url;
}
//回车查询
/*
 * $(document).on('keydown', '#searchForm_multigene', function(event){
	if(event.keyCode == 13) {
		goSearchMultigene();
	}
});
*/
//多基因上一页
function prevPage_multigene() {
	$('.paginate.current').prev().trigger('click_multigene');
}
//多基因下一页
function nextPage_multigene() {
	$('.paginate.current').next().trigger('click_multigene');
}



//multigene排序
function list_multigene_BySort(target){
	var $this = $(target);
	var page = $(this).html();
	var currentMultigene = $('#currentMultigene').val();  
	var sortType = $this.find('i').hasClass('icon-arrow-up') ? "DESC" : "ASC";
	window.location.href = rootPath + "/result_multigene.htm?page=" + page + "&multigene=" + currentMultigene + "&isLink=false&isHistory=false&sort=" + sortType;
	if(sortType == "DESC") {
		$this.find('i').removeClass('icon-arrow-up').addClass('icon-arrow-down');
	}else{
		$this.find('i').removeClass('icon-arrow-down').addClass('icon-arrow-up');
	}
}

//多基因分页
$(document).on('click_multigene', '.paginate', function(){
	var page = $(this).html();
	var multigene = $("#searchForm_multigene").val().trim();
	var multigene1 = "";
	var genes = multigene.split("\n");
	for(var i=0;i<genes.length;i++){
		multigene1 +=genes[i]+",";
	}
	if(multigene1 == ',') {
		 $('#searchForm_multigene').focus();
	}
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	window.location.href = rootPath + "/result_multigene.htm?page=" + page + "&multigene=" + multigene1 + "&isLink=false&isHistory=false&sort=" + sortType;
});
$(document).on('click', '.paginate.multigene', function(){
	var page = $(this).html();
	var multigene = $("#searchForm_multigene").val().trim();
	var multigene1 = "";
	var genes = multigene.split("\n");
	for(var i=0;i<genes.length;i++){
		multigene1 +=genes[i]+",";
	}
	if(multigene1 == ',') {
		 $('#searchForm_multigene').focus();
	}
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	window.location.href = rootPath + "/result_multigene.htm?page=" + page + "&multigene=" + multigene1 + "&isLink=false&isHistory=false&sort=" + sortType;
});

function showFactor_Desc(desc, factor) {
	//alert(desc);
	if(desc == "")
	{
		desc = "no message";
	}
	$("#dialog").text(desc);
	$("#dialog").dialog({
		title: factor + " Description",
	    bgiframe: true,
	    resizable: false,
	    height:240,
	    width:400,
	    modal: true,
	    buttons: {
	        Cancel: function() {
	            $(this).dialog('close');
	        }
	    }
	});
}

//范围查询
//function goSearchGenomicRegion(){
//	var chr = $("#searchForm_chr").val().trim();
//	var start = $("#searchForm_start").val().trim();
//	var end = $("#searchForm_end").val().trim();
//	
//	if(chr == '') {
//		 $('#searchForm_chr').focus();
//		 return;
//	}
//	if(start == '') {
//		 $('#searchForm_start').focus();
//		 return;
//	}
//	if(end == '') {
//		 $('#searchForm_end').focus();
//		 return;
//	}
//	var sortType = $('#sortType').val();
//	if(!!!sortType) {
//		sortType = "ASC";
//	}
//	var url = rootPath + "/result_genomicregion.htm?page=1&chr=" + chr  + "&start="+ start +"&end="+ end +"&isLink=false&isHistory=false&sort=" + sortType;
//	window.location.href = url;
//}
function downloadData(){
	var experimentsArray = [], sourcesArray = [] ,columnsArray = [];
	$("input[name=dExperiments]:checked").each(function(){
		experimentsArray.push($(this).val());
	});
	var experiments = experimentsArray.join(",");
	
	$("input[name=dSources]:checked").each(function(){
		sourcesArray.push($(this).val());
	});
	var sources = sourcesArray.join(",");
	
	$("input[name=dColumn]:checked").each(function(){
		columnsArray.push($(this).val());
	});
	var columns = columnsArray.join(",");
	
	var multigene = "";
	var multigene1 = $("#searchForm_geneSymbols").val().trim();
	var genes = multigene1.split("\n");
	for(var i=0;i<genes.length;i++){
		multigene +=genes[i]+",";
	}
	if(multigene=="" || multigene==","){
		$("#searchForm_geneSymbols").focus();
	}
	var cell = $("#cell").val().trim();
	var factor = $("#factor").val().trim();
	var url = app.ctx + '/download.htm?experiments='+experiments+"&sources="+sources+"&geneSymbols="+multigene+"&columns="+columns+"&cell="+cell+"&factor="+factor;
	window.location.href = url;
}

function goSearchDiseasesRank() {
	var cancerType = $('#diseasesType option:selected').val();
	var url = rootPath + "/result_diseasesRank.htm?cancerType=" + cancerType;
	
	window.location.href = url;
}

//查询
function goSearch_miRNA(){	
	var miRNA = $('#searchForm_miRNA').val().trim();
	if(miRNA == '' || miRNA =='hsa-') {
		 $('#searchForm_miRNA').focus();
	}
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	var url = rootPath + "/result_miRNA.htm?page=1&miRNA=" + miRNA + "&sort=" + sortType + "&isLink=false&isHistory=false";
	$('#currentMiRNA').val(miRNA);
	
	window.location.href = url;
}

$(document).on('keydown', '#searchForm_miRNA', function(event){
	if(event.keyCode == 13) {
		goSearch_miRNA();
	}
});
//miRNA上一页
function prevPage_miRNA() {
	$('.paginate.current').prev().trigger('click_miRNA');
}
//miRNA下一页
function nextPage_miRNA() {
	$('.paginate.current').next().trigger('click_miRNA');
}
//miRNA排序
function list_miRNA_BySort(target){
	var $this = $(target);
	var page = $(this).html();
	var currentMiRNA = $("#searchForm_miRNA").val().trim();  
	var sortType = $this.find('i').hasClass('icon-arrow-up') ? "DESC" : "ASC";
	window.location.href = rootPath + "/result_miRNA.htm?page=" + page + "&miRNA=" + currentMiRNA + "&isLink=false&isHistory=false&sort=" + sortType;
	if(sortType == "DESC") {
		$this.find('i').removeClass('icon-arrow-up').addClass('icon-arrow-down');
	}else{
		$this.find('i').removeClass('icon-arrow-down').addClass('icon-arrow-up');
	}
}

//miRNA分页
$(document).on('click_miRNA', '.paginate', function(){
	var page = $(this).html();
	var miRNA = $("#searchForm_miRNA").val().trim();
	if(miRNA == '') {
		 $('#searchForm_miRNA').focus();
	}
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	window.location.href = rootPath + "/result_miRNA.htm?page=" + page + "&miRNA=" + miRNA + "&isLink=false&isHistory=false&sort=" + sortType;
});
$(document).on('click', '.paginate.miRNA', function(){
	var page = $(this).html();
	var miRNA = $("#searchForm_miRNA").val().trim();
	if(miRNA == ''|| miRNA =='hsa-') {
		 $('#searchForm_miRNA').focus();
	}
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	window.location.href = rootPath + "/result_miRNA.htm?page=" + page + "&miRNA=" + miRNA+ "&isLink=false&isHistory=false&sort=" + sortType;
});
//datasetSearch 搜索
function goSearch_datasetSearch(){
	var cell = $('#searchForm_datasetSearch_cell').val().trim();
	if(cell == '') {
		 $('#searchForm_datasetSearch_cell').focus();
	}
	var factor = $('#searchForm_datasetSearch_factor').val().trim();
	
	if(factor == '') {
		 $('#searchForm_datasetSearch_factor').focus();
	}
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	var url = rootPath + "/result_datasetSearch.htm?page=1&cell=" + cell +"&factor="+factor+"&sort=" + sortType + "&isLink=false&isHistory=false";
	$('#currentCell').val(cell);
	$('#currentFactor').val(factor);
	
	window.location.href = url;
}
//回车查询
$(document).on('keydown', '#searchForm_datasetSearch_cell', function(event){
	if(event.keyCode == 13) {
		goSearch_datasetSearch();
	}
});
$(document).on('keydown', '#searchForm_datasetSearch_factor', function(event){
	if(event.keyCode == 13) {
		goSearch_datasetSearch();
	}
});

//分页
$(document).on('click_datasetSearch', '.paginate', function(){
	var page = $(this).html();
	var isLink = $('#isLink').val();
	var isHistory = !!$('#isHistory').val() ? $('#isHistory').val() : "false";
	var cell =  $('#currentCell').val();  
	var factor = $('#currentFactor').val();
	var sortType = $('#sortType').val();
	var isHistoryResult = !!$('#isHistoryResult').val() ? $('#isHistoryResult').val() : "false";
	window.location.href = rootPath + "/result_datasetSearch.htm?page=" + page + "&cell=" + cell  +"&factor=" + factor 
	+ "&sort=" + sortType + "&isLink=" + isLink + "&isHistory=" + isHistory + "&isHistoryResult=" + isHistoryResult;
});
$(document).on('click', '.paginate.datasetSearch', function(){
	var page = $(this).html();
	var cell = $("#searchForm_datasetSearch_cell").val().trim();
	var factor = $("#searchForm_datasetSearch_factor").val().trim();
	if(cell == '') {
		 $('#searchForm_datasetSearch_cell').focus();
	}
	if(factor == '') {
		 $('#searchForm_datasetSearch_factor').focus();
	}
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	window.location.href = rootPath + "/result_datasetSearch.htm?page=" + page + "&cell=" + cell  +"&factor=" + factor 
	+ "&sort=" + sortType + "&isLink=" + isLink + "&isHistory=" + isHistory + "&isHistoryResult=" + isHistoryResult;
});

//上一页
function prevPage_datasetSearch() {
	$('.paginate.current').prev().trigger('click_datasetSearch');
}
//下一页
function nextPage_datasetSearch() {
	$('.paginate.current').next().trigger('click_datasetSearch');
}


function goSearchGenomicRegion(){
	var genomicRegion = $("#selectedGenomicRegion").find("option:selected").val();
	var url = rootPath + "/result_genomicRegion.htm?genomicRegion=" + genomicRegion;
	window.location.href = url;
}

//分页
$(document).on('click_genomicRegion', '.paginate', function(){
	var page = $(this).html();
	var genomicRegion = $("#selectedGenomicRegion").find("option:selected").val();
	window.location.href = rootPath + "/result_genomicRegion.htm?genomicRegion="+genomicRegion+"&page=" + page ;
});
$(document).on('click', '.paginate.genomicRegion', function(){
	var page = $(this).html();
	var genomicRegion = $("#selectedGenomicRegion").find("option:selected").val();

	window.location.href = rootPath + "/result_genomicRegion.htm?genomicRegion="+genomicRegion+"&page=" + page ;
});

//上一页
function prevPage_genomicRegion() {
	$('.paginate.current').prev().trigger('click_genomicRegion');
}
//下一页
function nextPage_genomicRegion() {
	$('.paginate.current').next().trigger('click_genomicRegion');
}

function geneSearchAdvanced(){
	var geneSymbol = $('#searchForm_advanced').val().trim();
	var geneCell = $('#searchForm_cell').val().trim();
	var geneDetail = $('#searchForm_detail').val().trim();
	var genome = $('#genome_advanced').val().trim();
	if(geneSymbol == '') {
		 $('#searchForm').focus();
	}
	if(geneCell == '' && geneDetail == ''){
		if(geneCell == ''){
			$('#searchForm_cell').focus();
			return;
		} 
		if(geneDetail == ''){
			 $('#searchForm_detail').focus();
			 return;
		}
	}
	var experimentsArray = [], sourcesArray = [];
	$("input[name=experiments_advanced]:checked:visible").each(function(){
		experimentsArray.push($(this).val());
	});
	var experiments = experimentsArray.join(",");
	
	$("input[name=sources_advanced]:checked:visible").each(function(){
		sourcesArray.push($(this).val());
	});
	var sources = sourcesArray.join(",");
	var sortType = $('#sortType').val();
	if(!sortType) {
		sortType = "ASC";
	}
	var url = rootPath + "/result_geneSearchAdvanced.htm?page=1&geneSymbol=" + geneSymbol + "&genome=" + genome + "&cell=" +geneCell + "&detail=" +geneDetail+ "&experiments=" + experiments + "&sources=" + sources +"&sort=" + sortType + "&isLink=false&isHistory=false";
	$('#currentGeneSymbol').val(geneSymbol);
	$('#currentGeneCell').val(geneCell);
	$('#currentGeneD').val(geneDetail);
	setCookie("geneSymbol-"+geneSymbol, "<tr><td class='searchword' width='100px' style='text-align: center;'><a style='cursor: pointer;' href='"+url+"' target='_blank'>"+ geneSymbol+"</a></td></tr>");
	window.location.href = url;
}


//gene_advanced上一页
function prevPage_advanced() {
	$('.paginate.current').prev().trigger('click_advanced');
}
//gene_advanced下一页
function nextPage_advanced() {
	$('.paginate.current').next().trigger('click_advanced');
}
//gene_advanced排序
function list_advanced_BySort(target){
	var $this = $(target);
	var geneSymbol = $('#searchForm_advanced').val().trim();
	var geneCell = $('#searchForm_cell').val().trim();
	var geneDetail = $('#searchForm_detail').val().trim();
	if(geneSymbol == '') {
		 $('#searchForm').focus();
		 return;
	}
	if(geneCell == '' && geneDetail == ''){
		if(geneCell == ''){
			$('#searchForm_cell').focus();
			return;
		} 
		if(geneDetail == ''){
			 $('#searchForm_detail').focus();
			 return;
		}
	} 
	var sortType = $this.find('i').hasClass('icon-arrow-up') ? "DESC" : "ASC";
	var url = rootPath + "/result_geneSearchAdvanced.htm?page=1&geneSymbol=" + geneSymbol +"&cell=" +geneCell + "&detail=" +geneDetail+ "&experiments=" + experiments + "&sources=" + sources + "&sort=" + sortType + "&isLink=false&isHistory=false";
	window.location.href = url;
	if(sortType == "DESC") {
		$this.find('i').removeClass('icon-arrow-up').addClass('icon-arrow-down');
	}else{
		$this.find('i').removeClass('icon-arrow-down').addClass('icon-arrow-up');
	}
}

//advanced分页
$(document).on('click_advanced', '.paginate', function(){
	var page = $(this).html();
	var geneSymbol = $('#searchForm_advanced').val().trim();
	var geneCell = $('#searchForm_cell').val().trim();
	var geneDetail = $('#searchForm_detail').val().trim();
	if(geneSymbol == '') {
		 $('#searchForm').focus();
		 return;
	}
	if(geneCell == '' && geneDetail == ''){
		if(geneCell == ''){
			$('#searchForm_cell').focus();
			return;
		} 
		if(geneDetail == ''){
			 $('#searchForm_detail').focus();
			 return;
		}
	} 
	var experimentsArray = [], sourcesArray = [];
	$("input[name=experiments_advanced]:checked").each(function(){
		experimentsArray.push($(this).val());
	});
	var experiments = experimentsArray.join(",");
	
	$("input[name=sources_advanced]:checked").each(function(){
		sourcesArray.push($(this).val());
	});
	var sources = sourcesArray.join(",");
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	var url = rootPath + "/result_geneSearchAdvanced.htm?page=" + page + "&geneSymbol=" + geneSymbol +"&cell=" +geneCell + "&detail=" +geneDetail + "&experiments=" + experiments + "&sources=" + sources + "&sort=" + sortType + "&isLink=false&isHistory=false";
	window.location.href = url;
});
$(document).on('click', '.paginate.advanced', function(){
	var page = $(this).html();
	var geneSymbol = $('#searchForm_advanced').val().trim();
	var geneCell = $('#searchForm_cell').val().trim();
	var geneDetail = $('#searchForm_detail').val().trim();
	if(geneSymbol == '') {
		 $('#searchForm').focus();
		 return;
	}
	if(geneCell == '' && geneDetail == ''){
		if(geneCell == ''){
			$('#searchForm_cell').focus();
			return;
		} 
		if(geneDetail == ''){
			 $('#searchForm_detail').focus();
			 return;
		}
	} 
	
	var experimentsArray = [], sourcesArray = [];
	$("input[name=experiments_advanced]:checked").each(function(){
		experimentsArray.push($(this).val());
	});
	var experiments = experimentsArray.join(",");
	
	$("input[name=sources_advanced]:checked").each(function(){
		sourcesArray.push($(this).val());
	});
	var sources = sourcesArray.join(",");
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	
	var url = rootPath + "/result_geneSearchAdvanced.htm?page=" + page + "&geneSymbol=" + geneSymbol +"&cell=" +geneCell + "&detail=" +geneDetail+ "&experiments=" + experiments + "&sources=" + sources + "&sort=" + sortType + "&isLink=false&isHistory=false";
	window.location.href = url;
	});

$(document).on('click','#exp_checkbox',function(){
	var checkboxes = document.getElementsByName('experiments_advanced');
	for(var i = 0; i < checkboxes.length; i++){
		checkboxes[i].checked = this.checked;
	}
});

$(document).on('click','#source_checkbox',function(){
	var checkboxes = document.getElementsByName('sources_advanced');
	for(var i = 0; i < checkboxes.length; i++){
		checkboxes[i].checked = this.checked;
	}
});

$(document).on('click','#expSetting_checkbox',function(){
	var checked = this.checked;
	$("input[name='experiments']").each(function(){
		this.checked = checked;
	});
});

$(document).on('click','#sourceSetting_checkbox',function(){
	var checkboxes = document.getElementsByName('sources');
	for(var i = 0; i < checkboxes.length; i++){
		checkboxes[i].checked = this.checked;
	}
});
//advanced页面etype，source 全选反选
$(document).on('click','#advancedSource_checkbox_mouse',function(){
	var checked = this.checked;
	$("input[name=sources_advanced]:visible").each(function(){
		this.checked = checked;
	});
});
$(document).on('click','#advancedSource_checkbox_human',function(){
	var checked = this.checked;
	$("input[name=sources_advanced]:visible").each(function(){
		this.checked = checked;
	});
});
$(document).on('click','#advancedEtype_checkbox_mouse',function(){
	var checked = this.checked;
	$("input[name=experiments_advanced]:visible").each(function(){
		this.checked = checked;
	});
});
$(document).on('click','#advancedEtype_checkbox_human',function(){
	var checked = this.checked;
	$("input[name=experiments_advanced]:visible").each(function(){
		this.checked = checked;
	});
});

function submitComment(sampleId){
	var text = $("#comment_"+sampleId).val().trim();
	if(text==""){
		$("#comment_"+sampleId).focus();
		return;
	}
	$.ajax({
		type : "post",
		url : rootPath + "/submitComment.htm",
		data : {
				content : text,
				sampleId : sampleId
				},
		dataType: 'html',
		success : function(data){
				if(data == "success")
				{
					showComment(sampleId);
				} else {
					$('#submit_error').css("display","block");
				}
				//$("#commentContent_"+sampleId).html(data);
				//$("#errorspan").text("success");
				//$("#errorspan").css("display","block");
				//$("#submitBtn").attr("disabled",true);
		},
		error : function(data){
			$('#submit_error').css("display","block");
		}
	});
}

function showComment(sampleId){
	$.ajax({
		url : rootPath + "/comment.htm",
		data : {
			sampleId : sampleId
		},
		dataType : 'html',
		type : "post",
		success : function(data){
			$("#commentContent_"+sampleId).html(data);
		},
		error : function(data){
			$('#submit_error').css("display","block");
		}
	});
	var currentDrag = document.getElementById(sampleId+"_Comment");
	initDragDialog(currentDrag, 3);
	$('.drag').not($(currentDrag)).css("z-index","1050");
	$(currentDrag).css("z-index","1060");
	currentDrag.style.display = "block";
	
}

function goSearchVariationGene(){
	var variationGene = $("#searchForm_variation").val().trim();
	if(variationGene == '') {
		 $('#searchForm_variation').focus();
	}
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	var url = rootPath + "/result_variation.htm?page=1&variationGene=" + variationGene  + "&isLink=false&isHistory=false&sort=" + sortType;
	window.location.href = url;
}

//variation上一页
function prevPage_variation() {
	$('.paginate.current').prev().trigger('click_variation');
}
//variation下一页
function nextPage_variation() {
	$('.paginate.current').next().trigger('click_variation');
}

//variation分页
$(document).on('click', '.paginate.variation', function(){
	var page = $(this).html();
	var variationGene = $("#searchForm_variation").val().trim();
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}

	window.location.href = rootPath + "/result_variation.htm?page=" + page + "&variationGene=" + variationGene  + "&isLink=false&isHistory=false&sort=" + sortType;
});

//variation分页
$(document).on('click_variation', '.paginate', function(){
	var page = $(this).html();
	var variationGene = $("#searchForm_variation").val().trim();
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	window.location.href = rootPath + "/result_variation.htm?page=" + page + "&variationGene=" + variationGene  + "&isLink=false&isHistory=false&sort=" + sortType;
});
function highlightTab(id,isJump)
{
	var user = getCookie("jforumSSOCookieNameUser");
	if(user != null){
		var userType = parseInt(user.split(",")[2]);
		//userType大于1的都是试用账户
		if(userType < 2){
		var lis = $("ul.nav.nav-tabs").first().children(); 
			for(var i=0;i<lis.size();i++){
				var _id = $(lis.get(i)).attr("id");
				if(id == _id){
					$("#"+_id).attr("class","choice active");
					$("#tab_content_"+_id).attr("style","display: _block");
				}else{
					if(id != 'geneAdvanced'){
						$("#"+_id).attr("class","disabled");
						$("#tab_content_"+_id).attr("style","display: none");
					}
				}
			}
		}
	
		var callBack = function(result){
			if(result.result){
				if(isJump) {
					if(id == 'gene'){
						//advanced按钮位置
						$("#geneAdvanced_div").attr("style","position: absolute;");
						$("#geneAdvanced_div").css("left",$("#searchForm").position().left);
						goSearch();
					}
					else if(id == 'pathway'){
						goSearchPathWay();
					}
					else if(id == 'multigene'){
						goSearchMultigene();
					}
					else if(id == 'genomicregion'){
						goSearchGenomicRegion();
					}
					else if(id == 'miRNA'){
						goSearch_miRNA();
					}
					else if(id == 'datasetSearch'){
						goSearch_datasetSearch();
					}
					else if(id == 'diseasesRank'){
						goSearchDiseasesRank();
					}
					else if(id == 'geneAdvanced'){
						var geneSymbol = $("#searchForm").val();
						var genome = $('#genome').val().trim();
						var url = rootPath + '/result_geneSearchAdvanced.htm?geneSymbol='+geneSymbol+"&genome="+genome;
						window.open(url);
					}
					else if(id == 'variation'){
						goSearchVariationGene();
					}
					else if(id == 'multiVariants'){
						geneSearch_multiVariants();
					}
				}else{
					if(id == 'pathway'){
						$("#demo").css('display','none');
					}else{
						$("#demo").css('display','block');
					}
				}
			}else{
				//试用用户也可使用gene搜索  不提示错误
				if(id == 'gene') return;
				$('#faildDialog .errorMessage').html(result.message);
				$('#faildDialog').modal({
				    backdrop:true,
				    keyboard:true,
				    show:true
				});
			}
		};
		var url = rootPath + "/user/trialAccount.json";
		sendRequest(url, null, "json", callBack, "");
	}
};

$(document).on('change','#genome',function(){
	var genome = $("#genome").val();
	if(genome == 'Human'){
		$("#source_Human").css("display","block");
		$("#source_Mouse").css("display","none");
		$("#etype_Human").css("display","block");
		$("#etype_Mouse").css("display","none");
		$("#processedCount_Human").css("display","block");
		$("#processedCount_Mouse").css("display","none");
	}else if(genome == 'Mouse'){
		$("#source_Human").css("display","none");
		$("#source_Mouse").css("display","block");
		$("#etype_Human").css("display","none");
		$("#etype_Mouse").css("display","block");
		$("#processedCount_Mouse").css("display","block");
		$("#processedCount_Human").css("display","none");
	}
});

$(document).on('change','#genome_advanced',function(){
	var genome = $("#genome_advanced").val();
	if(genome == 'Human'){
		$("#advanceSource_human").css("display","block");
		$("#advanceSource_mouse").css("display","none");
		$("#advanceEtype_human").css("display","block");
		$("#advanceEtype_mouse").css("display","none");
		
	}else if(genome == 'Mouse'){
		$("#advanceSource_human").css("display","none");
		$("#advanceSource_mouse").css("display","block");
		$("#advanceEtype_human").css("display","none");
		$("#advanceEtype_mouse").css("display","block");
	}
});
function showTab(id){
	if(id != 'gene'){
		$("#geneAdvanced_div").attr("style","display: none");
	}
	var lis = $("ul.nav.nav-tabs").first().children(); 
	for(var i=0;i<lis.size();i++){
		var _id = $(lis.get(i)).attr("id");
		if(id == _id){
			$("#"+_id).attr("class","choice active");
			$("#tab_content_"+_id).attr("style","display: _block");
		}else{
			if(id != 'geneAdvanced'){
				$("#"+_id).attr("class","disabled");
				$("#tab_content_"+_id).attr("style","display: none");
			}
			else if(id == 'geneAdvanced'){
				$("#tab_content_"+id).attr("style","display: _block");
				$("#"+_id).attr("style","display: none");
				$("#tab_content_"+_id).attr("style","display: none");
			}
		}
	}
}

function geneSearch_multiVariants(){
	var multiVariants = "";
	var multiVariants1 = $("#searchForm_multiVariants").val().trim();
	var variant = multiVariants1.split("\n");
	for(var i=0;i<variant.length;i++){
		multiVariants +=variant[i]+",";
	}
	if(multiVariants == ',') {
		 $('#searchForm_multigene').focus();
	}
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	var url = rootPath + "/result_variations.htm?page=1&variations=" + multiVariants  + "&isLink=false&isHistory=false&sort=" + sortType;
	window.location.href = url;
}

//多变异基因上一页
function prevPage_multiVariants() {
	$('.paginate.current').prev().trigger('click_multiVariants');
}
//多变异基因下一页
function nextPage_multiVariants() {
	$('.paginate.current').next().trigger('click_multiVariants');
}


//多变异基因排序
function list_multiVariants_BySort(target){
	var $this = $(target);
	var page = $(this).html();
	var multiVariants = $('#currentMultiVariants').val();  
	var sortType = $this.find('i').hasClass('icon-arrow-up') ? "DESC" : "ASC";
	window.location.href = rootPath + "/result_variations.htm?page=" + page + "&variations=" + multiVariants  + "&isLink=false&isHistory=false&sort=" + sortType;
	if(sortType == "DESC") {
		$this.find('i').removeClass('icon-arrow-up').addClass('icon-arrow-down');
	}else{
		$this.find('i').removeClass('icon-arrow-down').addClass('icon-arrow-up');
	}
}

//多变异基因分页
$(document).on('click_multiVariants', '.paginate', function(){
	var page = $(this).html();
	var multiVariants = $("#searchForm_multiVariants").val().trim();
	var multiVariants1 = "";
	var variants = multiVariants.split("\n");
	for(var i=0;i<variants.length;i++){
		multiVariants1 +=variants[i]+",";
	}
	if(multiVariants1 == ',') {
		 $('#searchForm_multiVariants').focus();
	}
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	window.location.href = rootPath + "/result_variations.htm?page=" + page + "&variations=" + multiVariants1  + "&isLink=false&isHistory=false&sort=" + sortType;
});
$(document).on('click', '.paginate.multiVariants', function(){
	var page = $(this).html();
	var multiVariants = $("#searchForm_multiVariants").val().trim();
	var multiVariants1 = "";
	var variants = multiVariants.split("\n");
	for(var i=0;i<variants.length;i++){
		multiVariants1 +=variants[i]+",";
	}
	if(multiVariants1 == ',') {
		 $('#searchForm_multiVariants').focus();
	}
	var sortType = $('#sortType').val();
	if(!!!sortType) {
		sortType = "ASC";
	}
	window.location.href = rootPath + "/result_variations.htm?page=" + page + "&variations=" + multiVariants1  + "&isLink=false&isHistory=false&sort=" + sortType;
});

//multiVariation you can also search singleGene..
$(document).on('click', '.linkRow .linkWord_multiVariation', function(){
	var key = $(this).html();
	if(key != '') {
		key = key.replace(',','');
		var sortType = $('#sortType').val();
		var url = rootPath + "/result_variation.htm?page=1&variationGene=" + key  + "&isLink=false&isHistory=false&sort=" + sortType;
		window.open(url);
	}
});