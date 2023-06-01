package edu.bupt.ui;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import edu.bupt.core.Utils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.sound.sampled.LineUnavailableException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 提供音高检测及可视化方法的类
 *
 * @author saniter
 * @date 2023/05/24
 */
public class PitchDetectOrChart {
    /**
     * 创建一个折线图的默认方法,返回画图的数据集
     *
     * @return {@link DefaultCategoryDataset}图的数据集
     */
    public static DefaultCategoryDataset DefaultCreatePlot() {
        return createPlot(null, null);
    }

    /**
     * 创建图
     * 创建一个折线图,返回画图的数据集
     *
     * @param yMin 纵轴下界
     * @param yMax 纵轴上界
     * @return {@link DefaultCategoryDataset}图的数据集
     */
    public static DefaultCategoryDataset createPlot(Integer yMin, Integer yMax) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        StandardChartTheme theme = new StandardChartTheme("CN");
        Font chs = new Font("宋体", Font.PLAIN, 20);
        theme.setRegularFont(chs);
        theme.setLargeFont(chs);
        theme.setSmallFont(chs);
        theme.setExtraLargeFont(chs);
        ChartFactory.setChartTheme(theme);

        JFreeChart chart = ChartFactory.createLineChart(
                "音高检测",
                "时间",
                "音高",
                dataset
        );
        if (yMin != null && yMax != null) {
            chart.getCategoryPlot().getRangeAxis().setRange(yMin, yMax);
        }

