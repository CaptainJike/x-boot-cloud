package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台管理-查询供应商模型列表.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminListProviderModelDTO implements Serializable {

    @Schema(description = "配置ID，编辑时可复用已保存 API Key")
    private Long id;

    @Schema(description = "供应商类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 50, message = "【供应商类型】最长50位")
    @NotBlank(message = "供应商类型不能为空")
    private String providerType;

    @Schema(description = "模型服务地址")
    @Size(max = 255, message = "【模型服务地址】最长255位")
    private String baseUrl;

    @Schema(description = "API Key")
    @Size(max = 2048, message = "【API Key】最长2048位")
    private String apiKey;
}
