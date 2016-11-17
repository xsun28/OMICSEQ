<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/tags.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src="${ctx}/static/js/jquery.js"></script>
<title> Omics Search Engine </title>
</head>
<script type="text/javascript">
$(document).ready(function(){
	//alert($("#mot").val());
	$("#myform").submit();
	//$("#chercher").trigger("click");
});

</script>

<body>

<div style="display: none">
<form action="http://genatlas.medecine.univ-paris5.fr/imagin/go_gene.php" name="genatlas" method="POST" id="myform">
	<input type="hidden" value="" name="thesaurus">
	<input type="hidden" size="60" value="${mot }" name="mot" id="mot">
</form>
</div>
</body>
</html>