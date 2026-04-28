package com.studyagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyagent.entity.ApiKey;
import com.studyagent.mapper.ApiKeyMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyService {

    private final ApiKeyMapper apiKeyMapper;

    private final Map<String, ApiKey> apiKeyCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initCache() {
        loadApiKeysToCache();
    }

    public void loadApiKeysToCache() {
        log.info("Loading API keys from database to cache...");
        apiKeyCache.clear();

        List<ApiKey> apiKeys = apiKeyMapper.selectList(
            new LambdaQueryWrapper<ApiKey>()
                .eq(ApiKey::getEnabled, true)
                .eq(ApiKey::getDel, 0)
        );

        for (ApiKey apiKey : apiKeys) {
            apiKeyCache.put(apiKey.getProvider(), apiKey);
            log.info("Cached API key for provider: {}", apiKey.getProvider());
        }

        log.info("Loaded {} API keys to cache", apiKeyCache.size());
    }

    public Optional<ApiKey> getApiKey(String provider) {
        return Optional.ofNullable(apiKeyCache.get(provider));
    }

    public Optional<String> getApiKeyValue(String provider) {
        return getApiKey(provider).map(ApiKey::getApiKey);
    }

    public Optional<String> getBaiduOcrSecretKey(String provider) {
        return getApiKey(provider).map(ApiKey::getBaiduOcrSecretKey);
    }

    public Optional<String> getBaseUrl(String provider) {
        return getApiKey(provider).map(ApiKey::getBaseUrl);
    }

    public Optional<String> getModelName(String provider) {
        return getApiKey(provider).map(ApiKey::getModelName);
    }
}
