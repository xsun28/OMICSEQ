<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/common/tags.jsp"%>
<!DOCTYPE html>
<html lang="en">

<head>

  <title>Omicseq Search Engine </title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<link type="text/css" rel="stylesheet" href="${ctx}/static/bootstrap/css/bootstrap.min.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/reset.min.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/result.min.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/custom.min.css">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/css/jquery-ui.min.css">
		<!-- Custom CSS -->
		<link type="text/css" rel="stylesheet" href="${ctx}/static/fancybox/jquery.fancybox-1.3.4.css">
	<style>
	#page-content{
		margin: 0 auto;
		width: 85%;
	}
.title{

 margin-bottom:2em;
		font-size:30px;
		font-weight: bold;
	margin-top:2em;
}
p{
		line-height: 2;
		margin-bottom: 1em;
		font-size: 20px;
		
		
	}
	.subtitle{
		font-size: 23px;
	
		font-style: italic;
		background-color: aqua;
	}
		#first-paragraph::first-letter{
		font-size: xx-large;
	}
	.block_view{
		background-color: #f4f4f4;
		display:block;
		padding: 1em 0em 1.6em 2em;
		margin-bottom: 4em;
	}
	
		p{
		line-height: 2.5;
		margin-bottom: 1em;
		font-size: 20px;
		
		
	}
	.block_view p{
		width:80%;
	}
	
	li > p{
	font-size:17px;	
	}
	
	
	i {
		color:blue;
		font-size:20px;
	}
	#gallery {
		margin-right: 2em;
		width:80%;
		height:100%;
		border-style:groove;
	}

	.fade {
   opacity: 1;
   transition: opacity .25s ease-in-out;
   -moz-transition: opacity .25s ease-in-out;
   -webkit-transition: opacity .25s ease-in-out;}

      .fade:hover {
       opacity: 0.6;}
	
	.p_image{
		margin:auto auto 1.6em 0;
		width:80%;
		
	}
	p.subtitle1{
		margin-bottom: 0;
		color:mediumpurple;
	}
.color-table  {

        display: inline-block;
                width:18%;
		margin: 0 2em 0 2em;
        }
        .color-text {
         text-align: center;
        }
        .color-table tr td{
        boder: 2px solid black;
        }	
	</style>
 
    <!-- jQuery -->
   
    <script type="text/javascript" src="${ctx}/static/js/jquery-1.7.2.min.js"></script>
		<script type="text/javascript" src="${ctx}/static/bootstrap/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/validation.js"></script>
		<script type="text/javascript" src="${ctx}/static/js/jquery-ui.js"></script>
		<script type="text/javascript" src="${ctx}/static/script/common.js"></script>
		<script type="text/javascript" src="${ctx}/static/script/main.js"></script>
		<script type="text/javascript" src="${ctx}/static/fancybox/jquery.fancybox-1.3.4.min.js"></script>
	
    <script>
    var rootPath = '';
    $(document).ready(function(){
    rootPath = '${ctx}';
    $('#gallery a').fancybox({
        overlayColor: '#060',
        overlayOpacity: .3,
        transitionIn: 'elastic',
        transitionOut: 'elastic'
});
    });
    </script>
</head>

<body>

	<%@ include file="../common/tutorial_header.jsp"%>
<div style="width:100%;height:80px;"></div>
	
    <div id="wrapper">

     <div id="page-content">
                   
                   	<p style="text-align:center;border-bottom-style: solid;border-bottom-color: blue"><img class="src_replace" id="search_logo"  style="width: 300px;height: 75px;" src="static/images/banner.png" ></p>
                       <div class="title"> 
                     Omicseq Tutorial 
                      
                       </div>
                     <div class="subtitle">
                      	1. Tutorial Video
                      </div>
                      <div class="block_view">
