package io.github.module.learning.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 知识图谱节点.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "知识图谱节点")
public class KnowledgeGraphNodeBO implements Serializable {

    private String id;
    private Long nodeId;
    private String nodeCode;
    private String title;
    private String description;
    private String learningObjective;
    private String whyItMatters;
    private String verificationMethod;
    private String completionCriteria;
    private String progressStatus;
    private String masteryLevel;
    private Integer masteryScore;
    private Integer evidenceCount;
    private List<String> prerequisiteNodeCodes;
    private List<String> unlocksNodeCodes;
    private List<String> tags;
}
