<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/tags.jsp"%>
<!DOCTYPE html>
<html>
<head>
<META HTTP-EQUIV="pragma" CONTENT="no-cache"> 
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate"> 
<META HTTP-EQUIV="expires" CONTENT="Wed, 26 Feb 1997 08:21:57 GMT"> 
<title>Comments</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet" href="http://mat1.gtimg.com/www/coral2.0/css/coral_v9.6.5.css?v20">
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
		function submitComment(){
			var path = '${ctx}';
			var text = $("#userComment").val().trim();
			if(text==""){
				$("#userComment").focus();
				return;
			}
			if('${user}'==""){
				$("#errorspan").text("please login");
				$("#errorspan").css("display","block");
				setInterval(function(){
					$("#errorspan").css("display","none");
				}, 3000);
				return;
			}
			$.ajax({
				type : "post",
				url : path + "/submitComment.htm",
				data : {content : text,
					sampleId : '${sampleId}'},
				dataType:String,
				success : function(data){
					$("#errorspan").text("success");
					$("#errorspan").css("display","block");
					$("#submitBtn").attr("disabled",true);
				}
			});
		}
	</script>
	<script type="text/javascript">
		var _bdhmProtocol = (("https:" == document.location.protocol) ? " https://" : " http://");
		document.write(unescape("%3Cscript src='" + _bdhmProtocol + "hm.baidu.com/h.js%3Fa06bf362a4238726a57e308ed1075652' type='text/javascript'%3E%3C/script%3E"));
	</script>
</head>
<body style="width: 80%; margin: auto;">
	<div id="commentArea" class="out" style="margin-top: 10px;">
		<div id="np-reply-box" class="np-reply-box blueLight np-reply-box-active" style="height:150px">
			<div class="np-reply-box-content textarea">
				<textarea id="userComment" accesskey="u" name="content" tabindex="1" style="height: 90px; padding: 10px;"></textarea><br/>
			</div>
			<div id="p_login_btn" class="commtSub np-reply-box-footer" style="position:relative;display:block;height:40px">
				<div class="submitBtn">
					<span class="np-tip-error" style="display:none;" id="errorspan"><fmt:message key="button.publishsuccess"/></span>
					<button id="top_post_btn" class="np-btn np-btn-submit" onclick="submitComment();"><fmt:message key="button.publish"/></button>
				</div>
			</div>
		</div>
	</div>
	<div id="allComments">
		<ul class="post-list np-comment-list" >
			<c:if test="${comments.size()!=0 }">
			<li class="np-title-hot">热门评论</li>
			<c:forEach items="${comments }" var="item">
			<li class="np-post topAll " style="padding-left: 0px;">
				<div class="np-post-body">
					<div class="np-post-header">
						<span class="np-user popClick " style="padding-left: 0px;" >${item.userName }</span>
						<div class="np-post-content" data-height="5">
							<p>${item.content }</p>
						</div>
					</div>
				</div>
			</li>
			</c:forEach>
			</c:if>
		</ul>
	</div>
	
</body>
</html>