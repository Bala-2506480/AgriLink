package com.cts.agrilink.inputAndProcurementMangement.repository;

import com.cts.agrilink.inputAndProcurementMangement.model.AgriInput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AgriInputRepository extends JpaRepository<AgriInput, Long> {
    List<AgriInput> findByCategoryIgnoreCase(String category);
    List<AgriInput> findByStatusIgnoreCase(String status);
}