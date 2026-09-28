package dev.liauchuk.garagetrack.fuelentry;

import dev.liauchuk.garagetrack.fuelentry.dto.CreateFuelEntryRequestDto;
import dev.liauchuk.garagetrack.fuelentry.dto.FuelEntryResponseDto;
import dev.liauchuk.garagetrack.fuelentry.dto.UpdateFuelEntryRequestDto;
import org.springframework.stereotype.Component;

@Component
public class FuelEntryMapper {
  public FuelEntry toEntity(CreateFuelEntryRequestDto dto) {
    FuelEntry fuelEntry = new FuelEntry();
    fuelEntry.setVehicleId(dto.vehicleId());
    fuelEntry.setFilledAt(dto.filledAt());
    fuelEntry.setOdometer(dto.odometerKm());
    fuelEntry.setPricePerLiter(dto.pricePerLiter());
    fuelEntry.setFullTank(Boolean.TRUE.equals(dto.fullTank()));
    fuelEntry.setFuelStation(dto.fuelStation());
    fuelEntry.setNotes(dto.notes());
    fuelEntry.setAmountLiters(dto.liters());
    fuelEntry.setTotalPrice(dto.pricePerLiter().multiply(dto.liters()));
    return fuelEntry;
  }

  public FuelEntryResponseDto toResponseDto(FuelEntry fuelEntry) {
    return new FuelEntryResponseDto(
        fuelEntry.getId(),
        fuelEntry.getFilledAt(),
        fuelEntry.getAmountLiters(),
        fuelEntry.getOdometer(),
        fuelEntry.getPricePerLiter(),
        fuelEntry.isFullTank(),
        fuelEntry.getFuelStation(),
        fuelEntry.getTotalPrice(),
        fuelEntry.getNotes(),
        fuelEntry.getCreatedAt(),
        fuelEntry.getUpdatedAt()
    );
  }

  public void updateEntity(UpdateFuelEntryRequestDto dto, FuelEntry fuelEntry) {
    if (dto.odometer() != null) {
      fuelEntry.setOdometer(dto.odometer());
    }
    if (dto.notes() != null) {
      fuelEntry.setNotes(dto.notes());
    }
    if (dto.amountLiters() != null) {
      fuelEntry.setAmountLiters(dto.amountLiters());
    }
    if (dto.pricePerLiter() != null) {
      fuelEntry.setPricePerLiter(dto.pricePerLiter());
    }
    if (dto.amountLiters() != null || dto.pricePerLiter() != null) {
      fuelEntry.setTotalPrice(
          fuelEntry.getPricePerLiter().multiply(fuelEntry.getAmountLiters())
      );
    }
    if (dto.fuelStation() != null) {
      fuelEntry.setFuelStation(dto.fuelStation());
    }
  }
}
