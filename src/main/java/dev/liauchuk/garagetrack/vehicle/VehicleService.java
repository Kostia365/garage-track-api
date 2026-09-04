package dev.liauchuk.garagetrack.vehicle;


import dev.liauchuk.garagetrack.vehicle.dto.CreateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.UpdateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.VehicleResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {
  private final VehicleRepository vehicleRepository;
  private final VehicleMapper vehicleMapper;

  public VehicleService(VehicleRepository vehicleRepository, VehicleMapper vehicleMapper) {
    this.vehicleRepository = vehicleRepository;
    this.vehicleMapper = vehicleMapper;
  }

  @Transactional
  public VehicleResponseDto create(CreateVehicleRequestDto dto) {
    if (dto.vin() != null && vehicleRepository.existsByVin(dto.vin())) {
      throw new IllegalStateException("vehicle with this vin already exist");
    }
    Vehicle vehicle = vehicleMapper.toEntity(dto);
    Vehicle savedVehicle = vehicleRepository.save(vehicle);
    return vehicleMapper.toResponseDto(savedVehicle);
  }

  public void update(UpdateVehicleRequestDto dto, Vehicle vehicle) {
    vehicleMapper.updateEntity(dto, vehicle);
    vehicleRepository.save(vehicle);
  }
}
