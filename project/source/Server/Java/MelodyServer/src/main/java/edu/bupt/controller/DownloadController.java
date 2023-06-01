package edu.bupt.controller;

import edu.bupt.dao.HistoryDao;
import edu.bupt.service.IHistoryService;
import edu.bupt.utils.RSAUtils;
import edu.bupt.utils.ServerBaseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * 提供下载服务的url和相关服务所需的方法
 */
@RestController
@Tag(name = "DownloadController", description = "用户下载数据的接口")
@RequestMapping(value = "/download", method = RequestMethod.GET)
public class DownloadController {
    @Autowired
    private RSAUtils rsaUtils;
    @Autowired
    private ServerBaseUtils serverBaseUtils;

    @Autowired
    private HistoryDao historyDao;
    @Autowired
    private IHistoryService historyService;

    /**
     * 提供下载用户历史数据的url,获取到的二进制流通过{@link HttpServletResponse}返回
     *
     * @param req    {@link HttpServletRequest}http请求相关信息和方法
     * @param resp   {@link HttpServletResponse}http响应相关信息和方法
     * @param origin 是原始文件还是配乐后的文件
     * @param index  历史记录的索引
     * @return {@link Result}通用返回类,包含状态码和信息
     * <ul>
     *     <li> {@code Code.SUCCESS=200} 请求成功
     *     <li> {@code Code.SERVICE_ERR=400} 请求异常
     * </ul>
     */
    @Operation(summary = "下载历史数据文件", description = "根据索引下载用户自身的历史数据")
    @ApiResponse(description = "返回文件的二进制数据")
    @GetMapping("/person/{index}")
    public Result downPersonalFile(HttpServletRequest req,
                                   HttpServletResponse resp,
                                   @Parameter(description = "若为1则返回原始文件，为0返回配乐后文件") Integer origin,
                                   @Parameter(description = "下载的数据的索引") @PathVariable Integer index) {
        boolean isOrigin = (int) Objects.requireNonNullElse(origin, 0) == 1;//默认返回配乐文件
        if (origin == null || index <= 0) return new Result(Code.VERIFY_FIL, "文件不存在!");

        String username = (String) req.getSession().getAttribute("username");

        System.out.println(index);
        String fileName = historyService.getHistoryAtIndex(username, index).getFileName();
        return download(isOrigin, resp, fileName);
    }

    /**
     * 提供根据文件md5下载文件的url,获取到的二进制数据通过{@link HttpServletResponse}返回
     *
     * @param resp {@link HttpServletResponse}http响应相关信息和方法
     * @param md5  文件md5
     * @return {@link Result}通用返回类,包含状态码和信息
     * <ul>
     *     <li> {@code Code.SUCCESS=200} 请求成功
     *     <li> {@code Code.SERVICE_ERR=400} 请求异常
     * </ul>
     */
    @Operation(summary = "根据md5下载对应文件", parameters = @Parameter(name = "md5", description = "想下载文件的哈希值"))
    @ApiResponse(description = "返回文件的二进制数据")
    @GetMapping("/{md5}")
    public Result downFile(HttpServletResponse resp, @PathVariable String md5) {
        String fileName = historyDao.getFileNameByMd5(md5);//根据md5获取原文件文件名
        if (fileName == null) return new Result(Code.SERVICE_ERR, "未找到资源!");
        resp.setHeader("file-name", ServerBaseUtils.processRespFileName(fileName));
        return download(false, resp, fileName);
    }


    /**
     * 下载指定文件名的文件,二进制流通过{@link HttpServletResponse}返回
     *
     * @param isOrigin 是否为源文件
     * @param response {@link HttpServletResponse}http响应相关信息和方法
     * @param fileName 文件名称
     * @return {@link Result}通用返回类,包含状态码和信息
     * <li> {@code Code.SUCCESS=200} 请求成功
     * <li> {@code Code.SERVICE_ERR=400} 请求异常
     */
    private Result download(boolean isOrigin, HttpServletResponse response, String fileName) {
        String inputCachePath = serverBaseUtils.getInputCachePath();
        String outputCachePath = serverBaseUtils.getOutputCachePath();
        try {
            File f;
            if (isOrigin) {
                f = new File(inputCachePath + "\\" + fileName);
            } else {
                f = new File(outputCachePath + "\\" + fileName);
            }
            if (!f.exists()) return new Result(Code.SERVICE_ERR, "未找到资源!可能已经过期.");

            FileInputStream stream = FileUtils.openInputStream(f);
            IOUtils.copy(stream, response.getOutputStream());
            stream.close();
            return new Result(Code.SUCCESS, "下载成功");
        } catch (IOException e) {
            e.printStackTrace();
            return new Result(Code.SERVICE_ERR, "服务器繁忙!");
        }
    }
}
