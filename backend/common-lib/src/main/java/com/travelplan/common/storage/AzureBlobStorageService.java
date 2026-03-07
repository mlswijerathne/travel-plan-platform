package com.travelplan.common.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "azure.storage.connection-string")
public class AzureBlobStorageService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    private BlobServiceClient blobServiceClient;

    @PostConstruct
    public void init() {
        blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    public String uploadImage(String containerName, MultipartFile file) throws IOException {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String blobName = UUID.randomUUID() + extension;

        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.upload(file.getInputStream(), file.getSize(), true);

        blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(file.getContentType()));

        return blobClient.getBlobUrl();
    }

    public void deleteImage(String containerName, String blobUrl) {
        String blobName = extractBlobName(blobUrl);
        if (blobName == null) return;

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.deleteIfExists();
    }

    private String extractBlobName(String blobUrl) {
        if (blobUrl == null) return null;
        int lastSlash = blobUrl.lastIndexOf('/');
        return lastSlash >= 0 ? blobUrl.substring(lastSlash + 1) : blobUrl;
    }
}
