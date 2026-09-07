package dev.liauchuk.garagetrack.vehicle;


import dev.liauchuk.garagetrack.vehicle.dto.CreateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.UpdateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.VehicleResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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



  @Transactional(readOnly = true)
  public VehicleResponseDto getById(long id) {
    Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() ->
        new IllegalStateException("vehicle with id " + id + " not found"));
    return vehicleMapper.toResponseDto(vehicle);
  }

  @Transactional(readOnly = true)
  public List<VehicleResponseDto> getAll() {
    return vehicleRepository.findAll().stream()
        .map(vehicleMapper::toResponseDto)
        .toList();
  }

  @Transactional
  public void softDelete(long id) {
    Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() -> new IllegalStateException("vehicle with id " + id + " not found"));
    vehicle.setActive(false);
    vehicleRepository.save(vehicle);
  }

  @Transactional(readOnly = true)
  public List<VehicleResponseDto> findAllActive() {
    return vehicleRepository.findAllByActiveTrue().stream()
        .map(vehicleMapper::toResponseDto)
        .toList();
  }
}
