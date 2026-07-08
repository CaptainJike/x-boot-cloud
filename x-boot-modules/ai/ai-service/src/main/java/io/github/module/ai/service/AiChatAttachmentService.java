package io.github.module.ai.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.model.request.AdminAiChatAttachmentDTO;
import io.github.module.ai.model.request.AdminAiChatDTO;
import io.github.module.ai.model.response.AdminAiMessageAttachmentBO;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.oss.facade.OssFileInfoFacade;
import io.github.module.oss.facade.OssUploadDownloadFacade;
import io.github.module.oss.model.response.OssFileDownloadReplyBO;
import io.github.module.oss.model.response.OssFileInfoBO;
import io.github.starter.ai.vo.AiChatMedia;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 后台 AI 对话附件校验与媒体转换.
 */
@RequiredArgsConstructor
@Service
public class AiChatAttachmentService {

    public static final String ATTACHMENT_TYPE_IMAGE = "image";

    public static final String ATTACHMENT_TYPE_FILE = "file";

    private static final Set<String> SUPPORTED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;

    private static final int MAX_ATTACHMENT_COUNT = 6;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private OssFileInfoFacade ossFileInfoFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private OssUploadDownloadFacade ossUploadDownloadFacade;

    /**
     * 校验并归一化聊天附件.
     */
    public ChatAttachments prepare(AdminAiChatDTO dto, AiModelConfigBO modelConfig) {
        String content = dto == null ? null : dto.getContent();
        List<AdminAiChatAttachmentDTO> attachmentDtos = dto == null ? Collections.emptyList() : dto.getAttachments();
        if (CollUtil.isEmpty(attachmentDtos)) {
            AiErrorEnum.INVALID_CHAT_CONTENT.assertNotBlank(content);
            return ChatAttachments.empty();
        }
        if (attachmentDtos.size() > MAX_ATTACHMENT_COUNT) {
            throw new BusinessException(400, "单条消息最多上传6个附件");
        }

        List<AdminAiMessageAttachmentBO> attachments = new ArrayList<>(attachmentDtos.size());
        List<AdminAiMessageAttachmentBO> imageAttachments = new ArrayList<>();
        for (int i = 0; i < attachmentDtos.size(); i++) {
            AdminAiMessageAttachmentBO attachment = normalizeAttachment(attachmentDtos.get(i), i);
            attachments.add(attachment);
            if (ATTACHMENT_TYPE_IMAGE.equals(attachment.getAttachmentType())) {
                imageAttachments.add(attachment);
            }
        }

        if (StrUtil.isBlank(content) && imageAttachments.isEmpty()) {
            throw new BusinessException(400, "仅上传普通文件时需要输入对话内容");
        }
        if (!imageAttachments.isEmpty() && !supportsImage(modelConfig)) {
            throw new BusinessException(AiErrorEnum.UNSUPPORTED_IMAGE_MODALITY);
        }
        return new ChatAttachments(Collections.unmodifiableList(attachments), Collections.unmodifiableList(imageAttachments));
    }

    /**
     * 将已校验图片附件转换成 Spring AI starter 使用的媒体请求.
     */
    public List<AiChatMedia> toImageMedia(ChatAttachments attachments) {
        if (attachments == null || CollUtil.isEmpty(attachments.images())) {
            return Collections.emptyList();
        }
        List<AiChatMedia> mediaList = new ArrayList<>(attachments.images().size());
        for (AdminAiMessageAttachmentBO image : attachments.images()) {
            mediaList.add(toMedia(image));
        }
        return mediaList;
    }