<iframe width="560" height="315" src="https://www.youtube.com/embed/tfmjh6ADVu0" frameborder="0" allowfullscreen></iframe>   
</div> 
                      <div class="subtitle">
                      	2. What is Omicseq?
                      </div>
                       <div class="block_view">
						   <p id="first-paragraph"> Omicseq is a web portal that serves as an omics data explorer and a search engine. 
						   It retrieves and processes various types of omics data (RNA-seq, ChIP-seq, ATAC-seq) from multiple major genomic data
						    repositories such as ENCODE,TCGA, etc. Unlike most of the existing dataset search tools which exclusively relies on metadata, our search engine is powered by a ranking algorithm that fully utilize numerical values in the dataset. When query a gene or a pathway name, metadata will not be much helpful, but the numerical values inside the dataset about the gene or the pathway say a lot about whether the dataset is relevant to the query. The trackRank algorithm we developed, harnessing the numerical information of all the genes or pathways to come up with a rank order for each dataset. And the ones rank on top will be output to the users.  
						   </p>
						    <p> It provides two search services:</p>
						   <ul>
						   	<li><i> Gene Search</i><p>
						   		Gene can be searched by its name or NCBI RefSeq number. Related datasets are returned and ranked.
						   	</p></li>
						   	<li><i>Pathway Search</i>
						   	<p>
						   		Gene Pathway can be search by its name, and datasets of gene involved in the pathway are returned and ranked.
						   	</p>
						   	</li>
						   </ul> 
						</div>	
                    
                        <div class="subtitle">3. Gene Search Interface</div>
                    <div class="block_view">
                    
                    <p class="p_image">
                    <p class="subtitle1"> 1) Basic Interface</p>
                     <div id="gallery">
		<a class="href_replace" href="static/demoimages/gene.png"  title="Gene Search Bar"><img class="src_replace fade" src="${ctx}/static/demoimages/gene1.png" width="100%" height="100%" alt="Gene Search Bar"></a>
		</div>
						</p>
                  
                   <p>  This figure shows the basic interface of gene search which is activated by clicking the <i>"Gene"</i> tab on the search bar.
                     There are two required input parameters: 1. Gene name; 2. Database (human or mouse). The search bar can give hints on the gene name when entering the first character(s). </p>
                    
                      <p class="p_image">
                       <p class="subtitle1"> 2) Advanced Settings </p>
                     <div id="gallery">
		<a class="href_replace" href="static/demoimages/advanced_setting.png"  title="Advanced Setting"><img class="src_replace fade" src="${ctx}/static/demoimages/advanced_setting.png" width="100%" height="100%" alt="Advanced Setting"></a>
		</div>
						</p>
                     
                     <p> Advanced parameters can be specified by clicking the <i>"Setting"</i> button which includes:</p>
                     <ul>
                     	<li>
                     		
                     		<p> Experiments Types:  	
                     		Datasets under search can be filtered by setting a filter on experiments types such as ChIP-seq TF, Dnase-seq, GWAS, etc.</p>
                     	</li>
                     	<li>
                     		<p>Data Source: 
                     		Datasets can also be filtered by setting a filter on  datasoruces such as ArrayExpress, TCGA, GEA, etc.</p>
                     	
                     	</li>
                     </ul>
                     
				
                   
                     </div>
                      <div class="subtitle">2. An Example of Gene Search Result</div>
                     <div class="block_view">
                    <p class="p_image">
                      <div id="gallery">
		<a  class="href_replace" href="static/demoimages/gene2.png"  title="Gene Search Sample Results"><img class="src_replace fade" src="${ctx}/static/demoimages/gene2.png" width="100%" height="100%" alt="Gene Search Sample Results"></a>
						 </div>    
                  </p>
                   
                    <p> This figure shows the search results on gene KLK3, the gene coded for the prostate antigen (PSA) protein. Relevant datasets are returned and ranked as rows including following fields:</p>
                     <ul>
                     	<li>
                     		<p>Rank: This field shows the relevance of the gene in the dataset. The higher the rank , the more relevant the dataset is considered to the query gene.</p>
                     	</li>
                     	<li>
                     		<p>DatasetID: The identifier of the dataset.</p>
                     	</li>
                     	<li>
                     		<p>DataType: The experiment type on the dataset.</p>
                     	</li>
                     	<li>
                     		<p>Sample: The experimental sample.</p> 
                     	</li>
                     	<li>
                     		<p>Tissue/status/factor: The experimental tissue.</p> 
                     	</li>
                     	<li>
                     		<p>Order/Total: Order of the query gene in this dataset/total number of genes have scored in this dataset.</p>
                     	</li>
                     	<li>
                     		<p>Percentile: The percentile of the query gene among all genes in the genome in terms of the scores in this dataset.</p>
                     	</li>
                     	<li>
                     	<p>Study: The datasource.</p>
                     	</li>
                     	<li>
                     		<p>Lab: The university/institution which the contributing lab(s) belong to. </p>
                     	</li>
                     	<li>
                     		<p>More Info: Metainformation and links to related information on other websites. </p>
                     	</li>
                     </ul>
                      <p> For this example, as we can see, vast majority of the top ranked datasets are RNA-seq data collected from prostate cancer patients in the TCGA study. In almost all these datasets, klk3 gene shows up as the highest expressed gene in the entire genome, which speaks volume of its prominence as the biomarker for prostate cancer. 
					</p>
			<P> Note: colors are used on Data Type and Study columns to indicate categories. Please refer to the following color table</P>
						
						<table class ="color-table" >
						<tr>
						<th>Color</th>
						<th>Data Types</th>
						</tr>
						<tr>
						
						<td style="background-color:#fcfeb8"></td>
						<td class="color-text" >ChIP-seq</td>
						</tr>
						<tr>
						
						<td style="background-color:#fdc7c7"></td>
						<td class="color-text" >RNA-seq</td>
						</tr>
						<tr>
						
						<td style="background-color:#B0E0E6"></td>
						<td class="color-text" >CNV</td>
						</tr>
						<tr>
						
						<td style="background-color:#858EFA"></td>
						<td class="color-text" >Methylation</td>
						</tr>
						<tr>
						
						<td style="background-color:#8C8D8A"></td>
						<td class="color-text" >Microarray</td>
						</tr>
						<tr>
						
						<td style="background-color:#A6D377"></td>
						<td class="color-text" >Dnase-seq</td>
						</tr><tr>
						
						<td style="background-color:#AEE640"></td>
						<td class="color-text" >Summary Track</td>
						</tr><tr>
						
						<td style="background-color:#4292D1"></td>
						<td class="color-text" >Somatic Mutations</td>
						</tr>
						</table>
						
						
						<table class ="color-table" >
						<tr>
						<th>Color</th>
						<th>Studies</th>
						</tr>
						<tr>
						
						<td style="background-color:#fcfeb8"></td>
						<td class="color-text" >ENCODE</td>
						</tr>
						<tr>
						
						<td style="background-color:#fdc7c7"></td>
						<td class="color-text" >TCGA</td>
						</tr>
						<tr>
						
						<td style="background-color:#FA7777"></td>
						<td class="color-text" >TCGA Firebrowse</td>
						</tr>
						<tr>
						
						<td style="background-color:#caef90"></td>
						<td class="color-text" >ICGC</td>
						</tr>
						<tr>
						
						<td style="background-color:#eedded"></td>
						<td class="color-text" >SRA</td>
						</tr>
						<tr>
						
						<td style="background-color:#d7eaf8"></td>
						<td class="color-text" >Epigenome Roadmap</td>
						</tr><tr>
						
						<td style="background-color:#B2B2C2"></td>
						<td class="color-text" >GEO</td>
						</tr><tr>
						
						<td style="background-color:#9F80A0"></td>
						<td class="color-text" >CCLE</td>
						</tr><tr>
						
						<td style="background-color:#47D892"></td>
						<td class="color-text" >SUMMARY</td>
						</tr><tr>
						
						<td style="background-color:#D8C647"></td>
						<td class="color-text" >ArrayExpress</td>
						</tr>
						</table>	
                     </div>
                     
                      <div class="subtitle">4. Pathway Search Interface</div>
                    <div class="block_view">
                    
                    <p class="p_image">
                    <p class="subtitle1"> 1) Basic Interface</p>
                     <div id="gallery">
		<a class="href_replace" href="static/demoimages/pathway1.png"  title="Pathway Search Bar"><img class="src_replace fade" src="static/demoimages/pathway1.png" width="100%" height="100%" alt="Pathway Search Bar"></a>
		</div>
						</p>
                  		<p>This figure shows the basic interface of pathway search which is activated by clicking the <i>"Pathway"</i> button on the search bar.
                        Datasets related to a pathway of interest can be searched by pathway name on hg19 database. The search bar also gives hints on pathway names given first character(s). </p>
                    
                      <p class="p_image">
                       <p class="subtitle1"> 2) Advanced Settings </p>
                     <div id="gallery">
		<a class="href_replace" href="static/demoimages/advanced_setting.png"  title="Advanced Setting"><img class="src_replace fade" src="static/demoimages/advanced_setting.png" width="100%" height="100%" alt="Advanced Setting"></a>
		</div>
						</p>
                     
                     <p> Advanced parameters can be specified by clicking the <i>"Setting"</i> button which includes:</p>
                     <ul>
                     	<li>
                     		
                     		<p> Experiments Types:  	
                     		Datasets under search can be filtered by setting a filter on experiments types such as ChIP-seq TF, Dnase-seq, GWAS, etc.</p>
                     	</li>
                     	<li>
                     		<p>Data Source: 
                     		Datasets can also be filtered by setting a filter on  datasoruces such as ArrayExpress, TCGA, GEA, etc.</p>
                     	
                     	</li>
                     </ul>
