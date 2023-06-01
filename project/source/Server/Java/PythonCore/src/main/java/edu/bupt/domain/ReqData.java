package edu.bupt.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.hibernate.validator.constraints.Length;

/**
 * 要求数据
 * 包含请求数据的类
 *
 * @author saniter
 * @date 2023/05/24
 */
@Data
@AllArgsConstructor
public class ReqData {
    /**
     * 音高序列
     */
    @NotEmpty
    @NonNull
    private String pitchSequence;
    /**
     * 和弦
     */
    @NotEmpty
    @NonNull
    private String chord;
    /**
     * 速度
     */
    @NonNull
    @Min(0)
    private Integer speed;
    /**
     * 乐器
     */
    @NonNull
    @Length(min = 6, max = 6)
    private String instrument;
    /**
     * 情绪值
     */
    @NonNull
    @Min(0)
    private Float emo;
}
