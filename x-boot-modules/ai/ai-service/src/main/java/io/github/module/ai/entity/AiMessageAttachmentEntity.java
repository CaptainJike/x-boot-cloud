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
 * AI 消息附件.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName(value = "ai_message_attachment")
public class AiMessageAttachmentEntity extends BaseEntity<Long> {

    @Schema(description = "业务消息ID")
    @TableField(value = "message_id")
    private String messageId;

    @Schema(description = "业务会话ID")
    @TableField(value = "conversation_id")
    private String conversationId;

    @Schema(description = "OSS文件ID")
    @TableField(value = "oss_file_id")
    private Long ossFileId;

    @Schema(description = "附件类型(image/file)")
    @TableField(value = "attachment_type")
    private String attachmentType;

    @Schema(description = "文件名")
    @TableField(value = "file_name")
    private String fileName;

    @Schema(description = "MIME类型")
    @TableField(value = "mime_type")
    private String mimeType;

    @Schema(description = "文件大小")
    @TableField(value = "file_size")
    private Long fileSize;

    @Schema(description = "排序号")
    @TableField(value = "sort_no")
    private Integer sortNo;
}
