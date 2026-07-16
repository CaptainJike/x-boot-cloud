package io.github.module.learning.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 知识图谱边.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "知识图谱边")
public class KnowledgeGraphEdgeBO implements Serializable {

    private String id;
    private String sourceCode;
    private String targetCode;
    private String type;
    private String label;
    private String strength;
    private String summary;
}
