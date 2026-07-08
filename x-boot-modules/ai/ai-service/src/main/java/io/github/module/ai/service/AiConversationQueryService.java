package io.github.module.ai.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.entity.AiConversationEntity;
import io.github.module.ai.entity.AiMessageAttachmentEntity;
import io.github.module.ai.entity.AiMessageEntity;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.mapper.AiConversationMapper;
import io.github.module.ai.mapper.AiMessageAttachmentMapper;
import io.github.module.ai.mapper.AiMessageMapper;
import io.github.module.ai.model.request.AdminListAiConversationDTO;
import io.github.module.ai.model.request.AdminListAiMessageDTO;
import io.github.module.ai.model.response.AdminAiConversationBO;
import io.github.module.ai.model.response.AdminAiConversationDetailBO;
import io.github.module.ai.model.response.AdminAiMessageAttachmentBO;
import io.github.module.ai.model.response.AdminAiMessageBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 后台 AI 会话查询服务.
 */
@RequiredArgsConstructor
@Service
public class AiConversationQueryService {

    private final AiConversationMapper aiConversationMapper;

    private final AiMessageMapper aiMessageMapper;

    private final AiMessageAttachmentMapper aiMessageAttachmentMapper;

    /**
     * 后台管理-分页列表 AI 会话.
     */
    public PageResult<AdminAiConversationBO> adminListConversations(PageParam pageParam,
                                                                    AdminListAiConversationDTO dto) {
        AdminListAiConversationDTO query = safeConversationDTO(dto);
        Page<AiConversationEntity> entityPage = aiConversationMapper.selectPage(
                new Page<>(pageParam.getPageNum(), pageParam.getPageSize()),
                new QueryWrapper<AiConversationEntity>()
                        .lambda()
                        .eq(CharSequenceUtil.isNotBlank(query.getConversationId()),
                                AiConversationEntity::getConversationId, clean(query.getConversationId()))
                        .eq(query.getUserId() != null, AiConversationEntity::getUserId, query.getUserId())
                        .like(CharSequenceUtil.isNotBlank(query.getTitle()),
                                AiConversationEntity::getTitle, clean(query.getTitle()))
                        .like(CharSequenceUtil.isNotBlank(query.getModelConfigCode()),
                                AiConversationEntity::getModelConfigCode, clean(query.getModelConfigCode()))
                        .eq(CharSequenceUtil.isNotBlank(query.getProviderType()),
                                AiConversationEntity::getProviderType, clean(query.getProviderType()))
                        .like(CharSequenceUtil.isNotBlank(query.getModelName()),
                                AiConversationEntity::getModelName, clean(query.getModelName()))
                        .eq(query.getStatus() != null, AiConversationEntity::getStatus, query.getStatus())
                        .orderByDesc(AiConversationEntity::getLastMessageAt)
                        .orderByDesc(AiConversationEntity::getUpdatedAt)
        );

        return conversationPage2BOPage(entityPage);
    }

    /**
     * 后台管理-会话详情.
     */
    public AdminAiConversationDetailBO adminGetConversation(String conversationId) {
        return conversationEntity2DetailBO(getConversationEntity(conversationId, true));
    }

    /**
     * 后台管理-分页列表 AI 消息.
     */
    public PageResult<AdminAiMessageBO> adminListMessages(String conversationId,
                                                         PageParam pageParam,
                                                         AdminListAiMessageDTO dto) {
        getConversationEntity(conversationId, true);
        AdminListAiMessageDTO query = safeMessageDTO(dto);
        Page<AiMessageEntity> entityPage = aiMessageMapper.selectPage(
                new Page<>(pageParam.getPageNum(), pageParam.getPageSize()),
                new QueryWrapper<AiMessageEntity>()
                        .lambda()
                        .eq(AiMessageEntity::getConversationId, clean(conversationId))
                        .eq(CharSequenceUtil.isNotBlank(query.getRole()), AiMessageEntity::getRole, clean(query.getRole()))
                        .eq(query.getStatus() != null, AiMessageEntity::getStatus, query.getStatus())
                        .orderByAsc(AiMessageEntity::getSequenceNo)
                        .orderByAsc(AiMessageEntity::getSentAt)
        );

        return messagePage2BOPage(entityPage);
    }

    private AiConversationEntity getConversationEntity(String conversationId, boolean throwIfInvalidId) {
        AiConversationEntity entity = aiConversationMapper.selectOne(new QueryWrapper<AiConversationEntity>()
                .lambda()
                .eq(AiConversationEntity::getConversationId, clean(conversationId))
                .last(BaseConstant.CRUD.SQL_LIMIT_1));
        if (throwIfInvalidId) {
            AiErrorEnum.INVALID_ID.assertNotNull(entity);
        }
        return entity;
    }

