package com.cts.agrilink.controller;

import com.cts.agrilink.dto.AgrilinkDto.*;
import com.cts.agrilink.model.CropCatalog;
import com.cts.agrilink.service.CropCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agriLink/cropSeason")
public class CropCatalogController {

    private final CropCatalogService cropCatalogService;

    public CropCatalogController(CropCatalogService cropCatalogService) {
        this.cropCatalogService = cropCatalogService;
    }

    @PostMapping("/createCatalog")
    public ResponseEntity<Map<String, String>> createCatalog(
            @Valid @RequestBody CropCatalogCreateRequest request) {
        cropCatalogService.createCatalog(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Crop created successfully"));
    }

    @GetMapping("/fetchCatalogs")
    public ResponseEntity<List<CropCatalogResponse>> fetchCatalogs(
            @RequestParam(required = false) CropCatalog.CatalogStatus catalogStatus) {
        return ResponseEntity.ok(cropCatalogService.fetchCatalogs(catalogStatus));
    }

    @GetMapping("/fetchCatalogById/{cropId}")
    public ResponseEntity<CropCatalogResponse> fetchCatalogById(
            @PathVariable Integer cropId) {
        return ResponseEntity.ok(cropCatalogService.fetchCatalogById(cropId));
    }

    @GetMapping("/fetchBySeason/{cropSeason}")
    public ResponseEntity<List<CropCatalogResponse>> fetchBySeason(
            @PathVariable CropCatalog.CropSeason cropSeason) {
        return ResponseEntity.ok(cropCatalogService.fetchBySeason(cropSeason));
    }

    @GetMapping("/fetchByCategory/{cropCategory}")
    public ResponseEntity<List<CropCatalogResponse>> fetchByCategory(
            @PathVariable CropCatalog.CropCategory cropCategory) {
        return ResponseEntity.ok(cropCatalogService.fetchByCategory(cropCategory));
    }

    @PutMapping("/updateCatalog/{cropId}")
    public ResponseEntity<Map<String, String>> updateCatalog(
            @PathVariable Integer cropId,
            @Valid @RequestBody CropCatalogUpdateRequest request) {
        cropCatalogService.updateCatalog(cropId, request);
        return ResponseEntity.ok(Map.of("message", "Crop catalog updated successfully"));
    }

    @DeleteMapping("/deleteCatalog/{cropId}")
    public ResponseEntity<Map<String, String>> deleteCatalog(
            @PathVariable Integer cropId) {
        cropCatalogService.deleteCatalog(cropId);
        return ResponseEntity.ok(Map.of("message", "Crop deleted successfully"));
    }
}
