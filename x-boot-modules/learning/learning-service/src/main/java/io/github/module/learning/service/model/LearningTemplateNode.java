package io.github.module.learning.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LearningTemplateNode {

    private String nodeCode;

    private String title;

    private String description;

    private String learningObjective;

    private String whyItMatters;

    private Integer estimatedMinutes;

    private Integer difficultyLevel;

    private String verificationMethod;

    private String completionCriteria;

    @Builder.Default
    private List<String> prerequisiteNodeCodes = new ArrayList<>();
}
