package com.cts.agrilink.repository;

import com.cts.agrilink.model.CropPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CropPlanRepository extends JpaRepository<CropPlan, Integer> {
    List<CropPlan> findByFarmerId(Long farmerId);
    List<CropPlan> findByPlanStatus(CropPlan.PlanStatus planStatus);
    List<CropPlan> findByPlanSeasonAndPlanYear(CropPlan.PlanSeason planSeason, Integer planYear);
    boolean existsByFarmerIdAndHoldingIdAndCropCatalog_CropIdAndPlanSeasonAndPlanYear(
            Long farmerId, Long holdingId, Integer cropId,
            CropPlan.PlanSeason planSeason, Integer planYear);
    boolean existsByCropCatalog_CropIdAndPlanStatusNotIn(
            Integer cropId, List<CropPlan.PlanStatus> excludedStatuses);
}
