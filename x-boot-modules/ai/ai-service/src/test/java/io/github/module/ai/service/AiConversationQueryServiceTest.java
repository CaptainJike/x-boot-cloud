package io.github.module.ai.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.entity.AiConversationEntity;
import io.github.module.ai.entity.AiMessageAttachmentEntity;
import io.github.module.ai.entity.AiMessageEntity;
import io.github.module.ai.mapper.AiConversationMapper;
import io.github.module.ai.mapper.AiMessageAttachmentMapper;
import io.github.module.ai.mapper.AiMessageMapper;
import io.github.module.ai.model.request.AdminListAiConversationDTO;
import io.github.module.ai.model.request.AdminListAiMessageDTO;
import io.github.module.ai.model.response.AdminAiConversationBO;
import io.github.module.ai.model.response.AdminAiConversationDetailBO;
import io.github.module.ai.model.response.AdminAiMessageBO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiConversationQueryServiceTest {

    @Mock
    private AiConversationMapper aiConversationMapper;

    @Mock
    private AiMessageMapper aiMessageMapper;

    @Mock
    private AiMessageAttachmentMapper aiMessageAttachmentMapper;

    private AiConversationQueryService aiConversationQueryService;

    @BeforeAll
    static void initTableInfo() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, AiConversationEntity.class);
        TableInfoHelper.initTableInfo(assistant, AiMessageEntity.class);
        TableInfoHelper.initTableInfo(assistant, AiMessageAttachmentEntity.class);
    }

    @BeforeEach
    void setUp() {
        aiConversationQueryService = new AiConversationQueryService(
                aiConversationMapper, aiMessageMapper, aiMessageAttachmentMapper);
    }

    @Test
    void adminListConversationsReturnsPagedConversationBOs() {
        Page<AiConversationEntity> entityPage = new Page<>(1, 10);
        entityPage.setTotal(1);
        entityPage.setRecords(List.of(conversation("conv-1")));
        when(aiConversationMapper.selectPage(any(), any())).thenReturn(entityPage);

        PageResult<AdminAiConversationBO> result = aiConversationQueryService.adminListConversations(
                new PageParam(), AdminListAiConversationDTO.builder().title("问候").build());

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        AdminAiConversationBO bo = result.getRecords().getFirst();
        assertThat(bo.getConversationId()).isEqualTo("conv-1");
        assertThat(bo.getTitle()).isEqualTo("问候");
        assertThat(bo.getModelConfigCode()).isEqualTo("qwen");
        assertThat(bo.getMessageCount()).isEqualTo(2);
        verify(aiConversationMapper).selectPage(any(), any());
    }

    @Test
    void adminGetConversationReturnsDetail() {
        when(aiConversationMapper.selectOne(any())).thenReturn(conversation("conv-1"));

        AdminAiConversationDetailBO detail = aiConversationQueryService.adminGetConversation("conv-1");

        assertThat(detail.getConversationId()).isEqualTo("conv-1");
        assertThat(detail.getTitle()).isEqualTo("问候");
        assertThat(detail.getProviderType()).isEqualTo("OPENAI_COMPATIBLE");
        verify(aiConversationMapper).selectOne(any());
    }

    @Test
    void adminGetConversationThrowsWhenConversationMissing() {
        when(aiConversationMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> aiConversationQueryService.adminGetConversation("missing"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无效ID");
    }

    @Test
    void adminListMessagesValidatesConversationAndReturnsPagedMessages() {
        when(aiConversationMapper.selectOne(any())).thenReturn(conversation("conv-1"));
        Page<AiMessageEntity> entityPage = new Page<>(1, 10);
        entityPage.setTotal(1);
        entityPage.setRecords(List.of(message("msg-1")));
        when(aiMessageMapper.selectPage(any(), any())).thenReturn(entityPage);

        PageResult<AdminAiMessageBO> result = aiConversationQueryService.adminListMessages(
                "conv-1", new PageParam(), AdminListAiMessageDTO.builder().role("assistant").build());

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        AdminAiMessageBO bo = result.getRecords().getFirst();
        assertThat(bo.getConversationId()).isEqualTo("conv-1");
        assertThat(bo.getMessageId()).isEqualTo("msg-1");
        assertThat(bo.getRole()).isEqualTo("assistant");
        assertThat(bo.getContent()).isEqualTo("你好呀");
        assertThat(bo.getSequenceNo()).isEqualTo(2);
        assertThat(bo.getAttachments()).isEmpty();
        verify(aiConversationMapper).selectOne(any());
        verify(aiMessageMapper).selectPage(any(), any());
    }

    @Test
    void adminListMessagesReturnsMessageAttachments() {
        when(aiConversationMapper.selectOne(any())).thenReturn(conversation("conv-1"));
        Page<AiMessageEntity> entityPage = new Page<>(1, 10);
        entityPage.setTotal(1);
        entityPage.setRecords(List.of(message("msg-1")));
        when(aiMessageMapper.selectPage(any(), any())).thenReturn(entityPage);
        when(aiMessageAttachmentMapper.selectList(any())).thenReturn(List.of(attachment("msg-1")));

        PageResult<AdminAiMessageBO> result = aiConversationQueryService.adminListMessages(
                "conv-1", new PageParam(), new AdminListAiMessageDTO());

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().getFirst().getAttachments()).hasSize(1);
        assertThat(result.getRecords().getFirst().getAttachments().getFirst().getOssFileId()).isEqualTo(11L);
        assertThat(result.getRecords().getFirst().getAttachments().getFirst().getAttachmentType()).isEqualTo("image");
        assertThat(result.getRecords().getFirst().getAttachments().getFirst().getFileName()).isEqualTo("chart.png");
    }

    private AiConversationEntity conversation(String conversationId) {
        AiConversationEntity entity = new AiConversationEntity()
                .setConversationId(conversationId)
                .setUserId(9L)
                .setTitle("问候")
                .setModelConfigId(1L)
                .setModelConfigCode("qwen")
                .setProviderType("OPENAI_COMPATIBLE")
                .setModelName("qwen-plus")
                .setStatus(1)
                .setMessageCount(2)
                .setLastMessageAt(LocalDateTime.now())
                .setLastMessagePreview("你好呀");
        entity.setId(1L);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    private AiMessageEntity message(String messageId) {
        AiMessageEntity entity = new AiMessageEntity()
                .setMessageId(messageId)
                .setConversationId("conv-1")
                .setParentMessageId("user-msg-1")
                .setRole("assistant")
                .setContent("你好呀")
                .setContentType("text")
                .setModelConfigId(1L)
                .setModelConfigCode("qwen")
                .setProviderType("OPENAI_COMPATIBLE")
                .setModelName("qwen-plus")
                .setStatus(1)
                .setSequenceNo(2)
                .setSentAt(LocalDateTime.now());
        entity.setId(2L);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    private AiMessageAttachmentEntity attachment(String messageId) {
        AiMessageAttachmentEntity entity = new AiMessageAttachmentEntity()
                .setMessageId(messageId)
                .setConversationId("conv-1")
                .setOssFileId(11L)
                .setAttachmentType("image")
                .setFileName("chart.png")
                .setMimeType("image/png")
                .setFileSize(1024L)
                .setSortNo(0);
        entity.setId(3L);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
