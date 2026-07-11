package io.github.module.learning.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.facade.AiModelConfigFacade;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.learning.enums.LearningErrorEnum;
import io.github.module.learning.service.model.GeneratedLearningMap;
import io.github.module.learning.service.model.LearningTemplate;
import io.github.module.learning.service.model.ReflectionSummary;
import io.github.module.learning.service.model.TutorDecision;
import io.github.starter.ai.enums.AiProviderTypeEnum;
import io.github.starter.ai.service.XBootAiService;
import io.github.starter.ai.vo.AiModelConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * Learning OS AI 编排服务.
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class LearningAiService {

    static final Duration LEARNING_AI_TIMEOUT = Duration.ofSeconds(25);

    private final XBootAiService xBootAiService;

    private final ObjectMapper objectMapper;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private AiModelConfigFacade aiModelConfigFacade;

    public GeneratedLearningMap generateLearningMap(String targetTopic,
                                                    String selfAssessment,
                                                    Integer weeklyLearningMinutes,
                                                    String preferredLearningStyle,
                                                    LearningTemplate template) {
        GeneratedLearningMap fallback = GeneratedLearningMap.builder()
                .generationSummary("已基于学习模板生成初始学习地图，并结合你的基础做了保守排序。")
                .estimatedDays(Math.max(7, (int) Math.ceil(totalMinutes(template) * 1.0 / Math.max(weeklyLearningMinutes, 30) * 7)))
                .nodes(template.getNodes())
                .build();

        String prompt = """
                你是 Learning OS 的学习路径规划器。
                请根据学习主题、用户基础、每周学习时间和模板节点，输出结构化 JSON。
                输出 JSON 格式：
                {
                  "generationSummary": "...",
                  "estimatedDays": 21,
                  "nodes": [
                    {
                      "nodeCode": "...",
                      "title": "...",
                      "description": "...",
                      "learningObjective": "...",
                      "whyItMatters": "...",
                      "estimatedMinutes": 120,
                      "difficultyLevel": 2,
                      "verificationMethod": "...",
                      "completionCriteria": "...",
                      "prerequisiteNodeCodes": ["..."]
                    }
                  ]
                }
                要求：
                1. 不要删除全部模板节点，最多只允许调整顺序、文案、难度和预计时长。
                2. 节点编码必须沿用模板已有 nodeCode。
                3. estimatedDays 需与总学习时长和 weeklyLearningMinutes 大致一致。
                4. 只返回 JSON，不要返回 Markdown。

                学习主题：%s
                用户基础：%s
                每周学习分钟数：%s
                偏好学习风格：%s
                模板：%s
                """.formatted(
                targetTopic,
                selfAssessment,
                weeklyLearningMinutes,
                preferredLearningStyle,
                JSONUtil.toJsonStr(template)
        );

        return parseOrFallback(prompt, GeneratedLearningMap.class, fallback);
    }

    public TutorDecision decideTutorTurn(String targetTopic,
                                         String nodeTitle,
                                         String nodeObjective,
                                         String learnerQuestion,
                                         List<String> previousQuestions,
                                         String learnerAnswer) {
        TutorDecision fallback = TutorDecision.builder()
                .diagnosis(CharSequenceUtil.isBlank(learnerAnswer) ? "needs_prereq" : "ready")
                .actionType(CharSequenceUtil.isBlank(learnerAnswer) ? "diagnose" : "explain")
                .diagnosticQuestions(CollUtil.isNotEmpty(previousQuestions)
                        ? previousQuestions
                        : List.of(
                        "你在这个主题里已经掌握了哪些基础概念？",
                        "如果让你用自己的话解释这个概念，你会怎么说？",
                        "你觉得这个主题最难的地方是什么？"))
                .tutorResponse(CharSequenceUtil.isBlank(learnerAnswer)
                        ? "先别急着讲答案，我们先判断你当前的理解深度。"
                        : "从你的回答看，你已经有一些基础，我先基于你的现有理解补一层关键结构。")
                .nextStepSuggestions(List.of("尝试用自己的例子复述一遍", "把关键概念画成一张图"))
                .nodeCompleted(Boolean.FALSE)
                .build();

        String prompt = """
                你是 Learning OS 的 Tutor 决策器。
                请根据用户当前节点、提问、已问过的问题和最新回答，输出 JSON：
                {
                  "diagnosis": "ready|needs_prereq|misconception",
                  "actionType": "diagnose|explain|redirect",
                  "diagnosticQuestions": ["..."],
                  "tutorResponse": "...",
                  "nextStepSuggestions": ["..."],
                  "recommendedNodeCode": "...",
                  "nodeCompleted": false
                }
                规则：
                1. 如果 learnerAnswer 为空，优先输出 2 到 4 个诊断问题。
                2. 如果用户有明显误解，diagnosis 必须为 misconception。
                3. 如果需要补前置知识，recommendedNodeCode 可为空或返回当前主题相关前置节点编码。
                4. 只返回 JSON。

                学习主题：%s
                当前节点：%s
                节点目标：%s
                用户诉求：%s
                历史诊断问题：%s
                用户最新回答：%s
                """.formatted(
                targetTopic,
                nodeTitle,
                nodeObjective,
                CharSequenceUtil.nullToEmpty(learnerQuestion),
                JSONUtil.toJsonStr(previousQuestions),
                CharSequenceUtil.nullToEmpty(learnerAnswer)
        );

        return parseOrFallback(prompt, TutorDecision.class, fallback);
    }

    public ReflectionSummary summarizeReflection(String learnedToday,
                                                 String biggestInsight,
                                                 String newAwareness,
                                                 String unresolvedQuestion,
                                                 String whyStuck) {
        ReflectionSummary fallback = ReflectionSummary.builder()
                .summary("你今天已经形成了一次完整反思，最大的价值在于把“学到了什么”和“为什么不会”同时说清楚。")
                .nextAction("明天优先回到卡住的地方，用一个例子重新验证自己的理解。")
                .keyCognitiveChanges(List.of(CharSequenceUtil.blankToDefault(biggestInsight, "开始主动总结自己的理解")))
                .commonStickingPoints(List.of(CharSequenceUtil.blankToDefault(unresolvedQuestion, "仍需进一步澄清的知识点")))
                .build();

        String prompt = """
                你是 Learning OS 的 Reflection 总结器。
                请把用户的每日反思整理成 JSON：
                {
                  "summary": "...",
                  "nextAction": "...",
                  "keyCognitiveChanges": ["..."],
                  "commonStickingPoints": ["..."]
                }
                要求：
                1. summary 体现成长而非鸡汤。
                2. nextAction 必须是明天可执行的一步。
                3. 只返回 JSON。

                今天学到了什么：%s
                今天最大的收获：%s
                今天新的认知：%s
                今天哪里不会：%s
                为什么不会：%s
                """.formatted(
                learnedToday,
                biggestInsight,
                CharSequenceUtil.nullToEmpty(newAwareness),
                unresolvedQuestion,
                whyStuck
        );

        return parseOrFallback(prompt, ReflectionSummary.class, fallback);
    }

    private int totalMinutes(LearningTemplate template) {
        return template.getNodes().stream()
                .map(node -> node.getEstimatedMinutes() == null ? 0 : node.getEstimatedMinutes())
                .reduce(0, Integer::sum);
    }

    private AiModelConfig runtimeConfig() {
        AiModelConfigBO config = aiModelConfigFacade.getDefaultEnabledConfig();
        LearningErrorEnum.INVALID_MODEL_CONFIG.assertNotNull(config);
        Duration configuredTimeout = Duration.ofSeconds(config.getTimeoutSeconds() == null ? 60 : config.getTimeoutSeconds());
        return new AiModelConfig()
                .setProviderType(AiProviderTypeEnum.safeOf(config.getProviderType()))
                .setBaseUrl(config.getBaseUrl())
                .setApiKey(config.getApiKey())
                .setModelName(config.getModelName())
                .setTemperature(config.getTemperature())
                .setTimeout(configuredTimeout.compareTo(LEARNING_AI_TIMEOUT) > 0 ? LEARNING_AI_TIMEOUT : configuredTimeout);
    }

    private <T> T parseOrFallback(String prompt, Class<T> targetType, T fallback) {
        try {
            String answer = xBootAiService.chat(prompt, runtimeConfig());
            return objectMapper.readValue(normalizeJson(answer), targetType);
        } catch (Exception e) {
            log.warn("[LearningAI] 结构化输出解析失败，使用兜底方案 >> targetType={}", targetType.getSimpleName(), e);
            return fallback;
        }
    }

    private String normalizeJson(String answer) {
        String trimmed = CharSequenceUtil.trim(answer);
        if (CharSequenceUtil.isBlank(trimmed)) {
            return trimmed;
        }
        if (trimmed.startsWith("```")) {
            int firstLineBreak = trimmed.indexOf('\n');
            if (firstLineBreak >= 0) {
                trimmed = trimmed.substring(firstLineBreak + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        return CharSequenceUtil.trim(trimmed);
    }
}
