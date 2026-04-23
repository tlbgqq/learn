package com.studyagent.config;

import com.studyagent.service.ApiKeyService;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class LangChain4jConfig {

    private static final String DEFAULT_PROVIDER = "minimax";
    private final ApiKeyService apiKeyService;

    @Bean
    public ChatModel chatModel() {
        String apiKey = apiKeyService.getApiKeyValue(DEFAULT_PROVIDER)
                .orElseThrow(() -> new IllegalStateException("API key not found for provider: " + DEFAULT_PROVIDER));
        String baseUrl = apiKeyService.getBaseUrl(DEFAULT_PROVIDER).orElse("https://api.minimaxi.com/anthropic/v1");
        String modelName = apiKeyService.getModelName(DEFAULT_PROVIDER).orElse("MiniMax-M2.7");

        log.info("Initializing ChatModel with provider: {}, model: {}", DEFAULT_PROVIDER, modelName);

        return AnthropicChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.7)
                .maxTokens(50000)
                .build();
    }
}
