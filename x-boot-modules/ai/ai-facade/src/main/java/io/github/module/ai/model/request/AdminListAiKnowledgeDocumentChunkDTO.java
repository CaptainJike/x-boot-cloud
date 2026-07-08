package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台管理-分页列表知识库文档切片.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminListAiKnowledgeDocumentChunkDTO implements Serializable {

    @Schema(description = "切片内容关键词")
    private String keyword;

    @Schema(description = "切片状态(0=失败 1=成功 2=处理中)")
    private Integer status;

    @Schema(description = "向量化状态(0=失败 1=成功 2=处理中 3=待处理)")
    private Integer embeddingStatus;
}
