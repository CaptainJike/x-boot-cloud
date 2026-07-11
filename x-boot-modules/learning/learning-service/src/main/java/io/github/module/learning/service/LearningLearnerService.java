package io.github.module.learning.service;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.framework.core.context.TenantContext;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.module.learning.entity.LearnerAccountEntity;
import io.github.module.learning.entity.LearnerProfileEntity;
import io.github.module.learning.enums.LearningErrorEnum;
import io.github.module.learning.mapper.LearnerAccountMapper;
import io.github.module.learning.mapper.LearnerProfileMapper;
import io.github.module.learning.model.request.AppGithubLoginDTO;
import io.github.module.learning.model.response.AppLearnerLoginBO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 学习者账号服务.
 */
@RequiredArgsConstructor
@Service
public class LearningLearnerService {

    private final LearnerAccountMapper learnerAccountMapper;
    private final LearnerProfileMapper learnerProfileMapper;

    /**
     * APP 侧 GitHub 登录/注册.
     */
    @Transactional(rollbackFor = Exception.class)
    public AppLearnerLoginBO appGithubLogin(AppGithubLoginDTO dto) {
        LearningErrorEnum.INVALID_GITHUB_ACCOUNT.assertNotBlank(dto.getGithubUserId());
        LearningErrorEnum.INVALID_GITHUB_ACCOUNT.assertNotBlank(dto.getGithubLogin());

        LearnerAccountEntity accountEntity = learnerAccountMapper.getByGithubUserId(dto.getGithubUserId());
        if (accountEntity == null) {
            accountEntity = createGithubLearner(dto);
        } else {
            syncGithubLearner(accountEntity, dto);
        }

        LearningErrorEnum.LEARNER_DISABLED.assertTrue(accountEntity.getStatus() != EnabledStatusEnum.DISABLED);

        updateLastLoginAt(accountEntity.getId(), LocalDateTime.now());
        ensureLearnerProfile(accountEntity.getId());

        return AppLearnerLoginBO.builder()
                .userId(accountEntity.getId())
                .username(accountEntity.getLearnerNo())
                .nickname(accountEntity.getNickname())
                .email(accountEntity.getEmail())
                .phoneNo(accountEntity.getPhoneNo())
                .avatarUrl(accountEntity.getAvatarUrl())
                .tenantContext(new TenantContext())
                .build();
    }

    private LearnerAccountEntity createGithubLearner(AppGithubLoginDTO dto) {
        LearnerAccountEntity entity = LearnerAccountEntity.builder()
                .learnerNo(buildLearnerNo(dto.getGithubUserId()))
                .nickname(CharSequenceUtil.blankToDefault(dto.getGithubName(), dto.getGithubLogin()))
                .status(EnabledStatusEnum.ENABLED)
                .email(CharSequenceUtil.nullToEmpty(dto.getGithubEmail()))
                .avatarUrl(dto.getGithubAvatarUrl())
                .githubUserId(dto.getGithubUserId())
                .githubLogin(dto.getGithubLogin())
                .build();
        learnerAccountMapper.insert(entity);
        return entity;
    }

    private void syncGithubLearner(LearnerAccountEntity entity, AppGithubLoginDTO dto) {
        LearnerAccountEntity update = new LearnerAccountEntity();
        update.setId(entity.getId());
        update.setNickname(CharSequenceUtil.blankToDefault(dto.getGithubName(), entity.getNickname()));
        update.setEmail(CharSequenceUtil.blankToDefault(dto.getGithubEmail(), entity.getEmail()));
        update.setAvatarUrl(CharSequenceUtil.blankToDefault(dto.getGithubAvatarUrl(), entity.getAvatarUrl()));
        update.setGithubUserId(dto.getGithubUserId());
        update.setGithubLogin(dto.getGithubLogin());
        learnerAccountMapper.updateById(update);

        entity.setNickname(update.getNickname());
        entity.setEmail(update.getEmail());
        entity.setAvatarUrl(update.getAvatarUrl());
        entity.setGithubUserId(update.getGithubUserId());
        entity.setGithubLogin(update.getGithubLogin());
    }

    private void updateLastLoginAt(Long learnerId, LocalDateTime lastLoginAt) {
        LearnerAccountEntity update = new LearnerAccountEntity();
        update.setId(learnerId);
        update.setLastLoginAt(lastLoginAt);
        learnerAccountMapper.updateById(update);
    }

    private void ensureLearnerProfile(Long learnerId) {
        LearnerProfileEntity existing = learnerProfileMapper.selectOne(
                new QueryWrapper<LearnerProfileEntity>()
                        .lambda()
                        .eq(LearnerProfileEntity::getUserId, learnerId)
                        .last(" LIMIT 1")
        );
        if (existing != null) {
            return;
        }

        learnerProfileMapper.insert(LearnerProfileEntity.builder()
                .userId(learnerId)
                .preferredLearningStyle("")
                .latestSelfAssessment("")
                .focusArea("")
                .build());
    }

    private String buildLearnerNo(String githubUserId) {
        String learnerNo = "learner_" + githubUserId;
        return learnerNo.length() > 64 ? learnerNo.substring(learnerNo.length() - 64) : learnerNo;
    }
}
