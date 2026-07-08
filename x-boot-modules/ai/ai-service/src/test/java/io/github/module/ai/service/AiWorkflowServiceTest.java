package io.github.module.ai.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.entity.AiAgentEntity;
import io.github.module.ai.entity.AiWorkflowDefinitionEntity;
import io.github.module.ai.entity.AiWorkflowNodeEntity;
import io.github.module.ai.mapper.AiAgentMapper;
import io.github.module.ai.mapper.AiWorkflowDefinitionMapper;
import io.github.module.ai.mapper.AiWorkflowNodeMapper;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiWorkflowDTO;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiWorkflowNodeDTO;
import io.github.module.ai.model.request.AdminListAiWorkflowDTO;
import io.github.module.ai.model.request.AdminListAiWorkflowNodeDTO;
import io.github.module.ai.model.response.AiWorkflowBO;
import io.github.module.ai.model.response.AiWorkflowNodeBO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiWorkflowServiceTest {

    @BeforeAll
    static void initTableInfo() {
        MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, AiAgentEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, AiWorkflowDefinitionEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, AiWorkflowNodeEntity.class);
    }

    @Mock
    private AiWorkflowDefinitionMapper aiWorkflowDefinitionMapper;

    @Mock
    private AiWorkflowNodeMapper aiWorkflowNodeMapper;

    @Mock
    private AiAgentMapper aiAgentMapper;

    @InjectMocks
    private AiWorkflowService aiWorkflowService;

    @Test
    void adminListReturnsPagedWorkflows() {
        AiWorkflowDefinitionEntity entity = workflow();
        Page<AiWorkflowDefinitionEntity> entityPage = new Page<AiWorkflowDefinitionEntity>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(entity));
        when(aiWorkflowDefinitionMapper.selectPage(any(), any())).thenReturn(entityPage);

        PageResult<AiWorkflowBO> result = aiWorkflowService.adminList(
                new PageParam(),
                AdminListAiWorkflowDTO.builder().name("客服").build()
        );

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().getFirst().getWorkflowCode()).isEqualTo("customer-flow");
        assertThat(result.getRecords().getFirst().getExecutionCount()).isEqualTo(2);
    }

    @Test
    void adminSelectOptionsReturnsEnabledWorkflows() {
        AiWorkflowDefinitionEntity entity = workflow();
        when(aiWorkflowDefinitionMapper.selectList(any())).thenReturn(List.of(entity));

        List<AiWorkflowBO> result = aiWorkflowService.adminSelectOptions();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getWorkflowCode()).isEqualTo("customer-flow");
        verify(aiWorkflowDefinitionMapper).selectList(any());
    }

    @Test
    void adminInsertDefaultsDraftAndChecksAgent() {
        AiAgentEntity agent = new AiAgentEntity();
        agent.setId(8L);
        when(aiAgentMapper.selectById(8L)).thenReturn(agent);
        when(aiWorkflowDefinitionMapper.selectOne(any())).thenReturn(null);
        when(aiWorkflowDefinitionMapper.insert(any(AiWorkflowDefinitionEntity.class))).thenAnswer(invocation -> {
            AiWorkflowDefinitionEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return 1;
        });

        Long id = aiWorkflowService.adminInsert(validWorkflowDto()
                .setWorkflowCode(" customer-flow ")
                .setName(" 客服 流程 ")
                .setDescription(" ")
                .setVersionNo(null)
                .setAgentId(8L)
                .setDefinitionSnapshot(" "));

        assertThat(id).isEqualTo(1L);
        ArgumentCaptor<AiWorkflowDefinitionEntity> entityCaptor =
                ArgumentCaptor.forClass(AiWorkflowDefinitionEntity.class);
        verify(aiWorkflowDefinitionMapper).insert(entityCaptor.capture());
        AiWorkflowDefinitionEntity entity = entityCaptor.getValue();
        assertThat(entity.getWorkflowCode()).isEqualTo("customer-flow");
        assertThat(entity.getName()).isEqualTo("客服流程");
        assertThat(entity.getDescription()).isEmpty();
        assertThat(entity.getVersionNo()).isEqualTo(1);
        assertThat(entity.getDefinitionSnapshot()).isEmpty();
        assertThat(entity.getPublishedSnapshot()).isEmpty();
        assertThat(entity.getPublishStatus()).isZero();
        assertThat(entity.getExecutionCount()).isZero();
    }

    @Test
    void adminInsertRejectsDuplicateWorkflowVersion() {
        AiWorkflowDefinitionEntity existing = workflow();
        existing.setId(2L);
        when(aiWorkflowDefinitionMapper.selectOne(any())).thenReturn(existing);

        assertThatThrownBy(() -> aiWorkflowService.adminInsert(validWorkflowDto().setAgentId(null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("已存在相同工作流版本，请重新输入");
        verify(aiWorkflowDefinitionMapper, never()).insert(any(AiWorkflowDefinitionEntity.class));
    }

    @Test
    void adminInsertNodeCopiesWorkflowSnapshotAndNormalizesDefaults() {
        when(aiWorkflowDefinitionMapper.selectById(1L)).thenReturn(workflow());
        when(aiWorkflowNodeMapper.selectOne(any())).thenReturn(null);
        when(aiWorkflowNodeMapper.insert(any(AiWorkflowNodeEntity.class))).thenAnswer(invocation -> {
            AiWorkflowNodeEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return 1;
        });

        Long id = aiWorkflowService.adminInsertNode(validNodeDto()
                .setNodeKey(" call-api ")
                .setNodeName(" HTTP 工具 ")
                .setNodeType(" HTTP_TOOL ")
                .setRetryCount(null)
                .setSortOrder(null));

        assertThat(id).isEqualTo(10L);
        ArgumentCaptor<AiWorkflowNodeEntity> entityCaptor = ArgumentCaptor.forClass(AiWorkflowNodeEntity.class);
        verify(aiWorkflowNodeMapper).insert(entityCaptor.capture());
        AiWorkflowNodeEntity entity = entityCaptor.getValue();
        assertThat(entity.getWorkflowDefinitionId()).isEqualTo(1L);
        assertThat(entity.getWorkflowCode()).isEqualTo("customer-flow");
        assertThat(entity.getVersionNo()).isEqualTo(1);
        assertThat(entity.getNodeKey()).isEqualTo("call-api");
        assertThat(entity.getNodeName()).isEqualTo("HTTP工具");
        assertThat(entity.getNodeType()).isEqualTo("http_tool");
        assertThat(entity.getRetryCount()).isZero();
        assertThat(entity.getSortOrder()).isZero();
    }

    @Test
    void adminInsertNodeRejectsDuplicateNodeKey() {
        AiWorkflowNodeEntity existing = node();
        existing.setId(2L);
        when(aiWorkflowDefinitionMapper.selectById(1L)).thenReturn(workflow());
        when(aiWorkflowNodeMapper.selectOne(any())).thenReturn(existing);

        assertThatThrownBy(() -> aiWorkflowService.adminInsertNode(validNodeDto()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("已存在相同工作流节点，请重新输入");
        verify(aiWorkflowNodeMapper, never()).insert(any(AiWorkflowNodeEntity.class));
    }

    @Test
    void adminInsertNodeRejectsEndNodeWithDownstream() {
        when(aiWorkflowDefinitionMapper.selectById(1L)).thenReturn(workflow());

        assertThatThrownBy(() -> aiWorkflowService.adminInsertNode(validNodeDto()
                .setNodeType("end")
                .setNextNodeKeys("next")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无效工作流节点类型");
        verify(aiWorkflowNodeMapper, never()).selectOne(any());
        verify(aiWorkflowNodeMapper, never()).insert(any(AiWorkflowNodeEntity.class));
    }

    @Test
    void adminListNodesReturnsWorkflowNodes() {
        when(aiWorkflowDefinitionMapper.selectById(1L)).thenReturn(workflow());
        AiWorkflowNodeEntity node = node();
        node.setId(10L);
        when(aiWorkflowNodeMapper.selectList(any())).thenReturn(List.of(node));

        List<AiWorkflowNodeBO> result = aiWorkflowService.adminListNodes(AdminListAiWorkflowNodeDTO.builder()
                .workflowDefinitionId(1L)
                .nodeType("llm")
                .build());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(10L);
        assertThat(result.getFirst().getNodeKey()).isEqualTo("start");
        assertThat(result.getFirst().getNodeType()).isEqualTo("llm");
    }

    @Test
    void adminDeleteRemovesDefinitionsAndNodes() {
        aiWorkflowService.adminDelete(List.of(1L, 2L));

        verify(aiWorkflowDefinitionMapper).deleteBatchIds(List.of(1L, 2L));
        verify(aiWorkflowNodeMapper).delete(any());
    }

    private AdminInsertOrUpdateAiWorkflowDTO validWorkflowDto() {
        return AdminInsertOrUpdateAiWorkflowDTO.builder()
                .workflowCode("customer-flow")
                .name("客服流程")
                .description("企业客服流程")
                .agentId(8L)
                .versionNo(1)
                .entryNodeKey("start")
                .definitionSnapshot("{}")
                .status(EnabledStatusEnum.ENABLED.getValue())
                .build();
    }

    private AdminInsertOrUpdateAiWorkflowNodeDTO validNodeDto() {
        return AdminInsertOrUpdateAiWorkflowNodeDTO.builder()
                .workflowDefinitionId(1L)
                .nodeKey("start")
                .nodeName("开始LLM")
                .nodeType("llm")
                .description("生成回复")
                .nodeConfig("{}")
                .inputMapping("{}")
                .outputMapping("{}")
                .nextNodeKeys("end")
                .conditionExpression("")
                .errorStrategy("fail")
                .retryCount(1)
                .timeoutSeconds(30L)
                .sortOrder(1)
                .status(EnabledStatusEnum.ENABLED.getValue())
                .build();
    }

    private AiWorkflowDefinitionEntity workflow() {
        AiWorkflowDefinitionEntity entity = new AiWorkflowDefinitionEntity()
                .setWorkflowCode("customer-flow")
                .setName("客服流程")
                .setDescription("企业客服流程")
                .setAgentId(8L)
                .setVersionNo(1)
                .setEntryNodeKey("start")
                .setDefinitionSnapshot("{}")
                .setPublishedSnapshot("{}")
                .setStatus(EnabledStatusEnum.ENABLED.getValue())
                .setPublishStatus(0)
                .setExecutionCount(2);
        entity.setId(1L);
        return entity;
    }

    private AiWorkflowNodeEntity node() {
        AiWorkflowNodeEntity entity = new AiWorkflowNodeEntity()
                .setWorkflowDefinitionId(1L)
                .setWorkflowCode("customer-flow")
                .setVersionNo(1)
                .setNodeKey("start")
                .setNodeName("开始LLM")
                .setNodeType("llm")
                .setNextNodeKeys("end")
                .setRetryCount(1)
                .setTimeoutSeconds(30L)
                .setSortOrder(1)
                .setStatus(EnabledStatusEnum.ENABLED.getValue());
        entity.setId(10L);
        return entity;
    }
}
