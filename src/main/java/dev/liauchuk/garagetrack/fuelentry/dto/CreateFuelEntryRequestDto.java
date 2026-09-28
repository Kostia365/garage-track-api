package dev.liauchuk.garagetrack.fuelentry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Data for creating a fuel entry")
public record CreateFuelEntryRequestDto(
    @NotNull
    @Positive
    Long vehicleId,
    Instant filledAt,
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 5, fraction = 3)
    BigDecimal liters,
    @NotNull
    @PositiveOrZero
    Integer odometerKm,
    @NotNull
    @PositiveOrZero
    @Digits(integer = 5, fraction = 3)
    BigDecimal pricePerLiter,
    Boolean fullTank,
    @Size(max = 100)
    String fuelStation,
    String notes
) {
}
