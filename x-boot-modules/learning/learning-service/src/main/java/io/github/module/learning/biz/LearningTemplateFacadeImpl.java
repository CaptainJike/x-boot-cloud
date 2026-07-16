package io.github.module.learning.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.module.learning.facade.LearningTemplateFacade;
import io.github.module.learning.model.request.AppTemplateUpsertDTO;
import io.github.module.learning.model.response.LearningTemplateBO;
import io.github.module.learning.service.LearningTemplateAssetService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 学习模板资产 Facade 实现.
 */
@RequiredArgsConstructor
@DubboService(version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT)
public class LearningTemplateFacadeImpl implements LearningTemplateFacade {

    private final LearningTemplateAssetService learningTemplateAssetService;

    @Override
    public List<LearningTemplateBO> getMyTemplates(String type) {
        return learningTemplateAssetService.getMyTemplates(type);
    }

    @Override
    public LearningTemplateBO getTemplateById(Long templateId) {
        return learningTemplateAssetService.getTemplateById(templateId);
    }

    @Override
    public LearningTemplateBO createTemplate(AppTemplateUpsertDTO dto) {
        return learningTemplateAssetService.createTemplate(dto);
    }

    @Override
    public LearningTemplateBO updateTemplate(Long templateId, AppTemplateUpsertDTO dto) {
        return learningTemplateAssetService.updateTemplate(templateId, dto);
    }
}
