package edu.bupt.core;

import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * 实现了核心业务有关方法的类
 *
 * @author saniter
 * @date 2023/05/24
 */
public class Utils {
    static Random random = new Random();

    /**
     * 从File对象获取pcm音频流
     *
     * @param file 文件对象
     * @return {@link UniversalAudioInputStream}
     */
    public static UniversalAudioInputStream getPcmUniversalAudioStream(File file) {
        AudioInputStream stream = getPcmAudioStreamFromFile(file);
        AudioFormat format = stream.getFormat();
        TarsosDSPAudioFormat tarsosFormat = new TarsosDSPAudioFormat(
                format.getSampleRate(),
                format.getSampleSizeInBits(),
                1,
                true,
                false
        );
        return new UniversalAudioInputStream(stream, tarsosFormat);
    }

    /**
     * 从byte数组获取pcm音频流
     *
     * @param data 数据
     * @return {@link UniversalAudioInputStream}
     */
    public static UniversalAudioInputStream getPcmUniversalAudioStream(byte[] data) {
        AudioInputStream stream = getPcmAudioStreamFromBytes(data);
        AudioFormat format = stream.getFormat();
        TarsosDSPAudioFormat tarsosFormat = new TarsosDSPAudioFormat(
                format.getSampleRate(),
                format.getSampleSizeInBits(),
                1,
                true,
                false
        );
//        try {
//            byte[] bytes = new byte[10];
//            System.out.println(stream.read(bytes));
//            System.out.println(Arrays.toString(bytes));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        return new UniversalAudioInputStream(stream, tarsosFormat);
    }

