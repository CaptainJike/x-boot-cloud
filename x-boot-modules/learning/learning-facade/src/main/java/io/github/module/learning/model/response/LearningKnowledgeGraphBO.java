package io.github.module.learning.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.framework.core.constant.BaseConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习知识图谱快照.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "学习知识图谱快照")
public class LearningKnowledgeGraphBO implements Serializable {

    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime generatedAt;

    private String mode;
    private Long focusNodeId;
    private String focusNodeTitle;
    private String summary;
    private String graphReason;
    private Integer conceptCount;
    private Integer relationCount;
    private Integer weakRelationCount;
    private List<KnowledgeGraphNodeBO> nodes;
    private List<KnowledgeGraphEdgeBO> edges;
    private List<KnowledgeGraphEvidenceBO> evidence;
    private List<String> frontier;
    private List<String> weakAreas;
    private List<String> weakPaths;
}
