package dev.liauchuk.garagetrack.fuelentry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Response for fuel entry")
public record FuelEntryResponseDto(
    @NotNull
    @Schema(description = "Fuel entry identifier", example = "1")
    Long id,
    @Schema(description = "Date and time when the fuel entry was filled", example = "2026-08-12T10:30:00Z")
    Instant filledAt,
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 5, fraction = 3)
    @Schema(description = "Amount of fuel in liters", example = "10.5")
    BigDecimal liters,
    @NotNull
    @PositiveOrZero
    @Schema(description = "Odometer reading in kilometres", example = "12345")
    Integer odometerKm,
    @NotNull
    @PositiveOrZero
    @Digits(integer = 5, fraction = 3)
    @Schema(description = "Price per liter", example = "10.5")
    BigDecimal pricePerLiter,
    @Schema(description = "Whether the tank is full")
    Boolean fullTank,
    @Size(max = 100)
    @Schema(description = "Name of the fuel station", example = "Gas Station")
    String fuelStation,
    @Schema(description = "Total price of the fuel entry")
    @NotNull
    BigDecimal totalPrice,
    @Schema(description = "Notes about the fuel entry")
    @NotNull
    String notes,
    @NotNull
    @Schema(description = "Date and time when the fuel entry was created", example = "2026-08-12T10:30:00Z")
    Instant createdAt,
    @NotNull
    @Schema(description = "Date and time when the fuel entry was last updated", example = "2026-08-12T12:45:00Z")
    Instant updatedAt
) {

}
