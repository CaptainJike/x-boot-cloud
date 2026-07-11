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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日反思 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "每日反思")
public class DailyReflectionBO implements Serializable {

    @Schema(description = "反思ID")
    private Long id;

    @Schema(description = "反思日期")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_FORMAT)
    private LocalDate reflectionDate;

    @Schema(description = "今天学到了什么")
    private String learnedToday;

    @Schema(description = "今天最大的收获")
    private String biggestInsight;

    @Schema(description = "今天新的认知")
    private String newAwareness;

    @Schema(description = "今天哪里不会")
    private String unresolvedQuestion;

    @Schema(description = "为什么不会")
    private String whyStuck;

    @Schema(description = "成长日报摘要")
    private String dailySummary;

    @Schema(description = "下一步建议")
    private String nextAction;

    @Schema(description = "创建时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;
}
