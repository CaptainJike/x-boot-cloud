package io.github.module.learning.service;

import cn.hutool.json.JSONUtil;
import io.github.framework.core.context.UserContext;
import io.github.framework.core.context.UserContextHolder;
import io.github.module.learning.entity.LearningTemplateAssetEntity;
import io.github.module.learning.mapper.LearningTemplateAssetMapper;
import io.github.module.learning.model.request.AppGoalBriefDTO;
import io.github.module.learning.model.request.AppMapTemplateNodeDTO;
import io.github.module.learning.model.request.AppMapTemplateSnapshotDTO;
import io.github.module.learning.model.request.AppTemplateUpsertDTO;
import io.github.module.learning.model.response.LearningTemplateBO;
import io.github.module.learning.service.model.LearningTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningTemplateAssetServiceTest {

    @Mock
    private LearningTemplateAssetMapper learningTemplateAssetMapper;

    private LearningTemplateAssetService learningTemplateAssetService;

    @BeforeEach
    void setUp() {
        UserContextHolder.setUserContext(new UserContext()
                .setUserId(9L)
                .setUserName("learner"));
        learningTemplateAssetService = new LearningTemplateAssetService(learningTemplateAssetMapper);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void createTemplatePersistsAndReturnsServerShape() {
        AppTemplateUpsertDTO dto = sampleMapTemplateDto();
        doAnswer(invocation -> {
            LearningTemplateAssetEntity entity = invocation.getArgument(0);
            entity.setId(88L);
            entity.setCreatedAt(LocalDateTime.of(2026, 7, 16, 9, 0));
            entity.setUpdatedAt(LocalDateTime.of(2026, 7, 16, 9, 0));
            return 1;
        }).when(learningTemplateAssetMapper).insert(any(LearningTemplateAssetEntity.class));

        LearningTemplateBO result = learningTemplateAssetService.createTemplate(dto);

        assertThat(result.getId()).isEqualTo(88L);
        assertThat(result.getType()).isEqualTo("MAP");
        assertThat(result.getBrief().getTitle()).isEqualTo("学习 Spring AI");
        assertThat(result.getMapSnapshot()).isNotNull();
        assertThat(result.getMapSnapshot().getNodes()).hasSize(1);
        assertThat(result.getMapSnapshot().getNodes().getFirst().getLearningObjective()).isEqualTo("理解核心调用链");
    }

    @Test
    void resolveMapTemplateSeedBuildsReusableLearningTemplateFromSnapshot() {
        AppTemplateUpsertDTO dto = sampleMapTemplateDto();
        LearningTemplateAssetEntity entity = LearningTemplateAssetEntity.builder()
                .id(88L)
                .userId(9L)
                .templateType("MAP")
                .name(dto.getName())
                .summary(dto.getSummary())
                .domain(dto.getDomain())
                .tagsJson(JSONUtil.toJsonStr(dto.getTags()))
                .briefJson(JSONUtil.toJsonStr(dto.getBrief()))
                .mapSnapshotJson(JSONUtil.toJsonStr(dto.getMapSnapshot()))
                .build();
        when(learningTemplateAssetMapper.selectOne(any())).thenReturn(entity);

        LearningTemplate template = learningTemplateAssetService.resolveMapTemplateSeed("88", 9L);

        assertThat(template.getTemplateCode()).isEqualTo("user-map-template-88");
        assertThat(template.getKeywords()).contains("spring ai");
        assertThat(template.getNodes()).hasSize(1);
        assertThat(template.getNodes().getFirst().getLearningObjective()).isEqualTo("理解核心调用链");
        assertThat(template.getNodes().getFirst().getPrerequisiteNodeCodes()).containsExactly("boot-basic");
    }

    private AppTemplateUpsertDTO sampleMapTemplateDto() {
        return AppTemplateUpsertDTO.builder()
                .type("MAP")
                .name("Spring AI 地图模板")
                .summary("适合把 Spring AI 学成一个可运行项目。")
                .audience("有 Spring Boot 基础的后端开发者")
                .domain("后端开发")
                .tags(List.of("Spring AI", "Java"))
                .visibility("PRIVATE")
                .marketIntent(Boolean.FALSE)
                .publishStatus("DRAFT")
                .sourceType("manual")
                .brief(AppGoalBriefDTO.builder()
                        .title("学习 Spring AI")
                        .domain("后端开发")
                        .desiredOutcome("完成一个 Demo")
                        .weeklyLearningMinutes(300)
                        .targetWeeks(4)
                        .preferredLearningStyle("喜欢项目练习")
                        .successCriteria(List.of("完成一个可运行项目"))
                        .constraints(List.of("希望尽量贴近真实代码"))
                        .tags(List.of("Spring AI"))
                        .build())
                .generationSummary("先搭建基础应用，再补工具调用与 Agent。")
                .mapSnapshot(AppMapTemplateSnapshotDTO.builder()
                        .generationSummary("先搭建基础应用，再补工具调用与 Agent。")
                        .estimatedDays(28)
                        .activeNodeTitle("Spring AI 基础")
                        .nodes(List.of(AppMapTemplateNodeDTO.builder()
                                .nodeCode("spring-ai-basic")
                                .title("Spring AI 基础")
                                .description("理解项目结构和基础调用链")
                                .learningObjective("理解核心调用链")
                                .whyItMatters("这是后续功能扩展的前置基础")
                                .estimatedMinutes(120)
                                .difficultyLevel(2)
                                .verificationMethod("讲清楚一次请求如何从 Controller 走到模型")
                                .completionCriteria("能独立跑通一个最小调用示例")
                                .prerequisiteNodeCodes(List.of("boot-basic"))
                                .build()))
                        .build())
                .build();
    }
}
