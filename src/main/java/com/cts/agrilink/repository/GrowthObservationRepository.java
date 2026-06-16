package com.cts.agrilink.repository;

import com.cts.agrilink.model.CropPlan;
import com.cts.agrilink.model.GrowthObservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GrowthObservationRepository extends JpaRepository<GrowthObservation, Integer> {
    List<GrowthObservation> findByCropPlan_PlanId(Integer planId);

    @Query("SELECT g FROM GrowthObservation g WHERE g.pestOrDiseaseFlag = :pestOrDiseaseFlag")
    List<GrowthObservation> findByPestOrDiseaseFlag(@Param("pestOrDiseaseFlag") Boolean pestOrDiseaseFlag);

    List<GrowthObservation> findByOfficerId(Integer officerId);
    List<GrowthObservation> findByGrowthStage(GrowthObservation.GrowthStage growthStage);
    boolean existsByCropPlan_PlanId(Integer planId);

    @Query("SELECT g FROM GrowthObservation g JOIN g.cropPlan cp " +
           "WHERE g.pestOrDiseaseFlag = true " +
           "AND (:officerId IS NULL OR g.officerId = :officerId) " +
           "AND (:planSeason IS NULL OR cp.planSeason = :planSeason) " +
           "AND (:planYear IS NULL OR cp.planYear = :planYear)")
    List<GrowthObservation> findPestAlerts(
            @Param("officerId") Integer officerId,
            @Param("planSeason") CropPlan.PlanSeason planSeason,
            @Param("planYear") Integer planYear);
}
