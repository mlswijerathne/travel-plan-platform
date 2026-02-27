package com.travelplan.tripplan.controller;

import com.travelplan.common.dto.PaginatedResponse;
import com.travelplan.tripplan.dto.PackageRequest;
import com.travelplan.tripplan.dto.PackageResponse;
import com.travelplan.tripplan.service.PackageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/packages")
public class PackageController {

    private final PackageService packageService;

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    // A simple wrapper to match exactly the required {"data": ...} spec
    private <T> Map<String, T> wrapData(T data) {
        return Collections.singletonMap("data", data);
    }

    @PostMapping
    public ResponseEntity<Map<String, PackageResponse>> createPackage(
            @Valid @RequestBody PackageRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "admin-uuid") String userId) {
        PackageResponse response = packageService.createPackage(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrapData(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, PackageResponse>> getPackageById(@PathVariable Long id) {
        return ResponseEntity.ok(wrapData(packageService.getPackageById(id)));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<PackageResponse>> searchPackages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PackageResponse> packagePage = packageService.searchPackages(PageRequest.of(page, size));
        PaginatedResponse<PackageResponse> response = PaginatedResponse.of(
                packagePage.getContent(),
                page,
                size,
                packagePage.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/featured")
    public ResponseEntity<Map<String, List<PackageResponse>>> getFeaturedPackages() {
        return ResponseEntity.ok(wrapData(packageService.getFeaturedPackages()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, PackageResponse>> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody PackageRequest request) {
        return ResponseEntity.ok(wrapData(packageService.updatePackage(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        packageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }
}
