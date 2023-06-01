$("#form-box").attr("target", "refearchForm");
$("#captcha-img").click(() => {
    let date = new Date().getTime();
    $("#captcha-img").attr("src", "/captcha?" + date);
})
$("#captcha").keypress(function (event) {
    if (event.which === 13) {
        $(".layui-btn").trigger("click");
    }
})
layui.use(['form', 'layer'], function () {
    var form = layui.form;
    form.on('submit(form_submit)', (formData) => {
        $.ajax({
            type: "GET",
            url: "/publickey",
            async: true,
            success: (data) => {
                let post_data = formData.field
                if (data.code === 200) {
                    let pub = data.data;
                    let encryptor = new JSEncrypt()
                    encryptor.setPublicKey(pub)
                    post_data.password = encryptor.encrypt(post_data.password);
                    $.ajax({
                        type: "POST",
                        url: "/user",
                        async: true,
                        data: JSON.stringify(post_data),
                        contentType: "application/json; charset=utf-8",
                        success: (data) => {
                            let code = data.code;
                            if (code === 6000) {
                                let s = window.location.href;
                                let reg = /https?:\/\/.*?\//;
                                let url = s.match(reg);
                                if (url == null) {
                                    return "网络错误,请稍后再试!";
                                } else {
                                    let target = url[0] + 'login';
                                    layer.msg("注册成功,即将跳转到登陆界面")
                                    setTimeout(() => {
                                        window.location.href = target
                                    }, 3000);
                                }
                            } else if (code === 1001) {
                                layer.msg(data.msg, {icon: 2});
                                $("#captcha-img").trigger('click')
                            } else if (code === 6001) {
                                layer.msg(data.msg, {icon: 2});
                            } else {
                                layer.msg(data.msg, {icon: 2});
                            }
                        }
                    });
                }else {
                    layer.msg("网络不稳定,请稍后再试!", {icon: 2});
                }
            }
        })
    })
    form.verify(
        {
            userName: function (value) {
                if (!new RegExp("^[a-zA-Z0-9_\u4e00-\u9fa5]+$").test(value)) {
                    return '用户名不能有特殊字符';
                }
                if (value.length > 10) {
                    return '用户名过长'
                }
            },
            password: function (value) {
                if (!new RegExp("^\\S{5,20}$").test(value)) {
                    return '密码必须由5-20位的非空格组成'
                }
            },
            password_check: function (value, dom) {
                let $ = layui.$;
                let pwd = $('#password').val();
                if (pwd !== value) {
                    return "密码不一致!"
                }
            },
            userExist: function (value) {
                let $ = layui.$;
                let _data = null;
                $.ajax({
                    type: "GET",
                    url: "/user?username=" + value,
                    async: false,
                    success: function (data) {
                        _data = data;
                    }
                });

                if (_data == null) {
                    return "网络错误,请稍后再试!";
                } else if (_data.code === 4000) {
                    //     用户名可用
                } else if (_data.code === 4001) {
                    return _data.msg;
                } else {
                    return _data.msg;
                }
            },
        }
    )
});