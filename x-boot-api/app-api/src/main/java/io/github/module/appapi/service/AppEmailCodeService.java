package io.github.module.appapi.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.DigestUtil;
import io.github.framework.core.exception.BusinessException;
import io.github.module.appapi.model.request.AppEmailCodeScene;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * APP 邮箱验证码服务，负责验证码生命周期与邮件投递，不负责学习者账号业务。
 */
@RequiredArgsConstructor
@Service
public class AppEmailCodeService {

    private static final String CODE_CACHE_KEY = "LearningOS:auth:email:code:%s:%s";
    private static final String COOLDOWN_CACHE_KEY = "LearningOS:auth:email:cooldown:%s:%s";
    private static final long CODE_TTL_SECONDS = 10 * 60;
    private static final long COOLDOWN_SECONDS = 60;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.from:${spring.mail.username:}}")
    private String from;

    public void sendCode(String email, AppEmailCodeScene scene) {
        String normalizedEmail = normalizeEmail(email);
        String keySuffix = keySuffix(normalizedEmail);
        String cooldownKey = String.format(COOLDOWN_CACHE_KEY, scene.name(), keySuffix);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            throw new BusinessException(429, "验证码发送过于频繁，请稍后再试");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        sendEmail(normalizedEmail, code);
        redisTemplate.opsForValue().set(
                String.format(CODE_CACHE_KEY, scene.name(), keySuffix), code, CODE_TTL_SECONDS, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);
    }

    public void verifyCode(String email, String code, AppEmailCodeScene scene) {
        String key = String.format(CODE_CACHE_KEY, scene.name(), keySuffix(normalizeEmail(email)));
        Object cachedCode = redisTemplate.opsForValue().get(key);
        if (cachedCode == null || !code.equals(String.valueOf(cachedCode))) {
            throw new BusinessException(400, "验证码不正确或已过期");
        }
        redisTemplate.delete(key);
    }

    private void sendEmail(String email, String code) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || CharSequenceUtil.isBlank(from)) {
            throw new BusinessException(500, "邮箱服务尚未配置，请联系管理员");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("Learning OS 登录验证码");
            helper.setText("你的 Learning OS 验证码是：<b>" + code
                    + "</b><br/>验证码 10 分钟内有效，请勿将验证码透露给他人。", true);
            mailSender.send(message);
        } catch (Exception exception) {
            throw new BusinessException(500, "验证码邮件发送失败，请稍后再试");
        }
    }

    private String normalizeEmail(String email) {
        String trimmedEmail = CharSequenceUtil.trim(email);
        return trimmedEmail == null ? null : trimmedEmail.toLowerCase(Locale.ROOT);
    }

    private String keySuffix(String email) {
        return DigestUtil.sha256Hex(email);
    }
}
