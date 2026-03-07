package com.travelplan.common.storage;

import com.travelplan.common.dto.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@ConditionalOnBean(AzureBlobStorageService.class)
public class ImageUploadController {

    private final AzureBlobStorageService storageService;

    public ImageUploadController(AzureBlobStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("container") String container) throws IOException {
        validateFile(file);
        validateContainer(container);
        String url = storageService.uploadImage(container, file);
        return ApiResponse.success(Map.of("url", url));
    }

    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, List<String>>> uploadMultipleImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("container") String container) throws IOException {
        validateContainer(container);
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            validateFile(file);
            urls.add(storageService.uploadImage(container, file));
        }
        return ApiResponse.success(Map.of("urls", urls));
    }

    @DeleteMapping
    public ApiResponse<Void> deleteImage(
            @RequestParam("url") String url,
            @RequestParam("container") String container) {
        validateContainer(container);
        storageService.deleteImage(container, url);
        return ApiResponse.success(null);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 10MB");
        }
    }

    private static final List<String> ALLOWED_CONTAINERS = List.of(
            "hotels", "vehicles", "tour-guides", "tourists", "packages", "events", "reviews", "products"
    );

    private void validateContainer(String container) {
        if (!ALLOWED_CONTAINERS.contains(container)) {
            throw new IllegalArgumentException("Invalid container: " + container);
        }
    }
}
