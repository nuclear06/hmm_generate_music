package edu.bupt.core;

import edu.bupt.domain.ReqData;
import edu.bupt.ui.PitchDetectOrChart;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 实现向业务服务器发送请求的类
 *
 * @author saniter
 * @date 2023/05/24
 */
@Slf4j
public class SendReqToFlask {
    private static String SERVER = "http://localhost:5000/main";

    /**
     * 向flask服务器发起请求,并返回输出流，
     * 如果响应的Content-Length为0返回null
     *
     * @param data 数据
     * @return {@link InputStream}二进制输出流
     */
    public static InputStream sendReqToFlask(ReqData data) {
        String param_url = getReqUri(data);
        try {
            URL url = new URL(SERVER + param_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15 * 1000);
            conn.setReadTimeout(10 * 1000);
            log.info(SERVER + param_url);

            conn.connect();
            if (conn.getContentLength() == 0) {
                return null;
            }
            return conn.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 向flask服务器发起请求,数据写入File对象，返回是否成功
     *
     * @param data   数据
     * @param output 输出的File对象
     * @return boolean
     */
    public static boolean sendReqToFlask(ReqData data, File output) {
        InputStream stream = sendReqToFlask(data);
        if (stream == null) return false;
        FileOutputStream fStream = null;
        try {
            fStream = FileUtils.openOutputStream(output);
            IOUtils.copy(stream, fStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * 根据{@link ReqData}类构造请求uri
     *
     * @param data 请求数据
     * @return uri字符串
     */
    public static String getReqUri(ReqData data) {
        String pitchSequence = data.getPitchSequence();
        String chord = data.getChord();
        Integer speed = data.getSpeed();
        String instrument = data.getInstrument();
        Float emo = data.getEmo();
        String reqUrl = "?str=%s&a=%s&speed=%d&c=%s&happy=%.1f"
                .formatted(pitchSequence, chord, speed, instrument, emo);
        return reqUrl;
    }


    public static void main(String[] args) throws IOException {
        String pitch = PitchDetectOrChart.pitchDetectFormMicrophone(true, 4000L);
        ReqData data = new ReqData(pitch, "C", 5000, "110000", 0.9F);
        sendReqToFlask(data, new File("resp.mp3"));
    }
}
