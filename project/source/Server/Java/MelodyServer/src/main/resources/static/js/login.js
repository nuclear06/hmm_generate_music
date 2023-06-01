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
layui.use(['form', "layer"], function () {
    var form = layui.form;
    form.on("submit(login)", (formData) => {
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
                    // console.log(post_data)
                    $.ajax({
                        type: "POST",
                        url: "/user/login",
                        async: true,
                        contentType: "application/json; charset=utf-8",
                        data: JSON.stringify(post_data),
                        success: (data) => {
                            let code = data.code;
                            if (code === 5000) {
                                let s = window.location.href;
                                let reg = /https?:\/\/.*?\//;
                                let url = s.match(reg);
                                if (url == null) {
                                    return "网络错误,请稍后再试!";
                                } else {
                                    let target = url[0] + 'server';//不要加/
                                    layer.msg(data.msg+" 即将自动跳转.")
                                    setTimeout(() => {
                                        window.location.href = target
                                    }, 2000);
                                }
                            } else if (code === 1001) {
                                layer.msg(data.msg, {icon: 2});
                                $("#captcha-img").trigger('click')
                            } else if (code === 5001) {
                                layer.msg(data.msg, {icon: 2});
                                $("#captcha-img").trigger('click')
                            }else {
                                layer.msg(data.msg, {icon: 2});
                                $("#captcha-img").trigger('click')
                            }
                        }
                    })
                } else {
                    layer.msg("网络不稳定,请稍后再试!", {icon: 2});
                }
            }
        })
    })
    form.verify(
        {
            userName: function (value, dom) {
                if (!new RegExp("^[a-zA-Z0-9_\u4e00-\u9fa5]+$").test(value)) {
                    return '用户名不能有特殊字符';
                }
                if (value.length > 10) {
                    return '用户名过长'
                }
            },
            password: function (value, dom) {
                if (!new RegExp("^\\S{5,20}$").test(value)) {
                    return '密码必须由5-20位的非空格组成'
                }
            },
        }
    )
});