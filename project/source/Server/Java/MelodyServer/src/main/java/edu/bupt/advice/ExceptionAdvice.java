package edu.bupt.advice;

import edu.bupt.controller.Code;
import edu.bupt.controller.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;


/**
 * 全局异常处理器,负责处理服务器抛出的所有异常.
 *
 * @author saniter
 * @date 2023/05/23
 */
@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {
    /**
     * 对{@link MethodArgumentNotValidException}类的异常处理,该异常在后端校验错误时被抛出.
     *
     * @param e 捕捉到的异常
     * @return {@link Result} 通用返回类,包含状态码和信息
     * {@code Code.VERIFY_FIL=2001} 后端检验错误
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result verifyFailedAdvice(MethodArgumentNotValidException e) {
        StringBuilder sb = new StringBuilder();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            sb.append(error.getDefaultMessage()).append("/");
        }
        sb.deleteCharAt(sb.length() - 1);
        return new Result(Code.VERIFY_FIL, sb.toString());
    }

    /**
     * 对{@link MultipartException}类的异常处理,该异常在单词请求大小超过服务器限制时抛出.
     *
     * @param ex 捕捉到的异常
     * @return {@link Result}通用返回类,包含状态码和信息
     * {@code Code.VERIFY_FIL=2001} 上传数据大小超过限制
     */
    @ExceptionHandler(MultipartException.class)
    public Result RequestServerFileError(MultipartException ex) {
        ex.printStackTrace();
        return new Result(Code.VERIFY_FIL, "请求数据错误");
    }

    /**
     * 对其余所有异常的通用处理
     *
     * @param exception 捕捉到的异常
     * @return {@link Result}通用返回类,包含状态码和信息
     * {@code Code.SYSTEM_ERR=9003} 其他没有专门处理方式的异常
     */
    @ExceptionHandler(Exception.class)
    public Result handlerAllException(Exception exception) {
        exception.printStackTrace();
        return new Result(Code.SYSTEM_ERR, "网络错误!");
    }
}
