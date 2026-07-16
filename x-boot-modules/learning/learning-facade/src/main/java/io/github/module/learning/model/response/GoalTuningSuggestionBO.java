package io.github.module.learning.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Goal 调参建议 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Goal 调参建议")
public class GoalTuningSuggestionBO implements Serializable {

    private String field;
    private String title;
    private String rationale;
    private String before;
    private String after;
    private Integer priority;
}
