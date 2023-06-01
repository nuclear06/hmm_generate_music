package edu.bupt.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 记录用户基本信息和历史记录的类
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
@Schema(description = "用户数据")
public class UserData {
    @NonNull
    @Schema(description = "状态码")
    private Integer code;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "用户的请求历史数据")
    private List<History> history;
}