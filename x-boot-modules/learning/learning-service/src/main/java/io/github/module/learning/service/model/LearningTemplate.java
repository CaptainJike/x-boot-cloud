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
public class LearningTemplate {

    private String templateCode;

    private String name;

    private String description;

    @Builder.Default
    private List<String> keywords = new ArrayList<>();

    @Builder.Default
    private List<LearningTemplateNode> nodes = new ArrayList<>();
}
