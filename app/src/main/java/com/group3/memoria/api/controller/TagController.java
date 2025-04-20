package com.group3.memoria.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.annotation.PostConstruct;


@RestController
@RequestMapping("/upload-photo")
public class TagController {

    @Value("${imagga.api.key}")
    private String apiKey;

    @Value("${imagga.api.secret}")
    private String apiSecret;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.imagga.com/v2")
                .defaultHeaders(headers -> headers.setBasicAuth(apiKey, apiSecret))
                .build();
    }

    @PostMapping
    public ResponseEntity<List<String>> getTags(@RequestParam("image") MultipartFile image) {
        try {
            Resource fileResource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", fileResource);

            Map response = webClient.post()
                    .uri("/tags")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            Map<String, Object> result = (Map<String, Object>) response.get("result");
            if (result == null || !result.containsKey("tags")) {
                return ResponseEntity.ok(List.of("No tags found"));
            }

            List<Map<String, Object>> tagsRaw = (List<Map<String, Object>>) result.get("tags");

            List<String> highConfidenceTags = new ArrayList<>();
            List<TagScore> allTags = new ArrayList<>();

            for (Map<String, Object> tagEntry : tagsRaw) {
                Map<String, String> tag = (Map<String, String>) tagEntry.get("tag");
                double confidence = ((Number) tagEntry.get("confidence")).doubleValue();
                String tagText = tag.get("en");

                if (confidence >= 90.0) {
                    highConfidenceTags.add(tagText);
                }

                allTags.add(new TagScore(tagText, confidence));
            }

            if (!highConfidenceTags.isEmpty()) {
                return ResponseEntity.ok(highConfidenceTags);
            } else {
                allTags.sort((a, b) -> Double.compare(b.confidence, a.confidence));
                List<String> top3 = new ArrayList<>();
                for (int i = 0; i < Math.min(3, allTags.size()); i++) {
                    top3.add(allTags.get(i).name);
                }
                return ResponseEntity.ok(top3);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of("Error processing image: " + e.getMessage()));
        }
    }

    private static class TagScore {
        String name;
        double confidence;

        TagScore(String name, double confidence) {
            this.name = name;
            this.confidence = confidence;
        }
    }
}


