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
public class TutorDecision {

    private String diagnosis;

    private String actionType;

    @Builder.Default
    private List<String> diagnosticQuestions = new ArrayList<>();

    private String tutorResponse;

    @Builder.Default
    private List<String> nextStepSuggestions = new ArrayList<>();

    private String recommendedNodeCode;

    private Boolean nodeCompleted;
}
