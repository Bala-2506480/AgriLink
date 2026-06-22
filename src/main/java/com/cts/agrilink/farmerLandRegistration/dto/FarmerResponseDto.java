package com.cts.agrilink.farmerLandRegistration.dto;

import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class FarmerResponseDto {

    private Long farmerId;
    private Integer userId;
    private String name;
    private LocalDate dateOfBirth;
    private FarmerProfile.Gender gender;
    private String nationalIdNumber;
    private String village;
    private String district;
    private String state;
    private String phone;
    private String bankAccountNumber;
    private FarmerProfile.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FarmerResponseDto from(FarmerProfile fp) {
        return FarmerResponseDto.builder()
                .farmerId(fp.getFarmerId())
                .userId(fp.getUserId())
                .name(fp.getName())
                .dateOfBirth(fp.getDateOfBirth())
                .gender(fp.getGender())
                .nationalIdNumber(fp.getNationalIdNumber())
                .village(fp.getVillage())
                .district(fp.getDistrict())
                .state(fp.getState())
                .phone(fp.getPhone())
                .bankAccountNumber(fp.getBankAccountNumber())
                .status(fp.getStatus())
                .createdAt(fp.getCreatedAt())
                .updatedAt(fp.getUpdatedAt())
                .build();
    }
}
