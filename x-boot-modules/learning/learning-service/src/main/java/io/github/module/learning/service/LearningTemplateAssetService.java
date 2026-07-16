package io.github.module.learning.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.framework.core.context.UserContextHolder;
import io.github.framework.core.exception.BusinessException;
import io.github.module.learning.entity.LearningTemplateAssetEntity;
import io.github.module.learning.enums.LearningErrorEnum;
import io.github.module.learning.mapper.LearningTemplateAssetMapper;
import io.github.module.learning.model.request.AppGoalBriefDTO;
import io.github.module.learning.model.request.AppMapTemplateNodeDTO;
import io.github.module.learning.model.request.AppMapTemplateSnapshotDTO;
import io.github.module.learning.model.request.AppTemplateUpsertDTO;
import io.github.module.learning.model.response.GoalBriefBO;
import io.github.module.learning.model.response.LearningTemplateBO;
import io.github.module.learning.model.response.MapTemplateNodeBO;
import io.github.module.learning.model.response.MapTemplateSnapshotBO;
import io.github.module.learning.service.model.LearningTemplate;
import io.github.module.learning.service.model.LearningTemplateNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * 学习模板资产服务.
 */
@RequiredArgsConstructor
@Service
public class LearningTemplateAssetService {

    private final LearningTemplateAssetMapper learningTemplateAssetMapper;

    public List<LearningTemplateBO> getMyTemplates(String type) {
        Long userId = requireUserId();
        String normalizedType = normalizeTemplateType(type);
        QueryWrapper<LearningTemplateAssetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(LearningTemplateAssetEntity::getUserId, userId)
                .orderByDesc(LearningTemplateAssetEntity::getUpdatedAt);
        if (CharSequenceUtil.isNotBlank(normalizedType)) {
            queryWrapper.lambda().eq(LearningTemplateAssetEntity::getTemplateType, normalizedType);
        }
        return learningTemplateAssetMapper.selectList(queryWrapper).stream()
                .map(this::toTemplateBO)
                .toList();
    }

