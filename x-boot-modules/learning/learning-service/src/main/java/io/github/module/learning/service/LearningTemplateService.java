package io.github.module.learning.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import io.github.framework.core.exception.BusinessException;
import io.github.module.learning.enums.LearningErrorEnum;
import io.github.module.learning.service.model.LearningTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 学习模板加载与匹配.
 */
@Service
@Slf4j
public class LearningTemplateService {

    private static final List<String> TEMPLATE_FILES = List.of(
            "templates/learning/spring-ai-template.json",
            "templates/learning/rag-template.json",
            "templates/learning/mcp-template.json",
            "templates/learning/agent-template.json"
    );

    private final List<LearningTemplate> templates = loadTemplates();

    public LearningTemplate matchTemplate(String targetTopic) {
        String normalizedTopic = CharSequenceUtil.blankToDefault(targetTopic, "").toLowerCase(Locale.ROOT);
        LearningTemplate bestTemplate = null;
        int bestScore = -1;
        for (LearningTemplate template : templates) {
            int score = template.getKeywords().stream()
                    .filter(keyword -> normalizedTopic.contains(keyword.toLowerCase(Locale.ROOT)))
                    .mapToInt(keyword -> 1)
                    .sum();
            if (score > bestScore) {
                bestScore = score;
                bestTemplate = template;
            }
        }

        if (bestTemplate == null && CollUtil.isNotEmpty(templates)) {
            bestTemplate = templates.getFirst();
        }
        LearningErrorEnum.INVALID_TEMPLATE.assertNotNull(bestTemplate);
        return bestTemplate;
    }

    private List<LearningTemplate> loadTemplates() {
        List<LearningTemplate> loaded = new ArrayList<>(TEMPLATE_FILES.size());
        for (String file : TEMPLATE_FILES) {
            try {
                String json = ResourceUtil.readUtf8Str(file);
                loaded.add(JSONUtil.toBean(json, LearningTemplate.class));
            } catch (Exception e) {
                log.warn("[LearningTemplate] 加载模板失败 >> file={}", file, e);
            }
        }
        if (loaded.isEmpty()) {
            throw new BusinessException(LearningErrorEnum.INVALID_TEMPLATE);
        }
        return loaded;
    }
}
