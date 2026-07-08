package io.github.module.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 知识库文档切片配置.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiKnowledgeDocumentChunkConfig {

    public static final int DEFAULT_MAX_TOKENS = 500;

    public static final int DEFAULT_OVERLAP_TOKENS = 80;

    public static final int DEFAULT_TOKEN_CHAR_RATIO = 2;

    private Integer maxTokens;

    private Integer overlapTokens;

    private Integer tokenCharRatio;
}
