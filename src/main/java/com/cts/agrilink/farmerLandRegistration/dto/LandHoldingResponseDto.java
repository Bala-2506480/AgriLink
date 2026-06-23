package com.cts.agrilink.farmerLandRegistration.dto;

import com.cts.agrilink.farmerLandRegistration.model.LandHolding;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LandHoldingResponseDto {

    private Long holdingId;
    private Long farmerId;
    private String surveyNumber;
    private BigDecimal areaAcres;
    private LandHolding.SoilType soilType;
    private LandHolding.IrrigationSource irrigationSource;
    private LandHolding.OwnershipType ownershipType;
    private LandHolding.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LandHoldingResponseDto from(LandHolding lh) {
        return LandHoldingResponseDto.builder()
                .holdingId(lh.getHoldingId())
                .farmerId(lh.getFarmer().getFarmerId())
                .surveyNumber(lh.getSurveyNumber())
                .areaAcres(lh.getAreaAcres())
                .soilType(lh.getSoilType())
                .irrigationSource(lh.getIrrigationSource())
                .ownershipType(lh.getOwnershipType())
                .status(lh.getStatus())
                .createdAt(lh.getCreatedAt())
                .updatedAt(lh.getUpdatedAt())
                .build();
    }
}
