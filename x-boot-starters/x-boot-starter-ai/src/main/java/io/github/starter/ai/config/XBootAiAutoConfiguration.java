package io.github.starter.ai.config;

import io.github.starter.ai.factory.XBootAiFactory;
import io.github.starter.ai.factory.XBootChatModelFactory;
import io.github.starter.ai.service.XBootAiService;
import io.github.starter.ai.service.impl.SpringAiChatService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(XBootAiFactory.class)
public class XBootAiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public XBootChatModelFactory xBootChatModelFactory() {
        return new XBootChatModelFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public XBootAiService xBootAiService(final XBootChatModelFactory xBootChatModelFactory) {
        return new SpringAiChatService(xBootChatModelFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public XBootAiFactory xBootAiFactory(final XBootAiService xBootAiService) {
        return new XBootAiFactory(xBootAiService);
    }
}
