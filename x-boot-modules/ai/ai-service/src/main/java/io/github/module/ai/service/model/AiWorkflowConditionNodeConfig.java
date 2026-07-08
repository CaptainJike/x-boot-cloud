package io.github.module.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 工作流条件节点配置.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiWorkflowConditionNodeConfig {

    private List<AiWorkflowConditionBranchConfig> branches;

    private String defaultNextNodeKey;

    private String outputVariable;

    private Boolean failWhenNoMatch;
}
