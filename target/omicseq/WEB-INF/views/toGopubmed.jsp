<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/tags.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src="${ctx}/static/js/jquery.js"></script>
<title>Insert title here</title>
</head>

<script type="text/javascript">
$(document).ready(function(){
	//alert($("#mot").val());
	$("#myform1").submit();
	//$("#chercher").trigger("click");
});
</script>

<body>
	<div style="display:block">
		<form  method="post" action="http://www.gopubmed.com/web/gopubmed/20?WEB03fhaotkckb65I1I1I00i00101010e10021000400.y" id="myform1" onsubmit="return true">
			<input type="hidden"value="${u1 }" name="00i00101010e1002100040120000102" class="gui_textinput  MainSearchInput" id="u1">
		</form>
	
	</div>
	
</body>
</html>