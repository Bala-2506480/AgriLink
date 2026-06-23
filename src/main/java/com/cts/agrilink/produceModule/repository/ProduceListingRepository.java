package com.cts.agrilink.produceModule.repository;

import com.cts.agrilink.produceModule.entity.ProduceListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProduceListingRepository extends JpaRepository<ProduceListing, Long> {

    List<ProduceListing> findByFarmerId(Long farmerId);

    List<ProduceListing> findByCropId(Long cropId);

    List<ProduceListing> findByStatus(ProduceListing.ListingStatus status);

    List<ProduceListing> findByQualityGrade(ProduceListing.QualityGrade qualityGrade);
}
