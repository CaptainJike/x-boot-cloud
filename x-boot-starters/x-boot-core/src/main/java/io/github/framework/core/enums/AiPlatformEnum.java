package io.github.framework.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum AiPlatformEnum implements BaseEnum<String> {

    // ========== 国内平台 ==========

    TONG_YI("TongYi", "通义千问"), // 阿里
    YI_YAN("YiYan", "文心一言"), // 百度
    DEEP_SEEK("DeepSeek", "DeepSeek"), // DeepSeek
    ZHI_PU("ZhiPu", "智谱"), // 智谱 AI
    XING_HUO("XingHuo", "星火"), // 讯飞
    DOU_BAO("DouBao", "豆包"), // 字节
    HUN_YUAN("HunYuan", "混元"), // 腾讯
    SILICON_FLOW("SiliconFlow", "硅基流动"), // 硅基流动
    MINI_MAX("MiniMax", "MiniMax"), // 稀宇科技
    MOONSHOT("Moonshot", "月之暗面"), // KIMI
    BAI_CHUAN("BaiChuan", "百川智能"), // 百川智能

    // ========== 国外平台 ==========

    OPENAI("OpenAI", "OpenAI"), // OpenAI 官方
    GEMINI("Gemini", "Gemini"), // 谷歌 Gemini
    OLLAMA("Ollama", "Ollama"),

    STABLE_DIFFUSION("StableDiffusion", "StableDiffusion"), // Stability AI
    MIDJOURNEY("Midjourney", "Midjourney"), // Midjourney
    SUNO("Suno", "Suno"), // Suno AI
    GROK("Grok","Grok"), // Grok

    ;

    @EnumValue
    private final String value;
    private final String label;

    public static final String[] ARRAYS = Arrays.stream(values()).map(AiPlatformEnum::getValue).toArray(String[]::new);

}
