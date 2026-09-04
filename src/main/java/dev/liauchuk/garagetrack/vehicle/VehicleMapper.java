package dev.liauchuk.garagetrack.vehicle;

import dev.liauchuk.garagetrack.vehicle.dto.CreateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.UpdateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.VehicleResponseDto;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {
  public Vehicle toEntity(CreateVehicleRequestDto dto) {
    Vehicle vehicle = new Vehicle();
    vehicle.setMake(dto.make());
    vehicle.setModel(dto.model());
    vehicle.setProductionYear(dto.productionYear());
    vehicle.setVin(dto.vin());
    vehicle.setLicensePlate(dto.licensePlate());
    vehicle.setFuelType(dto.fuelType());
    vehicle.setCurrentMileageKm(dto.currentMileageKm());
    vehicle.setPurchaseDate(dto.purchaseDate());
    vehicle.setPurchasePrice(dto.purchasePrice());

    return vehicle;
  }

  public VehicleResponseDto toResponseDto(Vehicle vehicle) {
    return new VehicleResponseDto(
        vehicle.getId(),
        vehicle.getMake(),
        vehicle.getModel(),
        vehicle.getProductionYear(),
        vehicle.getVin(),
        vehicle.getLicensePlate(),
        vehicle.getFuelType(),
        vehicle.getCurrentMileageKm(),
        vehicle.getPurchaseDate(),
        vehicle.getPurchasePrice(),
        vehicle.isActive(),
        vehicle.getCreatedAt(),
        vehicle.getUpdatedAt()
    );
  }

  public void updateEntity(UpdateVehicleRequestDto dto, Vehicle vehicle) {
    vehicle.setMake(dto.make());
    vehicle.setModel(dto.model());
    vehicle.setProductionYear(dto.productionYear());
    vehicle.setVin(dto.vin());
    vehicle.setLicensePlate(dto.licensePlate());
    vehicle.setFuelType(dto.fuelType());
    vehicle.setCurrentMileageKm(dto.currentMileageKm());
    vehicle.setPurchaseDate(dto.purchaseDate());
    vehicle.setPurchasePrice(dto.purchasePrice());
  }
}
