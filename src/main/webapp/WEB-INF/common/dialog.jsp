<div id="successDialog" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	    <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
	        <h3 id="myModalLabel"><fmt:message key="label.tip"/></h3>
	    </div>
	    <div class="modal-body">
	        <div class="alert alert-success">
		      	  <fmt:message key="label.success"/>
		    </div>
	    </div>
	</div>
	<div id="confirmDialog" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	    <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
	        <h3 id="myModalLabel"><fmt:message key="label.tip"/></h3>
	    </div>
	    <div class="modal-body">
	        <div class="alert alert-danger fade in">
		        <p id="confirmMessage"></p>
		        <p>
		          <button class="btn btn-danger" type="button" data-dismiss="modal" aria-hidden="true" onclick="updateHistory();"><fmt:message key="button.override"/></button>
		          <button class="btn btn-default" type="button" data-dismiss="modal" aria-hidden="true"><fmt:message key="button.cancel"/></button>
		        </p>
		      </div>
	    </div>
	</div>
	<div id="faildDialog" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	    <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
	        <h3 id="myModalLabel"><fmt:message key="label.tip"/></h3>
	    </div>
	    <div class="modal-body">
	        <div class="errorMessage alert alert-danger">
		      	
		    </div>
	    </div>
	</div>