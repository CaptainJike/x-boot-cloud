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
public class ReflectionSummary {

    private String summary;

    private String nextAction;

    @Builder.Default
    private List<String> keyCognitiveChanges = new ArrayList<>();

    @Builder.Default
    private List<String> commonStickingPoints = new ArrayList<>();
}
