package io.github.module.learning.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Portfolio 候选目标验证记录 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Portfolio 候选目标验证记录")
public class PortfolioCandidateValidationRecordBO implements Serializable {

    private Long goalId;
    private String goalTitle;
    private String summary;
    private String whyBetter;
    private String firstProof;
    private String riskWatchout;
    private Integer confidence;
    private String decision;
    private String updatedAt;
    private String archivedAt;
    private String archivedReason;
}
