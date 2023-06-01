package edu.bupt.service;

import edu.bupt.core.SendReqToFlask;
import edu.bupt.core.Utils;
import edu.bupt.dao.HistoryDao;
import edu.bupt.dao.UserDao;
import edu.bupt.domain.History;
import edu.bupt.domain.ReqData;
import edu.bupt.utils.ServerBaseUtils;
import edu.bupt.utils.PwdSaltUtils;
import edu.bupt.ui.PitchDetectOrChart;
import jakarta.validation.constraints.NotEmpty;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Random;

/**
 * 实现核心业务的类
 */
@Slf4j
@Service
public class CoreService {
    @Value("${service.debug.save-sql-log:true}")
    private boolean saveLog;
    @Autowired
    private ServerBaseUtils serverBaseUtils;
    @Autowired
    private HistoryDao historyDao;
    @Autowired
    private UserDao userDao;
    /**
     * 记录了字符串格式的int和音调对应关系的Map
     */
    public static final Map<String, String> keys = Map.ofEntries(
            new AbstractMap.SimpleEntry<>("0", "C"),
            new AbstractMap.SimpleEntry<>("1", "bD"),
            new AbstractMap.SimpleEntry<>("2", "D"),
            new AbstractMap.SimpleEntry<>("3", "bE"),
            new AbstractMap.SimpleEntry<>("4", "E"),
            new AbstractMap.SimpleEntry<>("5", "F"),
            new AbstractMap.SimpleEntry<>("6", "bG"),
            new AbstractMap.SimpleEntry<>("7", "G"),
            new AbstractMap.SimpleEntry<>("8", "bA"),
            new AbstractMap.SimpleEntry<>("9", "A"),
            new AbstractMap.SimpleEntry<>("10", "bB"),
            new AbstractMap.SimpleEntry<>("11", "B")
    );
    private static final Random random = new Random();

    /**
     * 生成音乐返回音乐文件的名字
     * 注意:文件名不包括路径,路径为getOutputCachePath()
     * 出现异常返回null
     *
     * @param rhythm     节拍(0->3/4 1->4/4)
     * @param speed      传入为每拍的时间(ms)
     * @param chord      the chord
     * @param emo        emo被归一化到0~1
     * @param instrument 乐器
     * @param username   用户名
     * @param music      音频文件
     * @return 生成的文件路径
     */
    public String generateMusic(int rhythm, int speed, String chord, float emo, String instrument,
                                String username, MultipartFile music) {
        final float tempo = (60 / (float) speed) * 1000;//将BPM转换为每拍时长(ms)
        final float secondsPerSection = rhythm == 0 ? tempo * 3 : tempo * 4;//计算section的时长
        log.info("每小节的时长为:" + secondsPerSection + "ms");
        try {
            String fileName = music.getOriginalFilename();

            String newFileName = generateFileName(fileName, username);//输出一个有效可用的文件名
            String inputFilePath = serverBaseUtils.getInputCachePath() + "\\" + newFileName;
            String outputFilePath = serverBaseUtils.getOutputCachePath() + "\\" + newFileName;

            @NotEmpty byte[] bytes = music.getBytes();
            music.transferTo(new File(inputFilePath));//保存输入文件
            log.info("一个高音检测任务已启动");
            String pitch = PitchDetectOrChart.pitchDetect(bytes, secondsPerSection);//通过文件获取音高序列
            log.info("一个高音检测任务已结束:" + pitch.length());

            if ("".equals(pitch)) return "-1";

            ReqData reqData = new ReqData(pitch, chord, speed, instrument, emo);
            InputStream stream = SendReqToFlask.sendReqToFlask(reqData);//发出请求,获取旋律数据
            if (stream == null) {
                log.warn("Flask服务器返回空流");
                return null;
            }

            Utils.composeAudio(new ByteArrayInputStream(bytes), stream, 1F, 0.8F,
                    Utils.getOutputStream(new File(outputFilePath)));

            boolean piano = instrument.charAt(0) == '1';
            boolean pianoChord = instrument.charAt(1) == '1';
            boolean guitar = instrument.charAt(2) == '1';
            boolean guitarChord = instrument.charAt(3) == '1';
            boolean drum = instrument.charAt(4) == '1';
            boolean bass = instrument.charAt(5) == '1';


            if (saveLog) {
                @NonNull int id = userDao.getIdByUsername(username);
                historyDao.insert(new History(
                        id, new Timestamp(System.currentTimeMillis()), rhythm, chord, speed, emo,
                        piano, pianoChord, guitar, guitarChord, drum, bass,
                        newFileName, PwdSaltUtils.md5Encrypt(newFileName, null, null)));  //保存记录到数据库
            }
            return newFileName;

        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据已有文件名，返回一个可用文件名,该文件此前不存在.
     * 格式 username-fileName-(random int)
     *
     * @param oldFileName 已有文件名
     * @param username    用户名
     * @return 文件名
     */
    public String generateFileName(String oldFileName, String username) {
        String randFileName;
        String inputFileName;
        String outputFileName;
        if (oldFileName == null) oldFileName = "default.wav";
        if (!oldFileName.matches(".*?\\.\\w+$")) oldFileName += ".wav";//默认设置为wav文件后缀
        if (oldFileName.length() > 30)
            oldFileName = oldFileName.substring(oldFileName.length() - 31, oldFileName.length() - 1);//取后面倒数30个字符

        do {
            randFileName = "[" + username + "]-[" + random.nextInt(10000, 100000) + "]-" + oldFileName;
            inputFileName = serverBaseUtils.getInputCachePath() + "\\" + randFileName;
            outputFileName = serverBaseUtils.getOutputCachePath() + "\\" + randFileName;
        } while (Files.exists(Path.of(inputFileName)) || Files.exists(Path.of(outputFileName)));
        return randFileName;
    }

}