</div>
                      <div class="subtitle">4. An Example of Pathway Search Result</div>
                     <div class="block_view">
                    <p class="p_image">
                      <div id="gallery">
		<a  class="href_replace" href="static/demoimages/pathway2.png"  title="Pathway Search Example Results"><img class="src_replace fade" src="static/demoimages/pathway2.png" width="100%" height="100%" alt="Pathway Search Example Results"></a>
						 </div>    
                  </p>
                   
                    <p> This figure shows the search results on apoptotic program. Relevant datasets are returned and ranked as rows including following fields:</p>
                     <ul>
                     	<li>
                     		<p>Rank: This field shows the relevance of the pathway in the dataset. The higher the rank , the more relevant the dataset is considered to the query pathway.</p>
                     	</li>
                     	<li>
                     		<p>DatasetID: The identifier of the dataset.</p>
                     	</li>
                     	<li>
                     		<p>DataType: The experiment type on the dataset.</p>
                     	</li>
                     	<li>
                     		<p>Sample: The experimental sample.</p> 
                     	</li>
                     	<li>
                     		<p>Tissue/status/factor: The experimental tissue.</p> 
                     	</li>
                     	<li>
                     		<p>Average: Average score of all the genes in this pathway.</p>
                     	</li>
                     	<li>
                     		<p>Cumulative: Total scores of all the genes in this pathway.</p>
                     	</li>
                     	<li>
                     	<p>Percentile: Percentile of this pathway among all pathways considered in terms of the cumulative scores.</p>
                     	</li>
                     	<li>
                     	<p>Study: The datasource.</p>
                     	</li>
                     	<li>
                     		<p>Lab: The university/institution which the contributing lab(s) belong to. </p>
                     	</li>
                     	<li>
                     		<p>More Info: Metainformation and links to related information on other websites. </p>
                     	</li>
