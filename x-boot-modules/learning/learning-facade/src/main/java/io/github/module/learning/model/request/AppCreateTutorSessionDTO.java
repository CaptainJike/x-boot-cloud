package io.github.module.learning.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 创建 Tutor 会话请求.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "创建 Tutor 会话请求")
public class AppCreateTutorSessionDTO implements Serializable {

    @Schema(description = "学习节点ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "学习节点ID不能为空")
    private Long mapNodeId;

    @Schema(description = "用户本轮提问或诉求", example = "我想理解 RAG 的检索和生成为什么要分开")
    @Size(max = 4000, message = "【用户诉求】最长4000位")
    private String learnerQuestion;
}
