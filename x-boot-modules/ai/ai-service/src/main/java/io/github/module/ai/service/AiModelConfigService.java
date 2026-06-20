package io.github.module.ai.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.enums.YesOrNoEnum;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.entity.AiModelConfigEntity;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.mapper.AiModelConfigMapper;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiModelConfigDTO;
import io.github.module.ai.model.request.AdminListAiModelConfigDTO;
import io.github.module.ai.model.request.AdminListProviderModelDTO;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.model.response.AiModelConfigTestBO;
import io.github.module.ai.model.response.AiProviderModelBO;
import io.github.starter.ai.enums.AiProviderTypeEnum;
import io.github.starter.ai.service.XBootAiService;
import io.github.starter.ai.vo.AiModelConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AI 模型配置.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AiModelConfigService {

    private static final String TEST_PROMPT = "请只回复 OK，用于检测模型配置连通性。";

    private static final int ANSWER_PREVIEW_LENGTH = 500;

    private static final String DEFAULT_DASHSCOPE_BASE_URL =
            "https://dashscope.aliyuncs.com/compatible-mode/v1";

    private static final String DEFAULT_OPENAI_BASE_URL = "https://api.openai.com/v1";

    private static final String DEFAULT_DEEPSEEK_BASE_URL = "https://api.deepseek.com";

    private static final String DEFAULT_OLLAMA_BASE_URL = "http://localhost:11434";

    private final AiModelConfigMapper aiModelConfigMapper;

    private final XBootAiService xBootAiService;

    /**
     * 后台管理-分页列表.
     */
    public PageResult<AiModelConfigBO> adminList(PageParam pageParam, AdminListAiModelConfigDTO dto) {
        Page<AiModelConfigEntity> entityPage = aiModelConfigMapper.selectPage(
                new Page<>(pageParam.getPageNum(), pageParam.getPageSize()),
                new QueryWrapper<AiModelConfigEntity>()
                        .lambda()
                        .like(CharSequenceUtil.isNotBlank(dto.getCode()), AiModelConfigEntity::getCode, clean(dto.getCode()))
                        .like(CharSequenceUtil.isNotBlank(dto.getName()), AiModelConfigEntity::getName, clean(dto.getName()))
                        .eq(CharSequenceUtil.isNotBlank(dto.getProviderType()), AiModelConfigEntity::getProviderType, clean(dto.getProviderType()))
                        .like(CharSequenceUtil.isNotBlank(dto.getModelName()), AiModelConfigEntity::getModelName, clean(dto.getModelName()))
                        .eq(dto.getStatus() != null, AiModelConfigEntity::getStatus, dto.getStatus())
                        .orderByDesc(AiModelConfigEntity::getDefaultFlag)
                        .orderByDesc(AiModelConfigEntity::getCreatedAt)
        );

        return this.entityPage2BOPage(entityPage);
    }

    /**
     * 后台管理-启用模型配置下拉框.
     */
    public List<AiModelConfigBO> adminSelectOptions() {
        List<AiModelConfigEntity> entityList = aiModelConfigMapper.selectList(
                new QueryWrapper<AiModelConfigEntity>()
                        .lambda()
                        .eq(AiModelConfigEntity::getStatus, EnabledStatusEnum.ENABLED.getValue())
                        .orderByDesc(AiModelConfigEntity::getDefaultFlag)
                        .orderByAsc(AiModelConfigEntity::getName)
                        .orderByAsc(AiModelConfigEntity::getCode)
        );

        return this.entityList2BOs(entityList);
    }

    /**
     * 根据 ID 取详情.
     */
    public AiModelConfigBO getOneById(Long id) {
        return this.getOneById(id, false);
    }

    /**
     * 根据 ID 取详情.
     */
    public AiModelConfigBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException {
        AiModelConfigEntity entity = aiModelConfigMapper.selectById(id);
        if (throwIfInvalidId) {
            AiErrorEnum.INVALID_ID.assertNotNull(entity);
        }

        return this.entity2BO(entity);
    }

    /**
     * 后台管理-查看完整 API Key.
     */
    public String adminGetApiKey(Long id) throws BusinessException {
        AiModelConfigEntity entity = aiModelConfigMapper.selectById(id);
        AiErrorEnum.INVALID_ID.assertNotNull(entity);

        return cleanApiKey(entity.getApiKey());
    }

    /**
     * 根据配置编码取启用配置.
     */
    public AiModelConfigBO getEnabledConfigByCode(String code, boolean throwIfInvalidCode) throws BusinessException {
        AiModelConfigEntity entity = aiModelConfigMapper.selectOne(
                new QueryWrapper<AiModelConfigEntity>()
                        .lambda()
                        .eq(AiModelConfigEntity::getCode, clean(code))
                        .eq(AiModelConfigEntity::getStatus, EnabledStatusEnum.ENABLED.getValue())
                        .last(BaseConstant.CRUD.SQL_LIMIT_1)
        );
        if (throwIfInvalidCode) {
            AiErrorEnum.INVALID_CODE.assertNotNull(entity);
        }

        return this.entity2BO(entity);
    }

    /**
     * 取默认启用配置.
     */
    public AiModelConfigBO getDefaultEnabledConfig() {
        AiModelConfigEntity entity = aiModelConfigMapper.selectOne(
                new QueryWrapper<AiModelConfigEntity>()
                        .lambda()
                        .eq(AiModelConfigEntity::getStatus, EnabledStatusEnum.ENABLED.getValue())
                        .eq(AiModelConfigEntity::getDefaultFlag, YesOrNoEnum.YES.getValue())
                        .orderByDesc(AiModelConfigEntity::getUpdatedAt)
                        .last(BaseConstant.CRUD.SQL_LIMIT_1)
        );

        return this.entity2BO(entity);
    }

    /**
     * 后台管理-新增.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long adminInsert(AdminInsertOrUpdateAiModelConfigDTO dto) {
        log.info("[后台管理-新增AI模型配置] >> 入参={}", dto);
        this.checkExistence(dto);

        dto.setId(null);
        AiModelConfigEntity entity = new AiModelConfigEntity();
        BeanUtil.copyProperties(dto, entity);
        normalize(entity);
        checkApiKey(entity);

        this.clearOtherDefault(entity);
        aiModelConfigMapper.insert(entity);

        return entity.getId();
    }

    /**
     * 后台管理-编辑.
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdate(AdminInsertOrUpdateAiModelConfigDTO dto) {
        log.info("[后台管理-编辑AI模型配置] >> 入参={}", dto);
        AiModelConfigEntity existingEntity = aiModelConfigMapper.selectById(dto.getId());
        AiErrorEnum.INVALID_ID.assertNotNull(existingEntity);
        this.checkExistence(dto);
        dto.setApiKey(resolveUpdateApiKey(dto.getApiKey(), existingEntity.getApiKey()));

        AiModelConfigEntity entity = new AiModelConfigEntity();
        BeanUtil.copyProperties(dto, entity);
        normalize(entity);
        checkApiKey(entity);

        this.clearOtherDefault(entity);
        aiModelConfigMapper.updateById(entity);
    }

    /**
     * 后台管理-删除.
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(Collection<Long> ids) {
        log.info("[后台管理-删除AI模型配置] >> 入参={}", ids);
        aiModelConfigMapper.deleteBatchIds(ids);
    }

    /**
     * 后台管理-检测模型配置是否可用.
     */
    public AiModelConfigTestBO adminTest(Long id) {
        AiModelConfigBO bo = this.getOneById(id, true);
        long startAt = System.currentTimeMillis();
        AiModelConfigTestBO result = baseTestResult(bo);
        try {
            String answer = xBootAiService.chat(TEST_PROMPT, this.toRuntimeConfig(bo));
            return result
                    .setSuccess(true)
                    .setMessage("AI模型配置检测成功")
                    .setElapsedMilliseconds(System.currentTimeMillis() - startAt)
                    .setAnswerPreview(preview(answer));
        } catch (Exception e) {
            log.warn("[后台管理-检测AI模型配置失败] >> id={}, code={}", id, bo.getCode(), e);
            return result
                    .setSuccess(false)
                    .setMessage(rootMessage(e))
                    .setElapsedMilliseconds(System.currentTimeMillis() - startAt);
        }
    }

    /**
     * 后台管理-查询供应商模型列表.
     */
    public List<AiProviderModelBO> adminListProviderModels(AdminListProviderModelDTO dto) {
        AiProviderTypeEnum providerType = parseProviderType(dto.getProviderType());
        String apiKey = resolveProviderModelApiKey(providerType, dto);
        String baseUrl = resolveProviderModelBaseUrl(providerType, dto.getBaseUrl());

        try {
            return switch (providerType) {
                case OPENAI, OPENAI_COMPATIBLE, DEEPSEEK -> listOpenAiStyleModels(baseUrl, apiKey);
                case OLLAMA -> listOllamaModels(baseUrl);
            };
        } catch (RestClientException | IllegalArgumentException e) {
            throw new BusinessException(400, "获取模型列表失败：" + rootMessage(e));
        }
    }

    /**
     * 转换为 x-boot-starter-ai 运行时配置.
     */
    public AiModelConfig toRuntimeConfig(AiModelConfigBO bo) {
        if (bo == null) {
            return null;
        }
        String apiKey = cleanApiKey(bo.getApiKey());
        AiProviderTypeEnum providerType = parseProviderType(bo.getProviderType());
        if (providerType != AiProviderTypeEnum.OLLAMA) {
            AiErrorEnum.MISSING_API_KEY.assertNotBlank(apiKey);
        }

        return new AiModelConfig()
                .setProviderType(providerType)
                .setBaseUrl(bo.getBaseUrl())
                .setApiKey(apiKey)
                .setModelName(bo.getModelName())
                .setTemperature(bo.getTemperature())
                .setTimeout(toDuration(bo.getTimeoutSeconds()))
                .setEnabled(EnabledStatusEnum.ENABLED.getValue().equals(bo.getStatus()));
    }

    /*
    ----------------------------------------------------------------
                        私有方法 private methods
    ----------------------------------------------------------------
     */

    /**
     * 实体转 BO.
     */
    private AiModelConfigBO entity2BO(AiModelConfigEntity entity) {
        if (entity == null) {
            return null;
        }

        AiModelConfigBO bo = new AiModelConfigBO();
        BeanUtil.copyProperties(entity, bo);
        bo.setApiKeyMasked(maskApiKey(entity.getApiKey()));

        return bo;
    }

    /**
     * 实体 List 转 BO List.
     */
    private List<AiModelConfigBO> entityList2BOs(List<AiModelConfigEntity> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }

        List<AiModelConfigBO> ret = new ArrayList<>(entityList.size());
        entityList.forEach(entity -> ret.add(this.entity2BO(entity)));

        return ret;
    }

    /**
     * 实体分页转 BO 分页.
     */
    private PageResult<AiModelConfigBO> entityPage2BOPage(Page<AiModelConfigEntity> entityPage) {
        return new PageResult<AiModelConfigBO>()
                .setCurrent(entityPage.getCurrent())
                .setSize(entityPage.getSize())
                .setTotal(entityPage.getTotal())
                .setRecords(this.entityList2BOs(entityPage.getRecords()));
    }

    private AiModelConfigTestBO baseTestResult(AiModelConfigBO bo) {
        return AiModelConfigTestBO.builder()
                .code(bo.getCode())
                .name(bo.getName())
                .providerType(bo.getProviderType())
                .baseUrl(bo.getBaseUrl())
                .modelName(bo.getModelName())
                .apiKeyMasked(bo.getApiKeyMasked())
                .apiKeyPresent(StrUtil.isNotBlank(bo.getApiKey()))
                .build();
    }

    /**
     * 检查唯一编码.
     */
    private void checkExistence(AdminInsertOrUpdateAiModelConfigDTO dto) {
        AiModelConfigEntity existingEntity = aiModelConfigMapper.selectOne(
                new QueryWrapper<AiModelConfigEntity>()
                        .lambda()
                        .select(AiModelConfigEntity::getId)
                        .eq(AiModelConfigEntity::getCode, clean(dto.getCode()))
                        .last(BaseConstant.CRUD.SQL_LIMIT_1)
        );

        if (existingEntity != null && !existingEntity.getId().equals(dto.getId())) {
            throw new BusinessException(AiErrorEnum.DUPLICATE_MODEL_CONFIG);
        }
    }

    /**
     * 同一租户内只保留一个默认模型配置.
     */
    private void clearOtherDefault(AiModelConfigEntity entity) {
        if (!YesOrNoEnum.YES.getValue().equals(entity.getDefaultFlag())) {
            return;
        }
        AiModelConfigEntity updateEntity = new AiModelConfigEntity()
                .setDefaultFlag(YesOrNoEnum.NO.getValue());
        aiModelConfigMapper.update(
                updateEntity,
                new QueryWrapper<AiModelConfigEntity>()
                        .lambda()
                        .eq(AiModelConfigEntity::getDefaultFlag, YesOrNoEnum.YES.getValue())
                        .ne(entity.getId() != null, AiModelConfigEntity::getId, entity.getId())
        );
    }

    private void normalize(AiModelConfigEntity entity) {
        entity.setCode(clean(entity.getCode()));
        entity.setName(clean(entity.getName()));
        entity.setProviderType(parseProviderType(entity.getProviderType()).name());
        entity.setBaseUrl(clean(entity.getBaseUrl()));
        entity.setApiKey(cleanApiKey(entity.getApiKey()));
        entity.setModelName(clean(entity.getModelName()));
        entity.setDescription(StrUtil.blankToDefault(clean(entity.getDescription()), StrUtil.EMPTY));
    }

    private void checkApiKey(AiModelConfigEntity entity) {
        if (parseProviderType(entity.getProviderType()) != AiProviderTypeEnum.OLLAMA) {
            AiErrorEnum.MISSING_API_KEY.assertNotBlank(entity.getApiKey());
        }
    }

    private AiProviderTypeEnum parseProviderType(String providerType) {
        String cleanedProviderType = clean(providerType);
        AiErrorEnum.INVALID_PROVIDER_TYPE.assertNotBlank(cleanedProviderType);
        if (StrUtil.equalsAnyIgnoreCase(cleanedProviderType,
                "DASHSCOPE", "DASH_SCOPE", "QWEN", "TONGYI", "TONG_YI")) {
            return AiProviderTypeEnum.OPENAI_COMPATIBLE;
        }
        try {
            return AiProviderTypeEnum.valueOf(cleanedProviderType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(AiErrorEnum.INVALID_PROVIDER_TYPE);
        }
    }

    private Duration toDuration(Long timeoutSeconds) {
        if (timeoutSeconds == null) {
            return null;
        }
        return Duration.ofSeconds(timeoutSeconds);
    }

    private String preview(String answer) {
        String cleanAnswer = StrUtil.nullToEmpty(answer);
        if (cleanAnswer.length() <= ANSWER_PREVIEW_LENGTH) {
            return cleanAnswer;
        }
        return cleanAnswer.substring(0, ANSWER_PREVIEW_LENGTH);
    }

    private String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return StrUtil.blankToDefault(root.getMessage(), root.getClass().getSimpleName());
    }

    private String resolveUpdateApiKey(String inputApiKey, String existingApiKey) {
        String cleanInputApiKey = cleanApiKey(inputApiKey);
        if (StrUtil.isBlank(cleanInputApiKey)
                || StrUtil.equals(cleanInputApiKey, maskApiKey(existingApiKey))) {
            return existingApiKey;
        }
        return cleanInputApiKey;
    }

    private String resolveProviderModelApiKey(AiProviderTypeEnum providerType, AdminListProviderModelDTO dto) {
        if (providerType == AiProviderTypeEnum.OLLAMA) {
            return null;
        }
        String apiKey = cleanApiKey(dto.getApiKey());
        if (StrUtil.isBlank(apiKey) && dto.getId() != null) {
            AiModelConfigBO bo = this.getOneById(dto.getId(), true);
            apiKey = cleanApiKey(bo.getApiKey());
        }
        AiErrorEnum.MISSING_API_KEY.assertNotBlank(apiKey);
        return apiKey;
    }

    private String resolveProviderModelBaseUrl(AiProviderTypeEnum providerType, String baseUrl) {
        String cleanBaseUrl = clean(baseUrl);
        String resolvedBaseUrl = switch (providerType) {
            case OPENAI -> StrUtil.blankToDefault(cleanBaseUrl, DEFAULT_OPENAI_BASE_URL);
            case OPENAI_COMPATIBLE -> StrUtil.blankToDefault(cleanBaseUrl, DEFAULT_DASHSCOPE_BASE_URL);
            case DEEPSEEK -> StrUtil.blankToDefault(cleanBaseUrl, DEFAULT_DEEPSEEK_BASE_URL);
            case OLLAMA -> StrUtil.blankToDefault(cleanBaseUrl, DEFAULT_OLLAMA_BASE_URL);
        };
        return removeTrailingSlash(resolvedBaseUrl);
    }

    @SuppressWarnings("unchecked")
    private List<AiProviderModelBO> listOpenAiStyleModels(String baseUrl, String apiKey) {
        Map<String, Object> body = RestClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build()
                .get()
                .uri(baseUrl + "/models")
                .retrieve()
                .body(Map.class);
        Object data = body == null ? null : body.get("data");
        if (!(data instanceof List<?> dataList)) {
            return Collections.emptyList();
        }
        return dataList.stream()
                .map(this::openAiModelItem)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(AiProviderModelBO::getId))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<AiProviderModelBO> listOllamaModels(String baseUrl) {
        Map<String, Object> body = RestClient.builder()
                .build()
                .get()
                .uri(baseUrl + "/api/tags")
                .retrieve()
                .body(Map.class);
        Object models = body == null ? null : body.get("models");
        if (!(models instanceof List<?> modelList)) {
            return Collections.emptyList();
        }
        return modelList.stream()
                .map(this::ollamaModelItem)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(AiProviderModelBO::getId))
                .toList();
    }

    private AiProviderModelBO openAiModelItem(Object item) {
        if (!(item instanceof Map<?, ?> modelMap)) {
            return null;
        }
        String id = objectToString(modelMap.get("id"));
        if (StrUtil.isBlank(id)) {
            return null;
        }
        return AiProviderModelBO.builder()
                .id(id)
                .name(id)
                .build();
    }

    private AiProviderModelBO ollamaModelItem(Object item) {
        if (!(item instanceof Map<?, ?> modelMap)) {
            return null;
        }
        String name = StrUtil.blankToDefault(
                objectToString(modelMap.get("name")),
                objectToString(modelMap.get("model"))
        );
        if (StrUtil.isBlank(name)) {
            return null;
        }
        return AiProviderModelBO.builder()
                .id(name)
                .name(name)
                .build();
    }

    private String objectToString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String removeTrailingSlash(String value) {
        String result = value;
        while (StrUtil.endWith(result, "/")) {
            result = StrUtil.subBefore(result, "/", true);
        }
        return result;
    }

    private String maskApiKey(String apiKey) {
        String cleanApiKey = cleanApiKey(apiKey);
        if (StrUtil.isBlank(cleanApiKey)) {
            return null;
        }
        if (cleanApiKey.length() <= 10) {
            return "******";
        }
        return StrUtil.subPre(cleanApiKey, 6) + "******" + StrUtil.subSuf(cleanApiKey, cleanApiKey.length() - 4);
    }

    private String cleanApiKey(String value) {
        return StrUtil.trim(value);
    }

    private String clean(String value) {
        return CharSequenceUtil.cleanBlank(value);
    }
}
