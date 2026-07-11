package io.github.module.learning.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * Tutor 会话 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Tutor 会话")
public class TutorSessionBO implements Serializable {

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "节点ID")
    private Long mapNodeId;

    @Schema(description = "节点标题")
    private String nodeTitle;

    @Schema(description = "当前状态")
    private String status;

    @Schema(description = "最新轮次")
    private TutorTurnBO latestTurn;

    @Schema(description = "历史轮次")
    private List<TutorTurnBO> turns;
}
