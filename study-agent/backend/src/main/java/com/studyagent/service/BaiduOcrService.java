package com.studyagent.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class BaiduOcrService {

    private static final String PROVIDER_NAME = "baidu-ocr";

    private final OkHttpClient httpClient = new OkHttpClient().newBuilder()
            .readTimeout(300, TimeUnit.SECONDS)
            .build();

    private final ApiKeyService apiKeyService;

    @Value("${baidu.ocr.token-url}")
    private String tokenUrl;

    @Value("${baidu.ocr.ocr-url}")
    private String ocrUrl;

    private String accessToken;
    private long tokenExpireTime;

    public BaiduOcrService(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    public String recognizeText(MultipartFile image) {
        try {
            log.info("Using Baidu OCR for image: name={}, size={}",
                    image.getOriginalFilename(), image.getSize());

            String accessToken = getAccessToken();
            log.info("Got access token for Baidu OCR");

            String imageBase64 = Base64.getEncoder().encodeToString(image.getBytes());

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType,
                    "image=" + java.net.URLEncoder.encode(imageBase64, "UTF-8") +
                            "&detect_direction=false&detect_language=false&paragraph=false&probability=false");

            Request request = new Request.Builder()
                    .url(ocrUrl + "?access_token=" + accessToken)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("Accept", "application/json")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Baidu OCR request failed: " + response.code());
                }

                String responseBody = response.body().string();
                log.info("Baidu OCR response received");

                JSONObject json = new JSONObject(responseBody);

                if (json.has("error_code")) {
                    throw new RuntimeException("Baidu OCR error: " + json.getString("error_msg"));
                }

                String result = extractTextFromResponse(json);
                log.info("Baidu OCR completed, text length: {}", result.length());
                return result;
            }

        } catch (IOException e) {
            log.error("Baidu OCR processing failed", e);
            throw new RuntimeException("Baidu OCR处理失败: " + e.getMessage());
        }
    }

    private String extractTextFromResponse(JSONObject json) {
        StringBuilder result = new StringBuilder();

        if (json.has("words_result")) {
            var wordsResult = json.getJSONArray("words_result");
            for (int i = 0; i < wordsResult.length(); i++) {
                var item = wordsResult.getJSONObject(i);
                if (item.has("words")) {
                    result.append(item.getString("words")).append("\n");
                }
            }
        }

        return result.toString().trim();
    }

    private synchronized String getAccessToken() throws IOException {
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }

        log.info("Requesting new access token from Baidu...");

        String apiKey = apiKeyService.getApiKeyValue(PROVIDER_NAME)
                .orElseThrow(() -> new IllegalStateException("API key not found for provider: " + PROVIDER_NAME));
        String secretKey = apiKeyService.getBaiduOcrSecretKey(PROVIDER_NAME)
                .orElseThrow(() -> new IllegalStateException("Secret key not found for provider: " + PROVIDER_NAME));

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType,
                "grant_type=client_credentials&client_id=" + apiKey +
                        "&client_secret=" + secretKey);

        Request request = new Request.Builder()
                .url(tokenUrl)
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to get access token: " + response.code());
            }

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            if (json.has("error")) {
                throw new RuntimeException("Failed to get access token: " + json.getString("error_description"));
            }

            accessToken = json.getString("access_token");
            int expiresIn = json.getInt("expires_in");
            tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L;

            log.info("Access token obtained, expires in {} seconds", expiresIn);
            return accessToken;
        }
    }
}
