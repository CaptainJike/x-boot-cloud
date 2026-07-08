package io.github.module.ai.model.request;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 后台 AI 对话请求.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminAiChatDTO implements Serializable {

    private static final String ATTACHMENT_TYPE_IMAGE = "image";

    @Schema(description = "会话ID")
    @Size(max = 64, message = "【会话ID】最长64位")
    private String conversationId;

    @Schema(description = "AI模型配置编码")
    @Size(max = 64, message = "【AI模型配置编码】最长64位")
    private String modelConfigCode;

    @Schema(description = "对话内容，文本或图片附件至少提供一个")
    @Size(max = 8000, message = "【对话内容】最长8000位")
    private String content;

    @Schema(description = "消息附件列表，第一版图片可参与模型理解，普通文件仅随消息保存")
    @Valid
    @Size(max = 6, message = "【消息附件列表】最多6个")
    private List<AdminAiChatAttachmentDTO> attachments;

    @Schema(description = "知识库ID列表，传入后启用RAG检索")
    @Size(max = 20, message = "【知识库ID列表】最多20个")
    private List<Long> knowledgeBaseIds;

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "对话内容不能为空")
    public boolean isContentOrImageAttachmentPresent() {
        if (StrUtil.isNotBlank(content)) {
            return true;
        }
        if (CollUtil.isEmpty(attachments)) {
            return false;
        }
        return attachments.stream()
                .anyMatch(attachment -> attachment != null
                        && StrUtil.equalsIgnoreCase(ATTACHMENT_TYPE_IMAGE, attachment.getAttachmentType()));
    }
}
