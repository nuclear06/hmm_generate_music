package edu.bupt.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

/**
 * 记录用户使用服务的历史
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@TableName("tbl_user_history")
@Schema(description = "用于记录用户的服务请求历史")
public class History {
    @TableId
    @Schema(description = "历史记录id")
    @JsonIgnore
    private Integer historyId;
    @NonNull
    @Schema(description = "服务请求者id")
    @JsonIgnore
    private Integer applicantId;
    @NonNull
    @Schema(description = "时间戳")
    private Timestamp date;
    @NonNull
    @Schema(description = "请求的旋律")
    private Integer rhythm;
    @NonNull
    @TableField("`key`")
    @Schema(description = "请求的key")
    private String key;
    @NonNull
    @Schema(description = "请求的速度")
    private Integer speed;
    @NonNull
    @Schema(description = "请求的情绪值")
    private Float mention;
    @NonNull
    @Schema(description = "请求是否使用钢琴")
    private Boolean piano;
    @NonNull
    @Schema(description = "请求是否使用钢琴和弦")
    private Boolean chordPiano;
    @NonNull
    @Schema(description = "请求是否使用吉他")
    private Boolean guitar;
    @NonNull
    @Schema(description = "请求是否使用吉他和弦")
    private Boolean chordGuitar;
    @NonNull
    @Schema(description = "请求是否使用鼓")
    private Boolean drum;
    @NonNull
    @Schema(description = "请求是否使用贝斯")
    private Boolean bass;
    @NonNull
    @JsonIgnore
    @Schema(description = "存储文件的文件名")
    private String fileName;
    @NonNull
    @JsonIgnore
    @TableField(select = false)
    @Schema(description = "存储文件的md5值")
    private String fileNameMd5;
}
