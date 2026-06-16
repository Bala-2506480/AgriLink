package com.cts.agrilink.controller;

import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.model.CropCatalog;
import com.cts.agrilink.service.CropCatalogService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agriLink/cropSeason")
public class CropCatalogController {

    private static final Logger log = LoggerFactory.getLogger(CropCatalogController.class);

    private final CropCatalogService cropCatalogService;

    public CropCatalogController(CropCatalogService cropCatalogService) {
        this.cropCatalogService = cropCatalogService;
    }

    @PostMapping("/createCatalog")
    public ResponseEntity<Map<String, String>> createCatalog(
            @Valid @RequestBody CropCatalogCreateRequest request) {
        log.info("POST /createCatalog - cropName={}, season={}", request.getCropName(), request.getCropSeason());
        cropCatalogService.createCatalog(request);
        log.info("Crop created successfully: {}", request.getCropName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Crop created successfully"));
    }

    @GetMapping("/fetchCatalogs")
    public ResponseEntity<List<CropCatalogResponse>> fetchCatalogs(
            @RequestParam(required = false) CropCatalog.CatalogStatus catalogStatus) {
        log.info("GET /fetchCatalogs - status={}", catalogStatus);
        return ResponseEntity.ok(cropCatalogService.fetchCatalogs(catalogStatus));
    }

    @GetMapping("/fetchCatalogById/{cropId}")
    public ResponseEntity<CropCatalogResponse> fetchCatalogById(
            @PathVariable Integer cropId) {
        log.info("GET /fetchCatalogById/{}", cropId);
        return ResponseEntity.ok(cropCatalogService.fetchCatalogById(cropId));
    }

    @GetMapping("/fetchBySeason/{cropSeason}")
    public ResponseEntity<List<CropCatalogResponse>> fetchBySeason(
            @PathVariable CropCatalog.CropSeason cropSeason) {
        log.info("GET /fetchBySeason/{}", cropSeason);
        return ResponseEntity.ok(cropCatalogService.fetchBySeason(cropSeason));
    }

    @GetMapping("/fetchByCategory/{cropCategory}")
    public ResponseEntity<List<CropCatalogResponse>> fetchByCategory(
            @PathVariable CropCatalog.CropCategory cropCategory) {
        log.info("GET /fetchByCategory/{}", cropCategory);
        return ResponseEntity.ok(cropCatalogService.fetchByCategory(cropCategory));
    }

    @PutMapping("/updateCatalog/{cropId}")
    public ResponseEntity<Map<String, String>> updateCatalog(
            @PathVariable Integer cropId,
            @Valid @RequestBody CropCatalogUpdateRequest request) {
        log.info("PUT /updateCatalog/{}", cropId);
        cropCatalogService.updateCatalog(cropId, request);
        log.info("Crop updated successfully: cropId={}", cropId);
        return ResponseEntity.ok(Map.of("message", "Crop catalog updated successfully"));
    }

    @DeleteMapping("/deleteCatalog/{cropId}")
    public ResponseEntity<Map<String, String>> deleteCatalog(
            @PathVariable Integer cropId) {
        log.info("DELETE /deleteCatalog/{}", cropId);
        cropCatalogService.deleteCatalog(cropId);
        log.info("Crop deleted successfully: cropId={}", cropId);
        return ResponseEntity.ok(Map.of("message", "Crop deleted successfully"));
    }
}