    private AdminAiMessageAttachmentBO normalizeAttachment(AdminAiChatAttachmentDTO dto, int index) {
        if (dto == null || dto.getOssFileId() == null) {
            throw new BusinessException(AiErrorEnum.INVALID_CHAT_ATTACHMENT);
        }
        OssFileInfoBO fileInfo = ossFileInfoFacade.getOneById(dto.getOssFileId(), true);
        AiErrorEnum.INVALID_CHAT_ATTACHMENT.assertNotNull(fileInfo);

        String extension = resolveExtension(fileInfo, dto);
        boolean image = SUPPORTED_IMAGE_EXTENSIONS.contains(extension);
        Long fileSize = fileInfo.getFileSize();
        if (image && fileSize != null && fileSize > MAX_IMAGE_BYTES) {
            throw new BusinessException(400, "图片附件不能超过10MB");
        }

        String attachmentType = image ? ATTACHMENT_TYPE_IMAGE : ATTACHMENT_TYPE_FILE;
        String mimeType = image ? imageMimeType(extension) : StrUtil.blankToDefault(dto.getMimeType(), "application/octet-stream");
        return AdminAiMessageAttachmentBO.builder()
                .ossFileId(fileInfo.getId())
                .attachmentType(attachmentType)
                .fileName(resolveFileName(fileInfo, dto))
                .mimeType(mimeType)
                .fileSize(fileSize)
                .sortNo(resolveSortNo(dto.getSortNo(), index))
                .build();
    }

    private AiChatMedia toMedia(AdminAiMessageAttachmentBO image) {
        OssFileDownloadReplyBO reply = ossUploadDownloadFacade.downloadById(image.getOssFileId());
        if (reply == null) {
            throw new BusinessException(AiErrorEnum.UNSUPPORTED_IMAGE_ATTACHMENT);
        }
        AiChatMedia media = new AiChatMedia()
                .setMimeType(image.getMimeType())
                .setName(image.getFileName());
        if (reply.isRedirect2DirectUrl()) {
            if (StrUtil.isBlank(reply.getDirectUrl())) {
                throw new BusinessException(AiErrorEnum.UNSUPPORTED_IMAGE_ATTACHMENT);
            }
            return media.setUri(reply.getDirectUrl());
        }
        if (reply.getFileBytes() == null || reply.getFileBytes().length == 0) {
            throw new BusinessException(AiErrorEnum.UNSUPPORTED_IMAGE_ATTACHMENT);
        }
        return media.setData(reply.getFileBytes());
    }

    private boolean supportsImage(AiModelConfigBO modelConfig) {
        if (modelConfig == null) {
            return false;
        }
        return StrUtil.split(StrUtil.nullToEmpty(modelConfig.getSupportedModalities()), ',')
                .stream()
                .map(item -> clean(item).toLowerCase())
                .anyMatch(AiModelConfigService.MODALITY_IMAGE::equals);
    }

    private String resolveExtension(OssFileInfoBO fileInfo, AdminAiChatAttachmentDTO dto) {
        String extension = clean(fileInfo.getExtendName()).toLowerCase();
        if (StrUtil.isNotBlank(extension)) {
            return StrUtil.removePrefix(extension, ".");
        }
        String fileName = StrUtil.blankToDefault(dto.getFileName(), fileInfo.getOriginalFilename());
        String suffix = StrUtil.subAfter(fileName, ".", true);
        return clean(suffix).toLowerCase();
    }

    private String resolveFileName(OssFileInfoBO fileInfo, AdminAiChatAttachmentDTO dto) {
        String inputName = clean(dto.getFileName());
        if (StrUtil.isNotBlank(inputName)) {
            return inputName;
        }
        String originalFilename = clean(fileInfo.getOriginalFilename());
        String extension = clean(fileInfo.getExtendName());
        if (StrUtil.isBlank(originalFilename)) {
            return StrUtil.blankToDefault(fileInfo.getStorageFilename(), "attachment");
        }
        if (StrUtil.isBlank(extension) || StrUtil.endWithIgnoreCase(originalFilename, "." + extension)) {
            return originalFilename;
        }
        return originalFilename + "." + extension;
    }

    private Integer resolveSortNo(Integer sortNo, int index) {
        return sortNo == null ? index : sortNo;
    }

    private String imageMimeType(String extension) {
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    private String clean(String value) {
        return CharSequenceUtil.cleanBlank(value);
    }

    public record ChatAttachments(
            List<AdminAiMessageAttachmentBO> all,
            List<AdminAiMessageAttachmentBO> images
    ) {

        public static ChatAttachments empty() {
            return new ChatAttachments(Collections.emptyList(), Collections.emptyList());
        }
    }
}