</ul>
				 <P> Note: colors are used on Data Type and Study columns to indicate categories. Please refer to the following color table</P>
						
						<table class ="color-table" >
						<tr>
						<th>Color</th>
						<th>Data Types</th>
						</tr>
						<tr>
						
						<td style="background-color:#fcfeb8"></td>
						<td class="color-text" >ChIP-seq</td>
						</tr>
						<tr>
						
						<td style="background-color:#fdc7c7"></td>
						<td class="color-text" >RNA-seq</td>
						</tr>
						<tr>
						
						<td style="background-color:#B0E0E6"></td>
						<td class="color-text" >CNV</td>
						</tr>
						<tr>
						
						<td style="background-color:#858EFA"></td>
						<td class="color-text" >Methylation</td>
						</tr>
						<tr>
						
						<td style="background-color:#8C8D8A"></td>
						<td class="color-text" >Microarray</td>
						</tr>
						<tr>
						
						<td style="background-color:#A6D377"></td>
						<td class="color-text" >Dnase-seq</td>
						</tr><tr>
						
						<td style="background-color:#AEE640"></td>
						<td class="color-text" >Summary Track</td>
						</tr><tr>
						
						<td style="background-color:#4292D1"></td>
						<td class="color-text" >Somatic Mutations</td>
						</tr>
						</table>
						<table class ="color-table" >
						
						<tr>
						<th>Color</th>
						<th>Studies</th>
						</tr>
						<tr>
						
						<td style="background-color:#fcfeb8"></td>
						<td class="color-text" >ENCODE</td>
						</tr>
						<tr>
						
						<td style="background-color:#fdc7c7"></td>
						<td class="color-text" >TCGA</td>
						</tr>
						
						<tr>
						
						<td style="background-color:#caef90"></td>
						<td class="color-text" >ICGC</td>
						</tr>
						<tr>
						
						<td style="background-color:#eedded"></td>
						<td class="color-text" >SRA</td>
						</tr>
						<tr>
						
						<td style="background-color:#d7eaf8"></td>
						<td class="color-text" >Epigenome Roadmap</td>
						</tr><tr>
						
						<td style="background-color:#B2B2C2"></td>
						<td class="color-text" >GEO</td>
						</tr>
						<tr>
						<td style="background-color:#9F80A0"></td>
						<td class="color-text" >CCLE</td>
						</tr>
						</table>
                     </div>
                     
                     
	</div>     
<%@ include file="../common/footer.jsp"%>
</body>

</html>
	
