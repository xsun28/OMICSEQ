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
	<table width="900">
		<tbody><tr>
		<td align="center"><input type="hidden" size="60" value="${geneSymbol }" name="mot" id="mot"></td>
		</tr>
		<tr><td>&nbsp;</td></tr>
		
		<tr><td align="center">
		<table>
		<tbody><tr><td>Chr : </td><td>
		<select name="chr">
		<option></option>
		<option value="1">1</option>
		<option value="2">2</option>
		<option value="3">3</option>
		<option value="4">4</option>
		<option value="5">5</option>
		<option value="6">6</option>
		<option value="7">7</option>
		<option value="8">8</option>
		<option value="9">9</option>
		<option value="10">10</option>
		<option value="11">11</option>
		<option value="12">12</option>
		<option value="13">13</option>
		<option value="14">14</option>
		<option value="15">15</option>
		<option value="16">16</option>
		<option value="17">17</option>
		<option value="18">18</option>
		<option value="19">19</option>
		<option value="20">20</option>
		<option value="21">21</option>
		<option value="22">22</option>
		<option value="X">X</option>
		<option value="Y">Y</option>
		</select></td><td class="th1"> Start (in kb) : <input type="TEXT" value="" size="8" name="debchr"></td><td class="th1"> End (in kb) : <input type="TEXT" value="" size="8" name="finchr"></td></tr></tbody></table>
		</td></tr>
		<tr>
		<td align="center"><div id="liste_mot_gene">
		</div></td>
		</tr>
		</tbody>
	</table>
</form>
</div>
</body>
</html>