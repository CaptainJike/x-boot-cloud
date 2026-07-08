package io.github.module.ai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.framework.crud.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * AI 工具注册.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName(value = "ai_tool_registry")
public class AiToolRegistryEntity extends BaseEntity<Long> {

    @Schema(description = "工具编码")
    @TableField(value = "tool_code")
    private String toolCode;

    @Schema(description = "工具名称")
    @TableField(value = "name")
    private String name;

    @Schema(description = "工具描述")
    @TableField(value = "description")
    private String description;

    @Schema(description = "工具类型")
    @TableField(value = "tool_type")
    private String toolType;

    @Schema(description = "调用协议")
    @TableField(value = "protocol")
    private String protocol;

    @Schema(description = "工具入口地址")
    @TableField(value = "endpoint_url")
    private String endpointUrl;

    @Schema(description = "HTTP请求方法")
    @TableField(value = "http_method")
    private String httpMethod;

    @Schema(description = "请求参数Schema JSON")
    @TableField(value = "request_schema")
    private String requestSchema;

    @Schema(description = "响应Schema JSON")
    @TableField(value = "response_schema")
    private String responseSchema;

    @Schema(description = "工具配置Schema JSON")
    @TableField(value = "config_schema")
    private String configSchema;

    @Schema(description = "允许访问主机列表")
    @TableField(value = "allowed_hosts")
    private String allowedHosts;

    @Schema(description = "敏感字段列表")
    @TableField(value = "sensitive_fields")
    private String sensitiveFields;

    @Schema(description = "默认超时时间，单位毫秒")
    @TableField(value = "timeout_ms")
    private Long timeoutMs;

    @Schema(description = "版本号")
    @TableField(value = "version_no")
    private Integer versionNo;

    @Schema(description = "状态(0=禁用 1=启用)")
    @TableField(value = "status")
    private Integer status;

    @Schema(description = "备注")
    @TableField(value = "remark")
    private String remark;
}