        ChartFrame frame = new ChartFrame("First", chart);
        frame.pack();
        frame.setVisible(true);
        return dataset;
    }

    /**
     * 从File文件检测音高序列，并提供可视化图表
     *
     * @param yMin 支持null
     * @param yMax 支持null
     * @param file 文件
     */
    public static void pitchDetectMp3FileShow(File file, Integer yMin, Integer yMax) {
        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
        new Thread(() -> {
            int tag = 0;
            DefaultCategoryDataset dataset = PitchDetectOrChart.createPlot(yMin, yMax);
            while (true) {
                if (!queue.isEmpty()) {
                    dataset.addValue(queue.poll(), "音高", String.valueOf(tag++));
                }
            }
        }).start();

        PitchDetectionHandler handler = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                    AudioEvent audioEvent) {
                double plot = pitchDetectionResult.getPitch();
                if (plot < 0) return;
                queue.add(Utils.convertHzToNum(plot));
            }
        };
        AudioDispatcher adp = new AudioDispatcher(
                Utils.getPcmUniversalAudioStream(file),
                2048,
                0);
        adp.addAudioProcessor(new PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.YIN,
                44100,
                2048,
                handler));
        adp.run();
    }

    /**
     * 从麦克检测音高序列，并提供可视化图表
     */
    public static void pitchDetectFormMicrophoneWithChart() {
        try {
            ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
            new Thread(() -> {
                int tag = 0;
                DefaultCategoryDataset dataset = PitchDetectOrChart.DefaultCreatePlot();
                while (true) {
                    if (!queue.isEmpty()) {
                        dataset.addValue(queue.poll(), "音高", String.valueOf(tag++));
                    }
                }
            }).start();

            PitchDetectionHandler handler = (pitchDetectionResult, audioEvent) -> {
                double plot = pitchDetectionResult.getPitch();
                if (plot < 0) return;
                queue.add(Utils.convertHzToNum(plot));
            };
            AudioDispatcher adp = AudioDispatcherFactory.fromDefaultMicrophone(
                    22050,
                    2048,
                    0);
            adp.addAudioProcessor(new PitchProcessor(
                    PitchProcessor.PitchEstimationAlgorithm.YIN,
                    44100,
                    2048,
                    handler));
            adp.run();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从麦克风检测音高序列，并返回
     * 没有可视化界面
     *
     * @param printPitch 是否输出实时音高
     * @param limit      限制最大检测时长
     * @return 音高序列
     */
    public static String pitchDetectFormMicrophone(boolean printPitch, Long limit) {
        try {
            PitchDetectionHandler handler = (pitchDetectionResult, audioEvent) -> {
                float pitch = pitchDetectionResult.getPitch();
                if (pitch < 0) return;
                if (printPitch) System.out.print(Utils.convertHzToNum(pitch) + ", ");
                processPitch(pitch);
            };
            AudioDispatcher adp = AudioDispatcherFactory.fromDefaultMicrophone(
                    22050,
                    2048,
                    0);
            adp.addAudioProcessor(new PitchProcessor(
                    PitchProcessor.PitchEstimationAlgorithm.YIN,
                    44100,
                    2048,
                    handler));

            if (limit != null) {
                new Thread(() -> {
                    try {
                        Thread.sleep(limit);
                        System.out.println("===============over===============");
                        adp.stop();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
            adp.run();
        } catch (LineUnavailableException e) {

            e.printStackTrace();
        }
        return getData();
    }

    public static StringBuilder builder = new StringBuilder();

    /**
     * 检测File对象的音高序列,转换为midi,以字符串形式返回
     *
     * @param file           文件对象
     * @param timePerSection 每小节时间 「小节节拍数*每拍的时间(ms)」
     * @return {@link String}
     */
    public static String pitchDetect(File file, float timePerSection) {
        try {
            return pitchDetect(Utils.getInputStream(file).readAllBytes(), timePerSection);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 检测byte数组的音高序列,转换为midi,以字符串形式返回
     *
     * @param timePerSection 每小节的时间 「小节节拍数*每拍的时间(ms)」
     * @param data           二进制数据
     * @return 音高序列
     */
    public static String pitchDetect(byte[] data, float timePerSection) {
        builder = new StringBuilder();
        PitchDetectionHandler handler = (pitchDetectionResult, audioEvent) -> {
            final float pitch = pitchDetectionResult.getPitch();
            processPitch(pitch);
        };
        AudioDispatcher adp = new AudioDispatcher(
                Utils.getPcmUniversalAudioStream(data),
                2048,
                0
        );

        adp.addAudioProcessor(new AudioProcessor() {
            double secondsElapsed = 0;

            @Override
            public boolean process(AudioEvent audioEvent) {
                double currentTime = audioEvent.getTimeStamp();
                if (1000 * (currentTime - secondsElapsed) > timePerSection) {
                    secondsElapsed = currentTime;

                    int len = builder.length();
                    if (len != 0 && builder.charAt(len - 1) == ',') {
                        builder.deleteCharAt(len - 1);
                        builder.append('/');
                    }
                }
//                System.out.println(currentTime);
                return true;
            }

            @Override
            public void processingFinished() {
            }
        });

        //音高检测
        adp.addAudioProcessor(new PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.YIN,
                44100,
                2048,
                handler
        ));

        adp.run();
        return getData();
    }

    /**
     * 从缓存中获取音高序列数据
     * 获取StringBuilder内的字符串并清空数据
     *
     * @return 音高序列
     */
    private static String getData() {
        if (builder.toString().endsWith(",")) {
            builder.deleteCharAt(builder.length() - 1);
        }
        if (builder.toString().endsWith("/")) {
            builder.deleteCharAt(builder.length() - 1);
        }
        String data = builder.toString();
        builder = new StringBuilder();
        return data;
    }

    /**
     * 音高预处理
     *
     * @param pitch 球场
     */
    private static void processPitch(float pitch) {
        if (pitch < 0) return;
//        System.out.print(pitch + ",");
        int midi = Utils.convertHzToNum(pitch);
        builder.append(midi).append(",");
    }


    public static void main(String[] args) throws LineUnavailableException {
//        DefaultCategoryDataset dataset = DefaultCreatePlot();
//        dataset.addValue(100, "1", "2001");
//        dataset.addValue(200, "1", "2003");
//        dataset.addValue(1300, "1", "2006");

//        File file = new File("C:\\Users\\nuclear_08\\IdeaProjects\\AiModule\\PythonCore\\src\\main\\resources\\test
//        " +.mp3");
//        File file = new File("C:\\Users\\nuclear_08\\Desktop\\music.mp3");
        File file = new File("C:\\Users\\nuclear_08\\Desktop\\test\\童年Test.mp3");
//        File file = new File("C:\\Users\\nuclear_08\\Desktop\\test\\out.mp3");
        String s = pitchDetect(file, 3000F);
        System.out.println(s);

//        pitchDetectFormMicrophoneWithChart();
    }
}
