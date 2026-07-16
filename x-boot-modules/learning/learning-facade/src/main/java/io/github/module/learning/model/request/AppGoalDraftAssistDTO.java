package io.github.module.learning.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * Goal Draft Assist DTO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Goal Draft AI 补全请求")
public class AppGoalDraftAssistDTO implements Serializable {

    @Schema(description = "一句话原始意图")
    @Size(max = 2000, message = "【原始意图】最长2000位")
    private String rawIntent;

    @Schema(description = "当前 Goal Brief 草稿")
    @Valid
    private AppGoalBriefDTO brief;

    @Schema(description = "追问回答")
    @Size(max = 10, message = "【追问回答】最多10条")
    private List<@Size(max = 1000, message = "【追问回答项】最长1000位") String> followUpAnswers;
}
