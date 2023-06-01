package edu.bupt.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 通用返回数据类
 */
@Data
@AllArgsConstructor
@Schema(description = "通用返回结果")
public class Result {
    /**
     * 自定义响应代码
     */
    @Schema(description = "自定义响应代码")
    Integer code;
    /**
     * 数据
     */
    @Schema(example = "二进制数据", description = "传入前端的数据")
    Object data;
    /**
     * 信息
     */
    @Schema(description = "传入前端的信息")
    String msg;

    /**
     * Instantiates a new Result.
     *
     * @param code the code
     * @param msg  the msg
     */
    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
