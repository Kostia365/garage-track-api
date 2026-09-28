package dev.liauchuk.garagetrack.fuelentry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record UpdateFuelEntryRequestDto(
    @Schema(description = "Odometer reading")
    Integer odometer,
    @Schema(description = "Notes")
    String notes,
    @Schema(description = "Amount of fuel in liters")
    BigDecimal amountLiters,
    @Schema(description = "Price per liter")
    BigDecimal pricePerLiter,
    @Schema()
    String fuelStation
) {

}
