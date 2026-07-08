package io.github.module.adminapi.web.ai;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.service.AdminAiChatStreamClient;
import io.github.module.adminapi.util.AdminStpUtil;
import io.github.module.ai.facade.AiChatFacade;
import io.github.module.ai.model.request.AdminAiChatDTO;
import io.github.module.ai.model.request.AdminListAiConversationDTO;
import io.github.module.ai.model.request.AdminListAiMessageDTO;
import io.github.module.ai.model.response.AdminAiChatBO;
import io.github.module.ai.model.response.AdminAiChatModelOptionBO;
import io.github.module.ai.model.response.AdminAiChatStreamChunkBO;
import io.github.module.ai.model.response.AdminAiConversationBO;
import io.github.module.ai.model.response.AdminAiConversationDetailBO;
import io.github.module.ai.model.response.AdminAiMessageBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAiChatControllerTest {

    @Mock
    private AiChatFacade aiChatFacade;

    @Mock
    private AdminAiChatStreamClient aiChatStreamClient;

    private AdminAiChatController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminAiChatController(aiChatStreamClient);
        ReflectionTestUtils.setField(controller, "aiChatFacade", aiChatFacade);
    }

    @Test
    void listModelOptionsReturnsEnabledChatModelsAndPassesToFacade() {
        when(aiChatFacade.adminListModelOptions()).thenReturn(List.of(AdminAiChatModelOptionBO.builder()
                .id(1L)
                .code("qwen")
                .name("通义千问")
                .providerType("OPENAI_COMPATIBLE")
                .modelName("qwen-plus")
                .defaultFlag(1)
                .description("默认模型")
                .build()));

        ApiResult<List<AdminAiChatModelOptionBO>> result = controller.listModelOptions();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().getFirst().getCode()).isEqualTo("qwen");
        assertThat(result.getData().getFirst().getModelName()).isEqualTo("qwen-plus");
        verify(aiChatFacade).adminListModelOptions();
    }

    @Test
    void listConversationsReturnsPagedDataAndPassesParamsToFacade() {
        PageParam pageParam = new PageParam();
        AdminListAiConversationDTO dto = AdminListAiConversationDTO.builder()
                .title("问候")
                .build();
        PageResult<AdminAiConversationBO> pageResult = new PageResult<AdminAiConversationBO>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(AdminAiConversationBO.builder()
                        .conversationId("conv-1")
                        .title("问候")
                        .build()));
        when(aiChatFacade.adminListConversations(pageParam, dto)).thenReturn(pageResult);

        ApiResult<PageResult<AdminAiConversationBO>> result = controller.listConversations(pageParam, dto);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getTotal()).isEqualTo(1);
        assertThat(result.getData().getRecords().getFirst().getConversationId()).isEqualTo("conv-1");
        verify(aiChatFacade).adminListConversations(pageParam, dto);
    }

    @Test
    void getConversationPassesConversationIdToFacade() {
        when(aiChatFacade.adminGetConversation("conv-1")).thenReturn(AdminAiConversationDetailBO.builder()
                .conversationId("conv-1")
                .title("问候")
                .build());

        ApiResult<AdminAiConversationDetailBO> result = controller.getConversation("conv-1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getConversationId()).isEqualTo("conv-1");
        verify(aiChatFacade).adminGetConversation("conv-1");
    }

    @Test
    void listMessagesPassesConversationIdAndParamsToFacade() {
        PageParam pageParam = new PageParam();
        AdminListAiMessageDTO dto = AdminListAiMessageDTO.builder()
                .role("assistant")
                .build();
        PageResult<AdminAiMessageBO> pageResult = new PageResult<AdminAiMessageBO>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(AdminAiMessageBO.builder()
                        .conversationId("conv-1")
                        .messageId("msg-1")
                        .role("assistant")
                        .build()));
        when(aiChatFacade.adminListMessages("conv-1", pageParam, dto)).thenReturn(pageResult);

        ApiResult<PageResult<AdminAiMessageBO>> result = controller.listMessages("conv-1", pageParam, dto);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getRecords().getFirst().getMessageId()).isEqualTo("msg-1");
        verify(aiChatFacade).adminListMessages("conv-1", pageParam, dto);
    }

    @Test
    void chatReturnsApiResultAndPassesDtoToFacade() {
        AdminAiChatDTO dto = AdminAiChatDTO.builder().content("hi").build();
        when(aiChatFacade.adminChat(any())).thenReturn(AdminAiChatBO.builder()
                .conversationId("conv-1")
                .messageId("msg-1")
                .modelConfigCode("default")
                .answer("answer")
                .build());

        ApiResult<AdminAiChatBO> result = controller.chat(dto);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getConversationId()).isEqualTo("conv-1");
        assertThat(result.getData().getMessageId()).isEqualTo("msg-1");
        assertThat(result.getData().getAnswer()).isEqualTo("answer");
        ArgumentCaptor<AdminAiChatDTO> dtoCaptor = ArgumentCaptor.forClass(AdminAiChatDTO.class);
        verify(aiChatFacade).adminChat(dtoCaptor.capture());
        assertThat(dtoCaptor.getValue()).isSameAs(dto);
    }

    @Test
    void streamMapsChunksToServerSentEvents() {
        when(aiChatStreamClient.stream(any())).thenReturn(Flux.just(AdminAiChatStreamChunkBO.builder()
                .event("message")
                .messageId("msg-1")
                .conversationId("conv-1")
                .content("hello")
                .finish(false)
                .build()));

        List<ServerSentEvent<AdminAiChatStreamChunkBO>> events = controller.stream(AdminAiChatDTO.builder()
                        .content("hi")
                        .build())
                .collectList()
                .block();

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().event()).isEqualTo("message");
        assertThat(events.getFirst().id()).isEqualTo("msg-1");
        assertThat(events.getFirst().data().getContent()).isEqualTo("hello");
        verify(aiChatStreamClient).stream(any(AdminAiChatDTO.class));
    }

    @Test
    void streamMapsErrorChunkWithoutMessageId() {
        when(aiChatStreamClient.stream(any())).thenReturn(Flux.just(AdminAiChatStreamChunkBO.builder()
                .event("error")
                .conversationId("conv-1")
                .content("AI流式对话调用失败")
                .finish(true)
                .build()));

        List<ServerSentEvent<AdminAiChatStreamChunkBO>> events = controller.stream(AdminAiChatDTO.builder()
                        .content("hi")
                        .build())
                .collectList()
                .block();

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().event()).isEqualTo("error");
        assertThat(events.getFirst().id()).isNull();
        assertThat(events.getFirst().data().getContent()).isEqualTo("AI流式对话调用失败");
        verify(aiChatStreamClient).stream(any(AdminAiChatDTO.class));
    }

    @Test
    void chatStreamAndConversationQueriesRequireDedicatedAiChatPermissions() throws Exception {
        SaCheckPermission modelOptionsPermission = getPermission("listModelOptions");
        SaCheckPermission listPermission = getPermission(
                "listConversations", PageParam.class, AdminListAiConversationDTO.class);
        SaCheckPermission detailPermission = getPermission("getConversation", String.class);
        SaCheckPermission messagePermission = getPermission(
                "listMessages", String.class, PageParam.class, AdminListAiMessageDTO.class);
        SaCheckPermission chatPermission = getPermission("chat", AdminAiChatDTO.class);
        SaCheckPermission streamPermission = getPermission("stream", AdminAiChatDTO.class);

        assertThat(listPermission.type()).isEqualTo(AdminStpUtil.TYPE);
        assertThat(modelOptionsPermission.value()).containsExactly("AiChat:retrieve");
        assertThat(modelOptionsPermission.orRole()).containsExactly("SuperAdmin");
        assertThat(listPermission.value()).containsExactly("AiChat:retrieve");
        assertThat(listPermission.orRole()).containsExactly("SuperAdmin");
        assertThat(detailPermission.value()).containsExactly("AiChat:retrieve");
        assertThat(messagePermission.value()).containsExactly("AiChat:retrieve");
        assertThat(chatPermission.type()).isEqualTo(AdminStpUtil.TYPE);
        assertThat(chatPermission.value()).containsExactly("AiChat:chat");
        assertThat(chatPermission.orRole()).containsExactly("SuperAdmin");
        assertThat(streamPermission.type()).isEqualTo(AdminStpUtil.TYPE);
        assertThat(streamPermission.value()).containsExactly("AiChat:stream");
        assertThat(streamPermission.orRole()).containsExactly("SuperAdmin");
    }

    private SaCheckPermission getPermission(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = AdminAiChatController.class.getDeclaredMethod(methodName, parameterTypes);
        SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);
        assertThat(permission).isNotNull();
        return permission;
    }
}
