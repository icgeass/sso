
jQuery(document).ready(function() {

	/*
	 Fullscreen background
	 */
	$.backstretch([
        "/resources/assets/img/backgrounds/1.jpg",
		"/resources/assets/img/backgrounds/2.jpg",
		"/resources/assets/img/backgrounds/3.jpg"
	], {duration: 3000, fade: 750});
    
    /*
        Form validation
    */
    $('.login-form input[type="text"], .login-form input[type="password"], .login-form textarea').on('focus', function() {
    	$(this).removeClass('input-error');
		$('#message').remove();
    });
    
    $('.login-form').on('submit', function(e) {
    	
    	$(this).find('input[type="text"], input[type="password"], textarea').each(function(){
    		if($.trim($(this).val()) == "" ) {
    			e.preventDefault();
    			$(this).addClass('input-error');
    		}
    		else {
    			$(this).removeClass('input-error');
    		}
    	});
    	
    });
	// enter登录
	$("#password").keydown(function (e) {
		if (e.keyCode == 13) {
			doLogin();
		}
	});

	$('#submit').on('click', function (e) {
		doLogin();
    })

});

function showMsg(msg){
    var html = "<div id='message'>" + msg + "</div>";
    var messageDiv = $('#message');
    if(messageDiv.val() != null){
        messageDiv.remove();
	}
	if($.trim(msg) != ''){
    	$('.login-form').find('input[type="password"]').after(html);
	}

}

function enableButton(button, enable) {
	if(enable === true){
        button.attr('disabled', false);
        button.html('Sign in!');
	}else{
        button.attr('disabled', true);
        button.html('Now login...');
	}
}

function doLogin(){
	var submit = $('#submit');
	var jsencrypt = null;
	var passwordMaxLength = 16;
	var username = $.trim($("#username").val());
	var password = $.trim($("#password").val());
	if(username == "" || password == ""){
		alert("用户名或密码不能为空");
		return;
	}
	if(password.length > passwordMaxLength){
		alert("密码长度不能超过" + passwordMaxLength + "个字符");
		return;
	}
	var now = new Date();
	jsencrypt = new JSEncrypt();
	jsencrypt.setPublicKey($("#pubKey").val());
	password = jsencrypt.encrypt((now.valueOf() + (now.getTimezoneOffset() * 60 * 1000)) +  "," + password);
	if("false" == password){
		alert("加密失败")
		return;
	}
	// ajax 提交方式
    enableButton(submit, false);
    $.post("",
        {
            username: username,
            password: password
        },
        function (data, status) {
            var resp = JSON.parse(data);
            if (resp.success) {
                window.location.href = resp.ReturnUrl;
                return;
            } else {
                showMsg(resp.message);
            }
        }).fail(function () {
        showMsg('系统繁忙, 请稍后再试!')
    }).always(function () {
        enableButton(submit, true);
    });
	// window.btoa(password)
	// 表单提交方式
	//$("#password").val(password);
	//$("#loginForm").submit();
}
