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
    	<link href="${ctx}/static/css/simple-sidebar.css" rel="stylesheet">
		<link type="text/css" rel="stylesheet" href="${ctx}/static/fancybox/jquery.fancybox-1.3.4.css">
	<style>
	#sidebar-wrapper, #sidebar-wrapper li {
	background-color:lightblue;
	}
	#sidebar-wrapper li.sidebar-brand, #sidebar-wrapper li.sidebar-brand a {
	background-color:black;
	color:white;
	padding: 0px;
	margin: 0px;
	}
	#sidebar-wrapper a{
	background-color: lightblue;
	text-decoration: none;
	font: 20px, Lucida Console, Monospace;
	text-indent: 20px;
	border-top: 1px solid #ccc;
	border-left:1px solid #ccc;
	border-right:1px solid #999;
	border-bottom:1px solid #999;
	margin: 0px;
	padding: 0px;
	color: black;
	}
	#sidebar-wrapper a.service:hover{
	border-top:1px solid #999;
	border-left:1px solid #999;
	border-right:1px solid #ccc;
	border-bottom:1px solid #ccc;
	background-color: #555;
	color: white;
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
    var overview_url=$('#overview').attr("href");
    $('#page-content-wrapper').load(overview_url);
    $('#sidebar-wrapper a').click(function(){
    var url=$(this).attr("href");
    $('div.col-lg-12').load(url,function(responseTxt, statusTxt, xhr){
    	 $('#gallery a').fancybox({
             overlayColor: '#060',
             overlayOpacity: .3,
             transitionIn: 'elastic',
             transitionOut: 'elastic'
     });


    });
    return false;
    });
    });
    </script>
</head>

<body>

	<%@ include file="../common/tutorial_header.jsp"%>

	
    <div id="wrapper">

        <!-- Sidebar -->
        <div id="sidebar-wrapper">
            <ul class="sidebar-nav">
                <li class="sidebar-brand">
                    <a href="#">
                     Omicseq Tutorial   
                    </a>
                </li>
                <li>
                    <a href="${ctx}/static/html/overview.html" class="service" id="overview">Overview</a>
                </li>
                <li>
                    <a href="${ctx}/static/html/geneTutorial.html" class="service">Gene</a>
                </li>
                <li>
                    <a href="${ctx}/static/html/mirnaTutorial.html" class="service">miRNA</a>
                </li>
                <li>
                    <a href="${ctx}/static/html/pathwayTutorial.html" class="service">Pathway</a>
                </li>
                <li>
                    <a href="${ctx}/static/html/genomicRegionTutorial.html" class="service">Genomic Region</a>
                </li>
                <li>
                    <a href="${ctx}/static/html/datasetSearchTutorial.html" class="service">Dataset Search</a>
                </li>
                <li>
                    <a href="${ctx}/static/html/diseasesRankTutorial.html" class="service">Diseases Rank</a>
                </li>
                 <li>
                    <a href="#" class="service">Variants</a>
                </li>
                 <li>
                    <a href="#" class="service">MultiVariants</a>
                </li>
            </ul>
        </div>
        <!-- /#sidebar-wrapper -->
		
        <!-- Page Content -->
        <div id="page-content-wrapper">
            <div class="container-fluid">
                <div class="row">
                    <div class="col-lg-12">
                    
                        <!-- <h1>Tutorial Overview</h1>
                        <p> Omicseq TM project provides several search services on whole genome data including: </p>
                        <p>Make sure to keep all page content within the <code>#page-content-wrapper</code>.</p>
                        <a href="#menu-toggle" class="btn btn-default" id="menu-toggle">Toggle Menu</a> -->
                    </div>
                </div>
            </div>
        </div>
        <!-- /#page-content-wrapper -->

    </div>
    <!-- /#wrapper -->


    <!-- Menu Toggle Script -->
    <script>
    $("#menu-toggle").click(function(e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });
    </script>
<%@ include file="../common/footer.jsp"%>
</body>

</html>
	