    public LearningTemplateBO getTemplateById(Long templateId) {
        return toTemplateBO(requireOwnedTemplate(templateId, requireUserId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public LearningTemplateBO createTemplate(AppTemplateUpsertDTO dto) {
        validateTemplatePayload(dto);
        LearningTemplateAssetEntity entity = LearningTemplateAssetEntity.builder()
                .userId(requireUserId())
                .templateType(normalizeTemplateType(dto.getType()))
                .name(dto.getName())
                .summary(dto.getSummary())
                .domain(dto.getDomain())
                .audience(dto.getAudience())
                .tagsJson(JSONUtil.toJsonStr(normalizeStrings(dto.getTags())))
                .visibility(dto.getVisibility())
                .marketIntent(Boolean.TRUE.equals(dto.getMarketIntent()) ? 1 : 0)
                .publishStatus(dto.getPublishStatus())
                .usageCount(0)
                .sourceType(dto.getSourceType())
                .briefJson(JSONUtil.toJsonStr(dto.getBrief()))
                .generationSummary(resolveGenerationSummary(dto))
                .mapSnapshotJson("MAP".equals(normalizeTemplateType(dto.getType()))
                        ? JSONUtil.toJsonStr(dto.getMapSnapshot())
                        : null)
                .build();
        learningTemplateAssetMapper.insert(entity);
        return toTemplateBO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public LearningTemplateBO updateTemplate(Long templateId, AppTemplateUpsertDTO dto) {
        validateTemplatePayload(dto);
        LearningTemplateAssetEntity entity = requireOwnedTemplate(templateId, requireUserId());
        entity.setTemplateType(normalizeTemplateType(dto.getType()));
        entity.setName(dto.getName());
        entity.setSummary(dto.getSummary());
        entity.setDomain(dto.getDomain());
        entity.setAudience(dto.getAudience());
        entity.setTagsJson(JSONUtil.toJsonStr(normalizeStrings(dto.getTags())));
        entity.setVisibility(dto.getVisibility());
        entity.setMarketIntent(Boolean.TRUE.equals(dto.getMarketIntent()) ? 1 : 0);
        entity.setPublishStatus(dto.getPublishStatus());
        entity.setSourceType(dto.getSourceType());
        entity.setBriefJson(JSONUtil.toJsonStr(dto.getBrief()));
        entity.setGenerationSummary(resolveGenerationSummary(dto));
        entity.setMapSnapshotJson("MAP".equals(normalizeTemplateType(dto.getType()))
                ? JSONUtil.toJsonStr(dto.getMapSnapshot())
                : null);
        learningTemplateAssetMapper.updateById(entity);
        return toTemplateBO(entity);
    }

    public LearningTemplate resolveMapTemplateSeed(String rawTemplateId, Long userId) {
        Long templateId = parseTemplateId(rawTemplateId);
        if (templateId == null) {
            return null;
        }
        LearningTemplateAssetEntity entity = requireOwnedTemplate(templateId, userId);
        if (!Objects.equals(entity.getTemplateType(), "MAP")) {
            throw new BusinessException(400, "选中的地图模板无效");
        }
        AppMapTemplateSnapshotDTO snapshot = parseMapTemplateSnapshot(entity.getMapSnapshotJson());
        if (snapshot == null || CollUtil.isEmpty(snapshot.getNodes())) {
            throw new BusinessException(400, "地图模板缺少可复用的节点快照");
        }
        AppGoalBriefDTO brief = parseGoalBrief(entity.getBriefJson());
        return LearningTemplate.builder()
                .templateCode("user-map-template-" + entity.getId())
                .name(entity.getName())
                .description(CharSequenceUtil.blankToDefault(entity.getSummary(), entity.getName()))
                .keywords(buildTemplateKeywords(entity, brief))
                .nodes(snapshot.getNodes().stream()
                        .map(node -> toLearningTemplateNode(node, brief))
                        .toList())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public void incrementUsageCountIfPresent(String rawTemplateId, Long userId, String expectedType) {
        Long templateId = parseTemplateId(rawTemplateId);
        if (templateId == null) {
            return;
        }
        LearningTemplateAssetEntity entity = learningTemplateAssetMapper.selectOne(
                new QueryWrapper<LearningTemplateAssetEntity>()
                        .lambda()
                        .eq(LearningTemplateAssetEntity::getId, templateId)
                        .eq(LearningTemplateAssetEntity::getUserId, userId)
                        .last(" LIMIT 1")
        );
        if (entity == null) {
            return;
        }
        if (CharSequenceUtil.isNotBlank(expectedType)
                && !Objects.equals(entity.getTemplateType(), normalizeTemplateType(expectedType))) {
            return;
        }
        entity.setUsageCount((entity.getUsageCount() == null ? 0 : entity.getUsageCount()) + 1);
        learningTemplateAssetMapper.updateById(entity);
    }

    private LearningTemplateAssetEntity requireOwnedTemplate(Long templateId, Long userId) {
        LearningTemplateAssetEntity entity = learningTemplateAssetMapper.selectOne(
                new QueryWrapper<LearningTemplateAssetEntity>()
                        .lambda()
                        .eq(LearningTemplateAssetEntity::getId, templateId)
                        .eq(LearningTemplateAssetEntity::getUserId, userId)
                        .last(" LIMIT 1")
        );
        LearningErrorEnum.INVALID_TEMPLATE_ASSET.assertNotNull(entity);
        return entity;
    }

    private LearningTemplateBO toTemplateBO(LearningTemplateAssetEntity entity) {
        AppGoalBriefDTO brief = parseGoalBrief(entity.getBriefJson());
        AppMapTemplateSnapshotDTO mapSnapshot = parseMapTemplateSnapshot(entity.getMapSnapshotJson());
        return LearningTemplateBO.builder()
                .id(entity.getId())
                .type(entity.getTemplateType())
                .name(entity.getName())
                .summary(entity.getSummary())
                .domain(entity.getDomain())
                .audience(entity.getAudience())
                .tags(normalizeStrings(parseStringList(entity.getTagsJson())))
                .visibility(entity.getVisibility())
                .marketIntent(entity.getMarketIntent() != null && entity.getMarketIntent() == 1)
                .publishStatus(entity.getPublishStatus())
                .usageCount(entity.getUsageCount() == null ? 0 : entity.getUsageCount())
                .sourceType(entity.getSourceType())
                .brief(toGoalBriefBO(brief))
                .generationSummary(resolveGenerationSummary(entity, mapSnapshot))
                .mapSnapshot(toMapTemplateSnapshotBO(mapSnapshot))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private GoalBriefBO toGoalBriefBO(AppGoalBriefDTO dto) {
        if (dto == null) {
            return null;
        }
        GoalBriefBO bo = new GoalBriefBO();
        BeanUtil.copyProperties(dto, bo);
        bo.setSuccessCriteria(normalizeStrings(dto.getSuccessCriteria()));
        bo.setConstraints(normalizeStrings(dto.getConstraints()));
        bo.setTags(normalizeStrings(dto.getTags()));
        return bo;
    }

    private MapTemplateSnapshotBO toMapTemplateSnapshotBO(AppMapTemplateSnapshotDTO dto) {
        if (dto == null) {
            return null;
        }
        return MapTemplateSnapshotBO.builder()
                .generationSummary(dto.getGenerationSummary())
                .estimatedDays(dto.getEstimatedDays())
                .activeNodeTitle(dto.getActiveNodeTitle())
                .nodes(CollUtil.emptyIfNull(dto.getNodes()).stream()
                        .map(this::toMapTemplateNodeBO)
                        .toList())
                .build();
    }

    private MapTemplateNodeBO toMapTemplateNodeBO(AppMapTemplateNodeDTO dto) {
        if (dto == null) {
            return null;
        }
        return MapTemplateNodeBO.builder()
                .nodeCode(dto.getNodeCode())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .learningObjective(dto.getLearningObjective())
                .whyItMatters(dto.getWhyItMatters())
                .estimatedMinutes(dto.getEstimatedMinutes())
                .difficultyLevel(dto.getDifficultyLevel())
                .verificationMethod(dto.getVerificationMethod())
                .completionCriteria(dto.getCompletionCriteria())
                .prerequisiteNodeCodes(normalizeStrings(dto.getPrerequisiteNodeCodes()))
                .build();
    }

    private LearningTemplateNode toLearningTemplateNode(AppMapTemplateNodeDTO node, AppGoalBriefDTO brief) {
        String goalTitle = brief == null ? "当前学习目标" : CharSequenceUtil.blankToDefault(brief.getTitle(), "当前学习目标");
        String nodeTitle = CharSequenceUtil.blankToDefault(node.getTitle(), "学习节点");
        return LearningTemplateNode.builder()
                .nodeCode(CharSequenceUtil.blankToDefault(node.getNodeCode(), "node-" + Math.abs(nodeTitle.hashCode())))
                .title(nodeTitle)
                .description(firstNonBlank(node.getDescription(), node.getLearningObjective(), "围绕「" + nodeTitle + "」开展一次更可执行的学习推进。"))
                .learningObjective(firstNonBlank(node.getLearningObjective(), "完成「" + nodeTitle + "」相关理解与输出。"))
                .whyItMatters(firstNonBlank(node.getWhyItMatters(), "这是通往「" + goalTitle + "」的重要一步。"))
                .estimatedMinutes(node.getEstimatedMinutes() == null ? 45 : node.getEstimatedMinutes())
                .difficultyLevel(node.getDifficultyLevel() == null ? 2 : node.getDifficultyLevel())
                .verificationMethod(firstNonBlank(node.getVerificationMethod(), "用自己的话解释，并完成一次小练习验证。"))
                .completionCriteria(firstNonBlank(node.getCompletionCriteria(), "能围绕该节点完成一次解释、应用或复盘输出。"))
                .prerequisiteNodeCodes(normalizeStrings(node.getPrerequisiteNodeCodes()))
                .build();
    }

    private List<String> buildTemplateKeywords(LearningTemplateAssetEntity entity, AppGoalBriefDTO brief) {
        Set<String> keywords = new LinkedHashSet<>();
        normalizeStrings(parseStringList(entity.getTagsJson())).forEach(item -> keywords.add(item.toLowerCase(Locale.ROOT)));
        if (CharSequenceUtil.isNotBlank(entity.getDomain())) {
            keywords.add(entity.getDomain().toLowerCase(Locale.ROOT));
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            keywords.add(entity.getName().toLowerCase(Locale.ROOT));
        }
        if (brief != null && CharSequenceUtil.isNotBlank(brief.getTitle())) {
            keywords.add(brief.getTitle().toLowerCase(Locale.ROOT));
        }
        return new ArrayList<>(keywords);
    }

    private void validateTemplatePayload(AppTemplateUpsertDTO dto) {
        if ("MAP".equals(normalizeTemplateType(dto.getType()))) {
            if (dto.getMapSnapshot() == null) {
                throw new BusinessException(400, "地图模板必须包含地图快照");
            }
            if (CollUtil.isEmpty(dto.getMapSnapshot().getNodes())) {
                throw new BusinessException(400, "地图模板至少需要一个节点");
            }
        }
    }

    private String resolveGenerationSummary(AppTemplateUpsertDTO dto) {
        if ("MAP".equals(normalizeTemplateType(dto.getType()))) {
            return firstNonBlank(
                    dto.getGenerationSummary(),
                    dto.getMapSnapshot() == null ? null : dto.getMapSnapshot().getGenerationSummary(),
                    dto.getSummary()
            );
        }
        return dto.getGenerationSummary();
    }

    private String resolveGenerationSummary(LearningTemplateAssetEntity entity, AppMapTemplateSnapshotDTO snapshot) {
        if (!Objects.equals(entity.getTemplateType(), "MAP")) {
            return entity.getGenerationSummary();
        }
        return firstNonBlank(entity.getGenerationSummary(), snapshot == null ? null : snapshot.getGenerationSummary(), entity.getSummary());
    }

    private AppGoalBriefDTO parseGoalBrief(String json) {
        if (CharSequenceUtil.isBlank(json)) {
            return null;
        }
        try {
            return JSONUtil.toBean(json, AppGoalBriefDTO.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private AppMapTemplateSnapshotDTO parseMapTemplateSnapshot(String json) {
        if (CharSequenceUtil.isBlank(json)) {
            return null;
        }
        try {
            return JSONUtil.toBean(json, AppMapTemplateSnapshotDTO.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<String> parseStringList(String json) {
        if (CharSequenceUtil.isBlank(json)) {
            return List.of();
        }
        try {
            return JSONUtil.toList(JSONUtil.parseArray(json), String.class);
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private List<String> normalizeStrings(List<String> items) {
        return CollUtil.emptyIfNull(items).stream()
                .map(CharSequenceUtil::trim)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .toList();
    }

    private String normalizeTemplateType(String type) {
        return CharSequenceUtil.blankToDefault(type, "").trim().toUpperCase(Locale.ROOT);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (CharSequenceUtil.isNotBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private Long parseTemplateId(String rawTemplateId) {
        if (CharSequenceUtil.isBlank(rawTemplateId)) {
            return null;
        }
        try {
            return Long.valueOf(rawTemplateId.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Long requireUserId() {
        Long userId = UserContextHolder.getUserId();
        LearningErrorEnum.USER_NOT_LOGGED_IN.assertNotNull(userId);
        return userId;
    }
}
