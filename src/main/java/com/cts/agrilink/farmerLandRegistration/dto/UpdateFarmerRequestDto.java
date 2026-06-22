package com.cts.agrilink.farmerLandRegistration.dto;

import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateFarmerRequestDto {

    @Size(max = 100)
    private String village;

    @Size(max = 100)
    private String district;

    @Size(max = 15)
    private String phone;

    @Size(max = 30)
    private String bankAccountNumber;

    private FarmerProfile.Status status;
}
