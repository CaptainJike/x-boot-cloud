package io.github.module.ai.service;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.entity.AiKnowledgeDocumentEntity;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.mapper.AiKnowledgeDocumentMapper;
import io.github.module.ai.service.model.AiKnowledgeDocumentSource;
import io.github.module.oss.facade.OssUploadDownloadFacade;
import io.github.module.oss.model.response.OssFileDownloadReplyBO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

/**
 * AI 知识库文档 OSS 来源.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AiKnowledgeDocumentSourceService {

    private final AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private OssUploadDownloadFacade ossUploadDownloadFacade;

    /**
     * 根据文档 ID 加载原始 OSS 文件来源.
     */
    public AiKnowledgeDocumentSource loadSource(Long documentId) throws BusinessException {
        AiKnowledgeDocumentEntity document = aiKnowledgeDocumentMapper.selectById(documentId);
        AiErrorEnum.INVALID_ID.assertNotNull(document);

        return this.loadSource(document);
    }

    /**
     * 根据文档记录加载原始 OSS 文件来源.
     */
    public AiKnowledgeDocumentSource loadSource(AiKnowledgeDocumentEntity document) throws BusinessException {
        log.info("[AI知识库文档OSS来源-加载原始文件] >> documentId={}, ossFileId={}",
                document.getId(),
                document.getOssFileId());
        OssFileDownloadReplyBO downloadReply = ossUploadDownloadFacade.downloadById(document.getOssFileId());
        this.checkDownloadReply(downloadReply);

        return AiKnowledgeDocumentSource.builder()
                .documentId(document.getId())
                .knowledgeBaseId(document.getKnowledgeBaseId())
                .ossFileId(document.getOssFileId())
                .documentName(document.getDocumentName())
                .originalFilename(document.getOriginalFilename())
                .extendName(document.getExtendName())
                .fileSize(document.getFileSize())
                .md5(document.getMd5())
                .storagePlatform(document.getStoragePlatform())
                .storageFilename(downloadReply.getStorageFilename())
                .directUrlSource(downloadReply.isRedirect2DirectUrl())
                .directUrl(downloadReply.getDirectUrl())
                .fileBytes(downloadReply.getFileBytes())
                .build();
    }

    private void checkDownloadReply(OssFileDownloadReplyBO downloadReply) {
        if (downloadReply == null) {
            throw new BusinessException(AiErrorEnum.KNOWLEDGE_DOCUMENT_SOURCE_UNAVAILABLE);
        }
        if (downloadReply.isRedirect2DirectUrl()) {
            if (StrUtil.isBlank(downloadReply.getDirectUrl())) {
                throw new BusinessException(AiErrorEnum.KNOWLEDGE_DOCUMENT_SOURCE_UNAVAILABLE);
            }
            return;
        }
        if (ArrayUtil.isEmpty(downloadReply.getFileBytes())) {
            throw new BusinessException(AiErrorEnum.KNOWLEDGE_DOCUMENT_SOURCE_EMPTY);
        }
    }
}