    /**
     * 从文件对象得到音频输出流
     *
     * @param file 文件对象
     * @return {@link AudioInputStream}
     */
    public static AudioInputStream getPcmAudioStreamFromFile(File file) {
        try {
            return getPcmAudioStreamFromBytes(getInputStream(file).readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 合并音频双通道为单通道，返回音频输出流
     *
     * @param data 二进制数据
     * @return {@link AudioInputStream}
     */
    public static AudioInputStream mergeAudioChannels(byte[] data) {
        File tmpFile = null;
        File outFile = null;
        try {
            tmpFile = File.createTempFile("merge_channels_", ".ffmpeg");
            String tmpPath = tmpFile.getCanonicalPath();
            String outputPath = tmpFile.getParentFile().getCanonicalPath() + "\\merged_" + generateRandom() + ".ffmpeg";
            tmpFile.deleteOnExit();
            FileUtils.writeByteArrayToFile(tmpFile, data);

            String command = ("ffmpeg -i \"%s\" -ac 1 -hide_banner -loglevel error -y -f mp3 \"%s\"")
                    .formatted(tmpPath, outputPath);
            System.out.println("ffmpeg-merge channels:" + command);
            Process process = new ProcessBuilder("cmd.exe", "/c", command).start();
            process.waitFor(10, TimeUnit.SECONDS);

            outFile = new File(outputPath);
            InputStream tmpStream = getInputStream(outFile);
            InputStream inputStream = new ByteArrayInputStream(tmpStream.readAllBytes());
            tmpStream.close();

            return AudioSystem.getAudioInputStream(inputStream);
        } catch (IOException | InterruptedException | UnsupportedAudioFileException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (tmpFile != null) tmpFile.delete();
            if (outFile != null) outFile.delete();
        }
    }

    /**
     * 从byte数组获得音频输出流
     *
     * @param data 数据
     * @return {@link AudioInputStream}
     */
    public static AudioInputStream getPcmAudioStreamFromBytes(byte[] data) {
        AudioInputStream audioInputStream = null;
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            audioInputStream = AudioSystem.getAudioInputStream(byteArrayInputStream);
            AudioFormat baseFormat = audioInputStream.getFormat();
            if (baseFormat.getChannels() > 1) {
                audioInputStream = mergeAudioChannels(data);
            }

            // 设定输出格式为pcm格式的音频文件,必须使用单声道,否则后面的检测会有问题
            AudioFormat targetFormat = new AudioFormat(
                    audioInputStream.getFormat().getSampleRate(),
                    16, 1, true, false
            );
            // 输出到音频
            audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return audioInputStream;
    }

    /**
     * 赫兹转换为音高
     *
     * @param hz 赫兹
     * @return 音高
     */
    public static int convertHzToNum(double hz) {
        double midi = 12 * (Math.log(hz / 440) / Math.log(2)) + 69;
        int note_num = (int) Math.round(midi);
        return note_num % 12;
    }

    /**
     * 组合音两段音频，返回是否成功
     * 按权值混合两段音频,长度以第一段为准,自动重复第二段音频.
     *
     * @param A      音频A
     * @param B      音频B
     * @param w1     音频A的音量权值
     * @param w2     音频B的音量权值
     * @param output 输出流
     * @return 操作是否成功
     */
    public static boolean composeAudio(InputStream A, InputStream B, float w1, float w2, OutputStream output) {
        File fileA = null;
        File fileB = null;
        File outFile = null;
        try {
            fileA = File.createTempFile("A_music", ".ffmpeg");
            fileB = File.createTempFile("B_music", ".ffmpeg");
            String APath = fileA.getCanonicalPath();
            String BPath = fileB.getCanonicalPath();
            String outputPath = fileA.getParentFile().getCanonicalPath() + "\\" + generateRandom() + ".ffmpeg";
            fileA.deleteOnExit();
            fileB.deleteOnExit();

            if (!(writeStreamToFile(fileA, A) && writeStreamToFile(fileB, B))) {
                return false;
            }

            String command = ("ffmpeg -i \"%s\" -i \"%s\" -filter_complex " +
                    "\"[0:a]volume=%.1f[a9];" +
                    "[1:a]volume=%.1f[a1];" +
                    "[a9]areverse,silenceremove=1:0:-50dB,areverse[a0];" +
                    "[a1]aloop=loop=-1:size=2e+07[a2];" +
                    "[a0][a2]amix=inputs=2:duration=first:dropout_transition=2\"" +
                    " -hide_banner -loglevel error -y -f mp3 \"%s\"")
                    .formatted(APath, BPath, w1, w2, outputPath);
            System.out.println("ffmpeg:" + command);
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
            process.waitFor(10, TimeUnit.SECONDS);
            outFile = new File(outputPath);
            InputStream resStream = getInputStream(outFile);
            IOUtils.copy(resStream, output);
            output.close();
            resStream.close();
            outFile.deleteOnExit();
            return process.exitValue() == 0;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fileA != null) fileA.delete();
            if (fileB != null) fileB.delete();
            if (outFile != null) outFile.delete();
        }
    }


    public static void main(String[] args) throws IOException {
        File file1 = new File("C:\\Users\\nuclear_08\\IdeaProjects\\AiModule\\PythonCore\\src\\main\\python" +
                "\\createMidi\\resource\\test.mp3");
        File file2 = new File("C:\\Users\\nuclear_08\\IdeaProjects\\AiModule\\PythonCore\\src\\main\\python" +
                "\\createMidi\\resource\\see you monther.mp3");

        File out = new File("C:\\Users\\nuclear_08\\Desktop\\tmp.mp3");
//        AudioInputStream stream = getPcmAudioStreamFromFile(file);
//
//        File output = new File("./music.pcm");
//        FileOutputStream outputStream = new FileOutputStream(output);
//        IOUtils.copy(stream, outputStream);

//        composeAudio(getAudioInputStream(file1), getAudioInputStream(file2), getOutputStream(out), 1, 1, false);
        boolean res = composeAudio(getInputStream(file1), getInputStream(file2), 0.5F, 2F, getOutputStream(out));
        System.out.println(res);
    }

    /**
     * 从File对象开启输入流
     *
     * @param file 文件
     * @return {@link InputStream}
     */
    public static InputStream getInputStream(File file) {
        try {
            return FileUtils.openInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从File对象开启输出流
     *
     * @param file 文件
     * @return {@link OutputStream}
     */
    public static OutputStream getOutputStream(File file) {
        try {
            return FileUtils.openOutputStream(file);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 将流写入文件，流会自动关闭
     *
     * @param file   文件
     * @param stream 流
     * @return boolean
     */
    public static boolean writeStreamToFile(File file, InputStream stream) {
        try {
            OutputStream outputStream = getOutputStream(file);
            if (outputStream == null) return false;
            IOUtils.copy(stream, outputStream);
            stream.close();
            outputStream.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成随机值(100,000~999,999)
     *
     * @return 随机值
     */
    public static String generateRandom() {
        return String.valueOf(random.nextInt(100000, 999999));
    }


}
