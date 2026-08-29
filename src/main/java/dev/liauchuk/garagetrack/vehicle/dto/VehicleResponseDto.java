package dev.liauchuk.garagetrack.vehicle.dto;

import dev.liauchuk.garagetrack.vehicle.FuelType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record VehicleResponseDto(
    @Schema(description = "Vehicle identifier", example = "1")
    Long id,
    @NotBlank
    @Size(max = 100)
    @Schema(description = "Car manufacturer", example = "Chevrolet")
    String make,
    @NotBlank
    @Size(max = 100)
    @Schema(description = "Car model", example = "Corvette")
    String model,
    @NotNull
    @Min(1886)
    @Schema(description = "Production year", example = "2017")
    Integer productionYear,
    @Schema(
        description = "Vehicle identification number",
        example = "JTDBR32E720123456"
    )
    String vin,
    @Size(max = 20)
    @Schema(description = "License plate", example = "NR123AB")
    String licensePlate,
    @NotNull
    @Schema(description = "Fuel type", example = "DIESEL")
    FuelType fuelType,
    @NotNull
    @PositiveOrZero
    @Schema(description = "Current mileage in kilometres", example = "75000")
    Integer currentMileageKm,
    @Schema(description = "Purchase date", example = "2026-08-12")
    LocalDate purchaseDate,
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Schema(description = "Purchase price", example = "44999.90")
    BigDecimal purchasePrice,
    @Schema(description = "status")
    boolean active,
    @Schema(
        description = "Date and time when the vehicle was created",
        example = "2026-08-12T10:30:00Z"
    )
    Instant createdAt,
    @Schema(
        description = "Date and time when the vehicle was last updated",
        example = "2026-08-12T12:45:00Z"
    )
    Instant updatedAt

) {
}
