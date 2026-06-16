package com.cts.agrilink.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Maps the GrowthObservation.GrowthStage enum to/from the exact ENUM values
 * stored in the growth_observation.growthStage column. Two DB values contain
 * characters that are not valid in Java identifiers ('Grain filling' has a
 * space, 'Harvest-ready' has a hyphen), so they are translated explicitly.
 */
@Converter
public class GrowthStageConverter
        implements AttributeConverter<GrowthObservation.GrowthStage, String> {

    @Override
    public String convertToDatabaseColumn(GrowthObservation.GrowthStage stage) {
        if (stage == null) return null;
        return switch (stage) {
            case Grain_filling -> "Grain filling";
            case Harvest_ready -> "Harvest-ready";
            default            -> stage.name();
        };
    }

    @Override
    public GrowthObservation.GrowthStage convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        return switch (dbValue) {
            case "Grain filling" -> GrowthObservation.GrowthStage.Grain_filling;
            case "Harvest-ready" -> GrowthObservation.GrowthStage.Harvest_ready;
            default              -> GrowthObservation.GrowthStage.valueOf(dbValue);
        };
    }
}
