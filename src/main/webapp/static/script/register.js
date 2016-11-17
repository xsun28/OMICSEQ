//显示密码格式提示
$(document).on('focus', '#registerForm #register_password', function(){
	validPasswordFormat();
	$('#pwdTip').show();
});
//隐藏密码格式提示
$(document).on('blur', '#registerForm #register_password', function(){
	validPasswordFormat();
	var el = $('#pwdTip').find('.wrongMessage');
	if(el.length == 0) {
		$('#pwdTip').hide();
	}
});
//显示用户名格式提示
$(document).on('focus', '#registerForm #register_userName', function(){
	validUserNameFormat();
	$('#usernameTip').show();
});
//隐藏用户名格式提示
$(document).on('blur', '#registerForm #register_userName', function(){
	validUserNameFormat();
	var el = $('#usernameTip').find('.wrongMessage');
	if(el.length == 0) {
		$('#usernameTip').hide();
	}
});
$(document).on('keyup', '#registerForm #register_userName', function(){
	validUserNameFormat();
});
$(document).on('keyup', '#registerForm #register_password', function(){
	validPasswordFormat();
});
//注册
$(document).on('click', '#regiterBtn', function(){
	var el = $('#registerForm').find('.wrongMessage').length;
	if(el > 0) {
		return;
	}
	var box = $('#registerForm');
	var userName = $('#register_userName', box).val();
	var password = $('#register_password', box).val();
	var confirmPassword = $('#confirmPassword', box).val();
	var email = $('#email', box).val();
	var company = $('#company', box).val();
	var lab = $('#lab', box).val();
	var department = $('#department', box).val();
	var companyType = $('#companyType', box).attr('typeId');
	var errorEl = $('#registerError');
	if (validateRequired(userName, $('#register_userName', box).attr('errorMessage'), errorEl) == false) {
		$('#register_userName').focus();
		return;
	}
	if (validateRequired(password, $('#register_password', box).attr('errorMessage'), errorEl) == false) {
		$('#register_password').focus();
		return;
	}
	if (validateRequired(confirmPassword, $('#confirmPassword', box).attr('errorMessage'), errorEl) == false) {
		$('#confirmPassword').focus();
		return;
	}
	if (validateMatch(password, confirmPassword, $('#confirmPassword', box).attr('errorMacth'), errorEl) == false) {
		$('#confirmPassword').val('');
		$('#confirmPassword').focus();
		return;
	}
	if (validateRequired(email, $('#email', box).attr('errorMessage'), errorEl) == false || validateEmail(email, $('#email', box).attr('formatError'),errorEl) == false) {
		$('#email').val('');
		$('#email').focus();
		return;
	}
	if (validateRequired(company, $('#company', box).attr('errorMessage'), errorEl) == false) {
		$('#company').val('');
		$('#company').focus();
		return;
	}
	var url = rootPath + '/user/register.json';
	var params = {"name":userName, "password":password, "email":email, "company":company, "lab":lab,
			"department":department, "companyType":companyType};
	var callBack = function(result){
		if(result.result) {
			window.location.href = rootPath + "/index.htm";
		}else{
			errorEl.html(result.message);
		}
	};
	var failed = $(this).attr('faildMessage');
	sendRequest(url, params, "json", callBack, failed);
});

//注册页面公司性质
$(document).on('click', '#companyType', function(){
	$('#backgroundDiv').css("height",screen.availHeight);
	var ul =$(this).next();
	if(ul.is(':hidden')) {
		ul.show();
		$('#backgroundDiv').show();
	}else{
		ul.hide();
		$('#backgroundDiv').hide();
	}
});
//选择公司性质
$(document).on('click', '#companyTypeUl li', function(){
	var id = $(this).attr('id');
	var name = $(this).html();
	$('#companyType').html(name);
	$('#companyType').attr('typeId',id);
	$('#companyType').css("color", "#555555");
	$('#companyType').trigger('click');
});

function validUserNameFormat() {
	//验证输入
	var value = $('#registerForm #register_userName').val();
	var nullRex = new RegExp(" ");
	if(nullRex.test(value)) {
		$('#usernameTip').find('.nullRule').addClass('wrongMessage');
	}else{
		$('#usernameTip').find('.nullRule').removeClass('wrongMessage');
	}
	if(value.length > 15 || value.length < 3) {
		$('#usernameTip').find('.lenRule').addClass('wrongMessage');
	}else{
		$('#usernameTip').find('.lenRule').removeClass('wrongMessage');
	}
	var lenRex = new RegExp("^[A-Za-z0-9_]+$");
	if(!lenRex.test(value)) {
		$('#usernameTip').find('.formatRule').addClass('wrongMessage');
	}else{
		$('#usernameTip').find('.formatRule').removeClass('wrongMessage');
	}
}

function validPasswordFormat() {
	//验证输入
	var value = $('#registerForm #register_password').val();
	var nullRex = new RegExp(" ");
	if(nullRex.test(value)) {
		$('#pwdTip').find('.nullRule').addClass('wrongMessage');
	}else{
		$('#pwdTip').find('.nullRule').removeClass('wrongMessage');
	}
	if(value.length > 14 || value.length < 6) {
		$('#pwdTip').find('.lenRule').addClass('wrongMessage');
	}else{
		$('#pwdTip').find('.lenRule').removeClass('wrongMessage');
	}
	var lenRex = new RegExp("^[A-Za-z0-9]+$");
	if(!lenRex.test(value)) {
		$('#pwdTip').find('.formatRule').addClass('wrongMessage');
	}else{
		$('#pwdTip').find('.formatRule').removeClass('wrongMessage');
	}
}
//回车登陆
$(document).on('keydown', '#password', function(event){
	if(event.keyCode == 13) {
		$('#loginBtn').trigger("click");
	}
});
