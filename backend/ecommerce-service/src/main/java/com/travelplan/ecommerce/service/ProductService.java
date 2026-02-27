package com.travelplan.ecommerce.service;

import com.travelplan.ecommerce.entity.Product;
import com.travelplan.ecommerce.enums.ProductCategory;
import com.travelplan.ecommerce.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder!", e);
        }
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String imageUrl = "http://localhost:8091/api/products/images/" + fileName;
            
            // Map the single uploaded image to the new List format
            if (product.getImages() == null) {
                product.setImages(new ArrayList<>());
            }
            product.getImages().add(imageUrl);
        }
        return productRepository.save(product);
    }

    public List<Product> searchProducts(String categoryStr, BigDecimal minPrice, BigDecimal maxPrice, String eventId) {
        if (categoryStr == null && minPrice == null && maxPrice == null && eventId == null) {
            return productRepository.findAll();
        }

        ProductCategory category = null;
        if (categoryStr != null && !categoryStr.isEmpty()) {
            try {
                category = ProductCategory.valueOf(categoryStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid
            }
        }
        return productRepository.searchProducts(category, minPrice, maxPrice, eventId);
    }
}