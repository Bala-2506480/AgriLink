package com.cts.agrilink.repository;

import com.cts.agrilink.model.CropCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CropCatalogRepository extends JpaRepository<CropCatalog, Integer> {
    List<CropCatalog> findByCropSeason(CropCatalog.CropSeason cropSeason);
    List<CropCatalog> findByCropCategory(CropCatalog.CropCategory cropCategory);
    List<CropCatalog> findByCatalogStatus(CropCatalog.CatalogStatus catalogStatus);
    boolean existsByCropNameAndCropSeason(String cropName, CropCatalog.CropSeason cropSeason);
}