    private PageResult<AdminAiConversationBO> conversationPage2BOPage(Page<AiConversationEntity> entityPage) {
        return new PageResult<AdminAiConversationBO>()
                .setCurrent(entityPage.getCurrent())
                .setSize(entityPage.getSize())
                .setTotal(entityPage.getTotal())
                .setRecords(conversationList2BOs(entityPage.getRecords()));
    }

    private List<AdminAiConversationBO> conversationList2BOs(List<AiConversationEntity> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }

        List<AdminAiConversationBO> ret = new ArrayList<>(entityList.size());
        entityList.forEach(entity -> ret.add(conversationEntity2BO(entity)));
        return ret;
    }

    private AdminAiConversationBO conversationEntity2BO(AiConversationEntity entity) {
        if (entity == null) {
            return null;
        }
        AdminAiConversationBO bo = new AdminAiConversationBO();
        BeanUtil.copyProperties(entity, bo);
        return bo;
    }

    private AdminAiConversationDetailBO conversationEntity2DetailBO(AiConversationEntity entity) {
        if (entity == null) {
            return null;
        }
        AdminAiConversationDetailBO bo = new AdminAiConversationDetailBO();
        BeanUtil.copyProperties(entity, bo);
        return bo;
    }

    private PageResult<AdminAiMessageBO> messagePage2BOPage(Page<AiMessageEntity> entityPage) {
        return new PageResult<AdminAiMessageBO>()
                .setCurrent(entityPage.getCurrent())
                .setSize(entityPage.getSize())
                .setTotal(entityPage.getTotal())
                .setRecords(messageList2BOs(entityPage.getRecords()));
    }

    private List<AdminAiMessageBO> messageList2BOs(List<AiMessageEntity> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }

        Map<String, List<AdminAiMessageAttachmentBO>> attachmentMap = attachmentMap(entityList);
        List<AdminAiMessageBO> ret = new ArrayList<>(entityList.size());
        entityList.forEach(entity -> ret.add(messageEntity2BO(entity, attachmentMap.get(entity.getMessageId()))));
        return ret;
    }

    private Map<String, List<AdminAiMessageAttachmentBO>> attachmentMap(List<AiMessageEntity> entityList) {
        List<String> messageIds = entityList.stream()
                .map(AiMessageEntity::getMessageId)
                .filter(CharSequenceUtil::isNotBlank)
                .toList();
        if (CollUtil.isEmpty(messageIds)) {
            return Collections.emptyMap();
        }
        List<AiMessageAttachmentEntity> attachments = aiMessageAttachmentMapper.selectList(
                new QueryWrapper<AiMessageAttachmentEntity>()
                        .lambda()
                        .in(AiMessageAttachmentEntity::getMessageId, messageIds)
                        .orderByAsc(AiMessageAttachmentEntity::getSortNo)
                        .orderByAsc(AiMessageAttachmentEntity::getId)
        );
        if (CollUtil.isEmpty(attachments)) {
            return Collections.emptyMap();
        }
        return attachments.stream()
                .collect(Collectors.groupingBy(
                        AiMessageAttachmentEntity::getMessageId,
                        Collectors.mapping(this::attachmentEntity2BO, Collectors.toList())));
    }

    private AdminAiMessageAttachmentBO attachmentEntity2BO(AiMessageAttachmentEntity entity) {
        if (entity == null) {
            return null;
        }
        AdminAiMessageAttachmentBO bo = new AdminAiMessageAttachmentBO();
        BeanUtil.copyProperties(entity, bo);
        return bo;
    }

    private AdminAiMessageBO messageEntity2BO(AiMessageEntity entity,
                                             List<AdminAiMessageAttachmentBO> attachments) {
        if (entity == null) {
            return null;
        }
        AdminAiMessageBO bo = new AdminAiMessageBO();
        BeanUtil.copyProperties(entity, bo);
        bo.setAttachments(attachments == null ? Collections.emptyList() : attachments);
        return bo;
    }

    private AdminListAiConversationDTO safeConversationDTO(AdminListAiConversationDTO dto) {
        return dto == null ? new AdminListAiConversationDTO() : dto;
    }

    private AdminListAiMessageDTO safeMessageDTO(AdminListAiMessageDTO dto) {
        return dto == null ? new AdminListAiMessageDTO() : dto;
    }

    private String clean(String value) {
        return CharSequenceUtil.cleanBlank(value);
    }
}
