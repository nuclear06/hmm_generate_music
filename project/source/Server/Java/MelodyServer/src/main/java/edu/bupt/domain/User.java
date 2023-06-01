package edu.bupt.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * 记录用户信息的类
 *
 * @author saniter
 * @date 2023/05/24
 */
@Data
@RequiredArgsConstructor
@Schema(description = "记录一个用户的信息")
public class User {
    @Schema(description = "id值")
    Integer id;
    @Schema(description = "用户名")
    @NotBlank(message = "名字为必填项")
    @Length(max = 10, message = "用户名长度必须小于10")
    @Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", message = "用户名格式错误")
    String username;
    @Schema(description = "密码")
    @TableField(select = false)
    @NotBlank(message = "密码为必填项")
    String password;

    @Schema(description = "验证码，用于发送请求时验证")
    @TableField(exist = false)
    @NotBlank(message = "验证码不能为空")
    private String captcha;


    /**
     * 实例化用户类
     *
     * @param username 用户名
     * @param password 密码
     */
    public User(@NonNull String username, String password) {
        this.username = username;
        this.password = password;
    }
}
