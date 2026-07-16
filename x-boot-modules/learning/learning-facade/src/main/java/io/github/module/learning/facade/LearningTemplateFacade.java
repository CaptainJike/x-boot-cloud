package io.github.module.learning.facade;

import io.github.module.learning.model.request.AppTemplateUpsertDTO;
import io.github.module.learning.model.response.LearningTemplateBO;

import java.util.List;

/**
 * 学习模板资产 Facade.
 */
public interface LearningTemplateFacade {

    /**
     * 查询当前用户的模板资产.
     */
    List<LearningTemplateBO> getMyTemplates(String type);

    /**
     * 查看模板资产详情.
     */
    LearningTemplateBO getTemplateById(Long templateId);

    /**
     * 创建模板资产.
     */
    LearningTemplateBO createTemplate(AppTemplateUpsertDTO dto);

    /**
     * 更新模板资产.
     */
    LearningTemplateBO updateTemplate(Long templateId, AppTemplateUpsertDTO dto);
}
