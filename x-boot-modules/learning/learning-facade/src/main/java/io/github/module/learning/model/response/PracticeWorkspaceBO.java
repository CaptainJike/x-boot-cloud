package io.github.module.learning.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.framework.core.constant.BaseConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务端权威练习工作区.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "服务端权威练习工作区")
public class PracticeWorkspaceBO implements Serializable {

    private Long goalId;
    private Integer schemaVersion;
    private String mode;

    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime generatedAt;

    private Long focusNodeId;
    private String focusNodeTitle;
    private String summary;
    private List<String> masteryFocus;
    private String recommendedNextStep;
    private List<PracticeTaskBO> tasks;
    private List<PracticeAttemptBO> attempts;
}
