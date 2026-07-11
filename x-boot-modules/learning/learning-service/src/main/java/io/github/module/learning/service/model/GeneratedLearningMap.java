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
public class GeneratedLearningMap {

    private String generationSummary;

    private Integer estimatedDays;

    @Builder.Default
    private List<LearningTemplateNode> nodes = new ArrayList<>();
}
