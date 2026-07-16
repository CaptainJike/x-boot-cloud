package io.github.module.learning.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.module.ai.facade.AiModelConfigFacade;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.learning.model.request.AppGoalBriefDTO;
import io.github.module.learning.model.response.GoalDraftAssistBO;
import io.github.module.learning.service.model.GeneratedLearningMap;
import io.github.module.learning.service.model.LearningTemplate;
import io.github.module.learning.service.model.LearningTemplateNode;
import io.github.starter.ai.enums.AiProviderTypeEnum;
import io.github.starter.ai.service.XBootAiService;
import io.github.starter.ai.vo.AiModelConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningAiServiceTest {

    @Mock
    private XBootAiService xBootAiService;

    @Mock
    private AiModelConfigFacade aiModelConfigFacade;

    private LearningAiService learningAiService;

    @BeforeEach
    void setUp() {
        learningAiService = new LearningAiService(xBootAiService, new ObjectMapper());
        ReflectionTestUtils.setField(learningAiService, "aiModelConfigFacade", aiModelConfigFacade);
    }

    @Test
    void generateLearningMapCapsRuntimeTimeoutWithinLearningWindow() {
        when(aiModelConfigFacade.getDefaultEnabledConfig()).thenReturn(defaultConfig(120L));
        when(xBootAiService.chat(anyString(), any(AiModelConfig.class))).thenReturn("""
                {
                  "generationSummary":"AI generated map",
                  "estimatedDays":14,
                  "nodes":[]
                }
                """);

        GeneratedLearningMap result = learningAiService.generateLearningMap(
                "学习 Spring AI",
                "有 Spring Boot 基础",
                180,
                "项目驱动",
                sampleTemplate()
        );

        ArgumentCaptor<AiModelConfig> configCaptor = ArgumentCaptor.forClass(AiModelConfig.class);
        verify(xBootAiService).chat(anyString(), configCaptor.capture());
        assertThat(configCaptor.getValue().getTimeout()).isEqualTo(LearningAiService.LEARNING_AI_TIMEOUT);
        assertThat(result.getGenerationSummary()).isEqualTo("AI generated map");
        assertThat(result.getEstimatedDays()).isEqualTo(14);
    }

    @Test
    void generateLearningMapKeepsShorterModelTimeout() {
        when(aiModelConfigFacade.getDefaultEnabledConfig()).thenReturn(defaultConfig(8L));
        when(xBootAiService.chat(anyString(), any(AiModelConfig.class))).thenReturn("""
                {
                  "generationSummary":"short timeout map",
                  "estimatedDays":10,
                  "nodes":[]
                }
                """);

        learningAiService.generateLearningMap(
                "学习 MCP",
                "刚入门",
                120,
                "循序渐进",
                sampleTemplate()
        );

        ArgumentCaptor<AiModelConfig> configCaptor = ArgumentCaptor.forClass(AiModelConfig.class);
        verify(xBootAiService).chat(anyString(), configCaptor.capture());
        assertThat(configCaptor.getValue().getTimeout()).isEqualTo(Duration.ofSeconds(8));
    }

    @Test
    void assistGoalDraftFallsBackToStructuredBriefWhenAiResponseInvalid() {
        when(aiModelConfigFacade.getDefaultEnabledConfig()).thenReturn(defaultConfig(30L));
        when(xBootAiService.chat(anyString(), any(AiModelConfig.class))).thenReturn("not-json");

        GoalDraftAssistBO result = learningAiService.assistGoalDraft(
                "学习 Spring AI 并做一个项目",
                AppGoalBriefDTO.builder()
                        .weeklyLearningMinutes(300)
                        .preferredLearningStyle("喜欢项目练习")
                        .build(),
                List.of("希望最后能做出一个 Demo")
        );

        assertThat(result.getDraftBrief()).isNotNull();
        assertThat(result.getDraftBrief().getTitle()).contains("学习 Spring AI");
        assertThat(result.getDraftBrief().getDomain()).isEqualTo("后端开发");
        assertThat(result.getDraftBrief().getWeeklyLearningMinutes()).isEqualTo(300);
        assertThat(result.getFollowUpQuestions()).hasSize(3);
        assertThat(result.getConfidence()).isEqualTo(0.56D);
    }

    private AiModelConfigBO defaultConfig(Long timeoutSeconds) {
        return AiModelConfigBO.builder()
                .code("default")
                .providerType(AiProviderTypeEnum.OLLAMA.name())
                .baseUrl("http://localhost:11434")
                .modelName("llama3.2")
                .temperature(0.4D)
                .timeoutSeconds(timeoutSeconds)
                .build();
    }

    private LearningTemplate sampleTemplate() {
        return LearningTemplate.builder()
                .templateCode("spring-ai")
                .name("Spring AI")
                .description("sample")
                .keywords(List.of("spring ai"))
                .nodes(List.of(LearningTemplateNode.builder()
                        .nodeCode("node-1")
                        .title("基础")
                        .description("基础描述")
                        .learningObjective("理解基础")
                        .whyItMatters("这是前置")
                        .estimatedMinutes(90)
                        .difficultyLevel(1)
                        .verificationMethod("讲清楚")
                        .completionCriteria("能独立复述")
                        .build()))
                .build();
    }
}
