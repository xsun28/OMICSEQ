/*-------------------------- +
  获取id, class, tagName
 +-------------------------- */
var get = {
	byId: function(id) {
		return typeof id === "string" ? document.getElementById(id) : id;
	},
	byClass: function(sClass, oParent) {
		var aClass = [];
		var reClass = new RegExp("(^| )" + sClass + "( |$)");
		var aElem = this.byTagName("*", oParent);
		for (var i = 0; i < aElem.length; i++) reClass.test(aElem[i].className) && aClass.push(aElem[i]);
		return aClass;
	},
	byTagName: function(elem, obj) {
		return (obj || document).getElementsByTagName(elem);
	}
};
/*-------------------------- +
  拖拽函数
 +-------------------------- */
function drag(oDrag, handle)
{
	var disX = dixY = 0;
	var oMin = get.byClass("min", oDrag)[0];
	var oMax = get.byClass("max", oDrag)[0];
	var oRevert = get.byClass("revert", oDrag)[0];
	var oClose = get.byClass("closeDialog", oDrag)[0];
	var dragMinWidth = $(oDrag).attr('minWidth');
	var dragMinHeight = $(oDrag).attr('minHeight');
	handle = handle || oDrag;
	handle.style.cursor = "move";
	handle.onmousedown = function (e)
	{
		var event = e || window.event;
		disX = event.clientX - oDrag.offsetLeft;
		disY = event.clientY - oDrag.offsetTop;
		$('.drag').not($(oDrag)).css("z-index","1050");
		$(oDrag).css("z-index","1060");
		document.onmousemove = function (e)
		{
			var event = e || window.event;
			var iL = event.clientX - disX;
			var iT = event.clientY - disY;
			var maxL = document.documentElement.clientWidth - oDrag.offsetWidth;
			var maxT = document.documentElement.clientHeight - oDrag.offsetHeight;
			
			iL <= 0 && (iL = 0);
			iT <= 55 && (iT = 55);
			iL >= maxL && (iL = maxL);
			iT >= maxT && (iT = maxT);
			
			oDrag.style.left = iL + "px";
			oDrag.style.top = iT + "px";
			
			return false;
		};
		
		document.onmouseup = function ()
		{
			document.onmousemove = null;
			document.onmouseup = null;
			this.releaseCapture && this.releaseCapture();
		};
		this.setCapture && this.setCapture();
		return false;
	};	
	//最大化按钮
	oMax.onclick = function ()
	{
		oDrag.style.top = 0;
		oDrag.style.left = 0;
		oDrag.style.width = document.documentElement.clientWidth - 2 + "px";
		oDrag.style.height = document.documentElement.clientHeight - 2 + "px";
		this.style.display = "none";
		oRevert.style.display = "block";
	};
	//还原按钮
	oRevert.onclick = function ()
	{		
		oDrag.style.width = dragMinWidth + "px";
		oDrag.style.height = dragMinHeight + "px";
		oDrag.style.left = (document.documentElement.clientWidth - oDrag.offsetWidth) / 2 + "px";
		oDrag.style.top = (document.documentElement.clientHeight - oDrag.offsetHeight) / 2 + "px";
		this.style.display = "none";
		oMax.style.display = "block";
	};
	//最小化按钮
	oMin.onclick = oClose.onclick = function ()
	{	
		oDrag.style.display = "none";
		$(oDrag).css("z-index","1050");
		var target = this.getAttribute("targetoA");
		var oA = document.getElementById(target);
		oA.style.display = "block";
	};
	//阻止冒泡
	oMin.onmousedown = oMax.onmousedown = oClose.onmousedown = function (event)
	{
		this.onfocus = function () {this.blur();};
		(event || window.event).cancelBubble = true	;
	};
}
function showDialog(oA) {
	var callBack = function(result){
		if(result.result){
			var dialogId = oA.getAttribute("targetDrag");
			var oDrag = document.getElementById(dialogId);
			$('.drag').not($(oDrag)).css("z-index","1050");
			$(oDrag).css("z-index","1060");
			oDrag.style.display = "block";
			oA.style.display = "none";
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
/*-------------------------- +
  改变大小函数
 +-------------------------- */
function resize(oParent, handle, isLeft, isTop, lockX, lockY)
{
	handle.onmousedown = function (e)
	{
		var event = e || window.event;
		var disX = event.clientX - handle.offsetLeft;
		var disY = event.clientY - handle.offsetTop;	
		var iParentTop = oParent.offsetTop;
		var iParentLeft = oParent.offsetLeft;
		var iParentWidth = oParent.offsetWidth;
		var iParentHeight = oParent.offsetHeight;
		
		document.onmousemove = function (e)
		{
			var event = e || window.event;
			
			var iL = event.clientX - disX;
			var iT = event.clientY - disY;
			var maxW = document.documentElement.clientWidth - oParent.offsetLeft - 2;
			var maxH = document.documentElement.clientHeight - oParent.offsetTop - 2;			
			var iW = isLeft ? iParentWidth - iL : handle.offsetWidth + iL;
			var iH = isTop ? iParentHeight - iT : handle.offsetHeight + iT;
			var dragMinWidth = $(oParent).attr('minWidth');
			var dragMinHeight = $(oParent).attr('minHeight');
			isLeft && (oParent.style.left = iParentLeft + iL + "px");
			isTop && (oParent.style.top = iParentTop + iT + "px");
			
			iW < dragMinWidth && (iW = dragMinWidth);
			iW > maxW && (iW = maxW);
			lockX || (oParent.style.width = iW + "px");
			
			iH < dragMinHeight && (iH = dragMinHeight);
			iH > maxH && (iH = maxH);
			lockY || (oParent.style.height = iH + "px");
			
			if((isLeft && iW == dragMinWidth) || (isTop && iH == dragMinHeight)) document.onmousemove = null;
			
			return false;	
		};
		document.onmouseup = function ()
		{
			document.onmousemove = null;
			document.onmouseup = null;
		};
		return false;
	};
};