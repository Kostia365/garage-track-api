package dev.liauchuk.garagetrack.vehicle.dto;

import dev.liauchuk.garagetrack.vehicle.FuelType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Data for updating a car")
public record UpdateVehicleRequestDto(
    @Size(max = 100)
    @Schema(description = "Car manufacturer", example = "Chevrolet")
    @Pattern(
        regexp = ".*\\S.*",
        message = "must not be blank"
    )
    String make,
    @Size(max = 100)
    @Schema(description = "Car model", example = "Corvette")
    @Pattern(
        regexp = ".*\\S.*",
        message = "must not be blank"
    )
    String model,
    @Min(1886)
    @Schema(description = "Production year", example = "2017")
    Integer productionYear,
    @Size(min = 17, max = 17)
    @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$")
    @Schema(
        description = "Vehicle identification number",
        example = "JTDBR32E720123456"
    )
    String vin,
    @Size(max = 20)
    @Schema(description = "License plate", example = "NR123AB")
    String licensePlate,
    @Schema(description = "Fuel type", example = "DIESEL")
    FuelType fuelType,
    @PositiveOrZero
    @Schema(description = "Current mileage in kilometres", example = "75000")
    Integer currentMileageKm,
    @Schema(description = "Purchase date", example = "2026-08-12")
    LocalDate purchaseDate,
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Schema(description = "Purchase price", example = "44999.90")
    BigDecimal purchasePrice
) {

}
