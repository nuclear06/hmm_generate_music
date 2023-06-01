package edu.bupt.controller;

import edu.bupt.service.CoreService;
import edu.bupt.utils.PwdSaltUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import static edu.bupt.service.CoreService.keys;

/**
 * 提供核心业务的url，也包含有关业务所需的方法.
 *
 * @author saniter
 * @date 2023/05/23
 */
@Slf4j
@Controller
@Tag(name = "ServerController", description = "业务核心接口，用于业务的请求")
public class ServerController {

    @Autowired
    private CoreService coreService;

    /**
     * 提供访问服务页面的url.
     *
     * @return server.html
     */
    @GetMapping("/server")
    public String getServer() {
        return "html/server";
    }

    /**
     * 业务数据提交接口,接收POST请求.
     *
     * <p>map中包含以下键值:
     * <ul>
     *     <li>rhythm
     *     <li>speed
     *     <li>key
     *     <li>mention
     *     <li>checkList
     *     <li>username
     * </ul>
     * 要求以上每个键都有对应的值,且要求符合相应的格式,具体格式要求参看api文档.
     * <p>file为上传的音频文件,要求不能为空且大小不超过20mb.
     *
     * @param req  {@link HttpServletRequest}http请求相关信息
     * @param map  储存提交键值对的数据结构
     * @param file 储存提交文件的数据结构
     * @return {@link Result}标准返回接口,包含状态码和信息
     * <ul>
     * <li> {@code Code.SERVICE_ERR=400} 服务请求出现异常
     * <li> {@code Code.VERIFY_FIL=2001} 未在音频文件检测到音高/数据未通过校验
     * <li> {@code Code.SUCCESS=200} 请求成功
     * </ul>
     */
    @PostMapping("/server")
    @ResponseBody
    @Operation(summary = "主业务接口，返回配乐文件的哈希值")
    @ApiResponse(description = "标准返回格式，data为生成文件的md5值")
    public Result mainServer(HttpServletRequest req, @RequestParam Map<String, Object> map,
                             @RequestParam("file") MultipartFile file) {

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.matches("(\\.wav)|(\\.mp3)$")) {
            return new Result(Code.VERIFY_FIL, "文件格式错误!");
        }
        if (file.getSize() / 1024 / 1024 > 20 || file.getSize() == 0) {//大小大于20mb
            return new Result(Code.VERIFY_FIL, "文件过大!");
        }
        try {
            int rhythm = Integer.parseInt((String) map.get("rhythm"));
            //0为3/4拍 1为4/4拍
            int speed = Integer.parseInt((String) map.get("speed"));//speed为BPM
            float emo = Float.parseFloat((String) map.get("mention")) / 100;
            String chord = keys.get((String) map.get("key"));
            String instrument = parseInstrument((String) map.get("checkList"));
            String username = (String) req.getSession().getAttribute("username");
            if (chord == null || instrument == null || username == null) {
                throw new NullPointerException();
            }

            String newFileName = coreService.generateMusic(rhythm, speed, chord, emo, instrument, username, file);
            if (newFileName == null) {
                return new Result(Code.SERVICE_ERR, "服务异常,请稍后再试");
            } else if ("-1".equals(newFileName)) {
                return new Result(Code.VERIFY_FIL, "未检测音高!");
            }


            return new Result(Code.SUCCESS,
                    PwdSaltUtils.md5Encrypt(newFileName, null, null)
                    , "服务请求成功!");
        } catch (NullPointerException | NumberFormatException e) {
            return new Result(Code.VERIFY_FIL, "数据不符合规定");
        }
    }


    /**
     * 分析乐器数据，转换为容易处理的格式.
     *
     * <p>输入格式请参看api文档.
     * <p>转换目标格式为一个6位长的字符串,每个位置可能是0和1,位数上为0则意味着没有选取该位数对应的乐器,反之选取了.
     * <p>各个位数上对应的乐器如下(从低位到高位)
     * <ul>
     *     <li>贝斯
     *     <li>鼓
     *     <li>吉他柱式
     *     <li>吉他分解
     *     <li>钢琴柱式
     *     <li>钢琴分解
     * </ul>
     *
     * @param instrument 提交的乐器信息
     * @return {@link String}转换结果
     */
    public static String parseInstrument(String instrument) {
        int res = 0;

        if (instrument.startsWith("[")) instrument = instrument.substring(1);
        if (instrument.endsWith("]")) instrument = instrument.substring(0, instrument.length() - 1);
        HashSet<String> instru = new HashSet<>(Arrays.asList(instrument.split(",")));
        for (String s : instru) {
            switch (s) {
                case "钢琴分解" -> res += 1E6;
                case "钢琴柱式" -> res += 1E5;
                case "吉他分解" -> res += 1E4;
                case "吉他柱式" -> res += 1E3;
                case "鼓" -> res += 1E2;
                case "贝斯" -> res += 1;
                default -> {
                    log.warn("不是预料的乐器类型:" + s);
                    return null;
                }
            }
        }
        return String.format("%06d", res);
    }
}

