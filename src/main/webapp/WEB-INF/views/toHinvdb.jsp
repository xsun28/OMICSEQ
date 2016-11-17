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
	$("#myform2").submit();
	//$("#chercher").trigger("click");
});
</script>

<body>
	<div style="display: none">
	<form style="margin-bottom:0" target="_top" action="http://jbirc.jbic.or.jp/hinv/c-search/keyidsearch.do" method="POST" name="keyidSearchForm1" id="myform2">
					<strong>Search by</strong>
					<select class="form_list" name="KEN_TYPE">
					<option selected="" value="Keyword">Keyword</option>
					<option value="H-INVITATIONAL-ID">H-Inv ID (HIT)</option>
					<option value="cDNAXML_CLUSTER-ID">H-Inv cluster ID (HIX)</option>
					<option value="CDNA_H-INVITATIONAL-PROTEIN-ID">H-Inv protein ID (HIP)</option>
					<option value="GENE-FAMILY_HIF-ID">H-Inv gene family/group (HIF)</option>
					<option value="ACCESSION-NO">Accession number</option>
					<option value="cDNAXML_CHROMOSOME-NUMBER">Chromosome number</option>
					<option value="CHROMOSOME-BAND">Chromosome band</option>
					<option value="DATA-SOURCE_DEFINITION">Definition*</option>
					<option value="DATA-SOURCE_DB-REFERENCE_PROTEIN-MOTIF-ID">Data source ID</option>
					<option value="---">---</option>
					<option value="CDNA_DB-REFERENCE_CCDS">CCDS ID</option>
					<option value="SNP_DB-REFERENCE_DBSNP">dbSNP ID (rs number)</option>
					<option value="EC-NO_DB-REFERENCE_H-INV">EC number</option>
					<option value="CDNA_DB-REFERENCE_ENSEMBL">Ensembl ID</option>
					<option value="CDNA_DB-REFERENCE_ENTREZGENE">EntrezGene ID</option>
					<option value="FRNADB_FR_ID">FR ID</option>
					<option value="FRNADB_FR_ACC">FR Accession number</option>
					<option value="GO_DB-REFERENCE_GO">GO ID</option>
					<option value="GO_TERM">GO name*</option>
					<option value="HUGO-APPROVED-GENE-SYMBOL_GENEW">HGNC gene symbol</option>
					<option value="APPROVED-NAME_GENEW">HGNC gene name*</option>
					<option value="DOMAIN_DB-REFERENCE_INTERPRO">InterPro ID</option>
					<option value="DOMAIN_NAME">InterPro name*</option>
					<option value="CDNA-OMIM_DB-REFERENCE_OMIM">OMIM ID</option>
					<option value="CDNA-OMIM_DISEASE_DISEASE-NAME">OMIM title*</option>
					<option value="PATHWAY-ID_DB-REFERENCE_KEGG">Pathway ID</option>
					<option value="PATHWAY-NAME_DB-REFERENCE_KEGG">Pathway name*</option>
					<option value="CDNA_DB-REFERENCE_REFSEQ">RefSeq (gene) ID</option>
					<option value="CDNA_DB-REFERENCE_REFSEQ-PROTEIN-ID">RefSeq (protein) ID</option>
					<option value="STRUCTURE_DOMAIN_DB-REFERENCE_SCOP">SCOP ID</option>
					<option value="CDNA_DB-REFERENCE_UNIPROT-PROTEIN-ID">UniProt</option>
					</select>
					<strong>for</strong>
					<input type="text" value="${KEN_STR }" class="form_text" name="KEN_STR" maxlength="950" size="15">
					<input type="submit" class="form_go" value="GO">
					<a target="_top" class="lk2" href="../c-search/gotocrssearch.do"><strong>Advanced Search</strong></a>
					<input type="hidden" value="0" name="KEN_INDEX">
				</form>
	
</body>
</html>