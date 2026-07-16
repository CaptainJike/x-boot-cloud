package io.github.module.learning.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.DigestUtil;
import io.github.framework.core.context.TenantContext;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.module.learning.entity.LearnerAccountEntity;
import io.github.module.learning.entity.LearnerProfileEntity;
import io.github.module.learning.enums.LearningErrorEnum;
import io.github.module.learning.mapper.LearnerAccountMapper;
import io.github.module.learning.mapper.LearnerProfileMapper;
import io.github.module.learning.model.request.AppEmailLoginDTO;
import io.github.module.learning.model.request.AppEmailRegisterDTO;
import io.github.module.learning.model.request.AppGithubLoginDTO;
import io.github.module.learning.model.response.AppLearnerLoginBO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

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

    /**
     * APP 侧邮箱验证码登录.
     */
    @Transactional(rollbackFor = Exception.class)
    public AppLearnerLoginBO appEmailLogin(AppEmailLoginDTO dto) {
        String email = normalizeEmail(dto.getEmail());
        LearningErrorEnum.INVALID_EMAIL_ACCOUNT.assertNotBlank(email);

        LearnerAccountEntity accountEntity = learnerAccountMapper.getByEmail(email);
        LearningErrorEnum.EMAIL_ACCOUNT_NOT_FOUND.assertNotNull(accountEntity);
        return completeLogin(accountEntity);
    }

    /**
     * APP 侧邮箱注册并登录.
     */
    @Transactional(rollbackFor = Exception.class)
    public AppLearnerLoginBO appEmailRegister(AppEmailRegisterDTO dto) {
        String email = normalizeEmail(dto.getEmail());
        LearningErrorEnum.INVALID_EMAIL_ACCOUNT.assertNotBlank(email);
        LearningErrorEnum.EMAIL_ACCOUNT_ALREADY_EXISTS
                .assertTrue(learnerAccountMapper.getByEmail(email) == null);

        String nickname = CharSequenceUtil.blankToDefault(dto.getNickname(), email.substring(0, email.indexOf('@')));
        LearnerAccountEntity accountEntity = LearnerAccountEntity.builder()
                .learnerNo(buildLearnerNo("email_" + DigestUtil.md5Hex(email)))
                .nickname(nickname)
                .status(EnabledStatusEnum.ENABLED)
                .email(email)
                .build();
        learnerAccountMapper.insert(accountEntity);
        return completeLogin(accountEntity);
    }

    private AppLearnerLoginBO completeLogin(LearnerAccountEntity accountEntity) {
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
                .email(normalizeNullableEmail(dto.getGithubEmail()))
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
        update.setEmail(CharSequenceUtil.isBlank(dto.getGithubEmail())
                ? entity.getEmail() : normalizeNullableEmail(dto.getGithubEmail()));
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

    private String buildLearnerNo(String accountKey) {
        String learnerNo = "learner_" + accountKey.replaceAll("[^a-zA-Z0-9]", "_");
        return learnerNo.length() > 64 ? learnerNo.substring(learnerNo.length() - 64) : learnerNo;
    }

    private String normalizeEmail(String email) {
        String trimmedEmail = CharSequenceUtil.trim(email);
        return trimmedEmail == null ? null : trimmedEmail.toLowerCase(Locale.ROOT);
    }

    private String normalizeNullableEmail(String email) {
        return CharSequenceUtil.isBlank(email) ? null : normalizeEmail(email);
    }
}
