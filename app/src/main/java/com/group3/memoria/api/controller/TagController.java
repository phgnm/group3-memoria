package com.group3.memoria.api.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
public class TagController {
    @PostMapping("/api/tag")
    public List<String> getTags(@RequestParam("image") MultipartFile file) {
        try {
            // Example: Process the image and return mock data
            List<String> result = Arrays.asList(
                "Detected Object: Dog",
                "Confidence: 90%",
                "Color: Brown",
                "Size: Large"
            );
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to process image: " + e.getMessage());
        }
    }
}
