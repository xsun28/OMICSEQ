function showLoading() {
	//TODO
}
function hideLoading() {
	//TODO
}
function sendRequest(url, params, dataType, callBack, failerTip) {
	$.ajax({
		type: "POST",
		url: url,
		data: params,
		async : false,
		timeout: 240000,
		dataType: dataType,
		beforeSend: function(){
			
		},
		success: function(data){
			if(callBack) {
				callBack(data);
			}
		},
		error: function(request, textStatus, errorThrown){
			hideLoading();
			alert(failerTip);
		}
    });
}

//关闭对话框
$(document).on('click', '#backgroundDiv', function(){
	$('.dialog').hide();
	if($('#errorEl').length > 0) {
		$('#errorEl').hide();
	}
	$('#backgroundDiv').hide();
});
//显示登录框
function showLoginDialog(){
	$('.loginDialog').show();
	$('#backgroundDiv').show();
}
//点击remember me
$(document).on('click', '.rememberSpan', function(){
	if($(this).hasClass('deselectedSpan')) {
		$(this).removeClass('deselectedSpan');
		$(this).addClass('selectedSpan');
	}else{
		$(this).addClass('deselectedSpan');
		$(this).removeClass('selectedSpan');
	}
});
//d登录
$(document).on('click', '#login', function(){
	var box = $('#loginForm');
	var userName = $('#userName', box).val();
	var password = $('#password', box).val();
	var errorEl = $('#errorEl');
	if (validateRequired(userName, $('#userName', box).attr('errorMessage'), errorEl) == false) {
		$('#userName').focus();
		return;
	}
	if (validateRequired(password, $('#password', box).attr('errorMessage'), errorEl) == false) {
		$('#password').focus();
		return;
	}
	var isRemember = $('.rememberSpan').hasClass('selectedSpan') ? 'true' : 'false';
	var url = rootPath + '/user/login.json';
	var params = {"userName":userName, "password":password, "isRemember":isRemember};
	var callBack = function(result){
		if(result.result) {
			var href = window.location.href;
			if(href.indexOf('toRegister.htm') != -1) {
				window.location.href = rootPath + "/index.htm";
			}else{
				window.location.href = window.location.href;
			}
		}else{
			errorEl.html(result.message);
			errorEl.show();
		}
	};
	sendRequest(url, params, "json", callBack, "");
});
//进入注册页面
function showRegister() {
	window.location.href = rootPath + "/user/toRegister.htm";
}

//注销
$(document).on('click', '#logoutBtn', function(){
	var url = rootPath + '/user/logout.json';
	var callBack = function(result){
		if(result.result) {
			window.location.href = rootPath + "/index.htm";
		}
	};
	sendRequest(url, {}, "json", callBack, "");
});
//显示语言选择框 
function showLanguageDialog() {
	$('.languageDialog').show();
	$('#backgroundDiv').show();
}
//切换语言
function changeLanguage(isChiness) {
	$('.languageDialog').hide();
	$('#backgroundDiv').hide();
	var language = isChiness ? "locale=zh_CN" : "locale=en";
	var url = window.location.href;
	if(window.location.href.indexOf("?") == -1) {
		url = window.location.href + "?" + language;
	}else{
		if(window.location.href.indexOf("locale") == -1) {
			url = window.location.href + "&" + language;
		}else{
			if(isChiness) {
				url = window.location.href.replace("locale=en", "locale=zh_CN");
			}else{
				url = window.location.href.replace("locale=zh_CN", "locale=en");
			}
		}
	}
	window.location.href = url;
}

function getCookie(name) 
{ 
    var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");
 
    if(arr=document.cookie.match(reg))
 
        return unescape(arr[2]); 
    else 
        return null; 
}

function setCookie(name,value) 
{ 
    var Days = 3; 
    var exp = new Date(); 
    exp.setTime(exp.getTime() + Days*24*60*60*1000); 
    document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString(); 
} 

//删除cookies 
function delCookie(name) 
{ 
    var exp = new Date(); 
    exp.setTime(exp.getTime() - 1); 
    var cval=getCookie(name); 
    if(cval!=null) 
        document.cookie= name + "="+cval+";expires="+exp.toGMTString(); 
} 

