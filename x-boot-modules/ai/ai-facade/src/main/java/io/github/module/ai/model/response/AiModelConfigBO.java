package io.github.module.ai.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

/**
 * AI 模型配置 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiModelConfigBO implements Serializable {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "创建时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime updatedAt;

    @Schema(description = "配置编码")
    private String code;

    @Schema(description = "配置名称")
    private String name;

    @Schema(description = "供应商类型")
    private String providerType;

    @Schema(description = "模型服务地址")
    private String baseUrl;

    @JsonIgnore
    @Schema(description = "API Key")
    private String apiKey;

    @Schema(description = "API Key 脱敏值")
    private String apiKeyMasked;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "温度参数")
    private Double temperature;

    @Schema(description = "调用超时时间，单位秒")
    private Long timeoutSeconds;

    @Schema(description = "状态(0=禁用 1=启用)")
    private Integer status;

    @Schema(description = "是否默认配置(0=否 1=是)")
    private Integer defaultFlag;

    @Schema(description = "描述")
    private String description;
}
