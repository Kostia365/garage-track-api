package dev.liauchuk.garagetrack.vehicle;


import dev.liauchuk.garagetrack.vehicle.dto.CreateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.UpdateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.VehicleResponseDto;
import dev.liauchuk.garagetrack.vehicle.exception.VehicleMileageCannotBeDecreasedException;
import dev.liauchuk.garagetrack.vehicle.exception.VehicleNotFoundException;
import dev.liauchuk.garagetrack.vehicle.exception.VehicleVinAlreadyExistsException;
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
      throw new VehicleVinAlreadyExistsException(dto.vin());
    }
    Vehicle vehicle = vehicleMapper.toEntity(dto);
    Vehicle savedVehicle = vehicleRepository.save(vehicle);
    return vehicleMapper.toResponseDto(savedVehicle);
  }

  @Transactional(readOnly = true)
  public VehicleResponseDto getById(long id) {
    Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() ->
        new VehicleNotFoundException(id));
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
    Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() -> new VehicleNotFoundException(id));
    vehicle.setActive(false);
    vehicleRepository.save(vehicle);
  }

  @Transactional(readOnly = true)
  public List<VehicleResponseDto> findAllActive() {
    return vehicleRepository.findAllByActiveTrue().stream()
        .map(vehicleMapper::toResponseDto)
        .toList();
  }

  @Transactional
  public VehicleResponseDto update(long id, UpdateVehicleRequestDto dto) {
    Vehicle vehicle = vehicleRepository.findById(id)
        .orElseThrow(() -> new VehicleNotFoundException(id));

    if (dto.vin() != null && vehicleRepository.existsByVinAndIdNot(dto.vin(), id)) {
      throw new VehicleVinAlreadyExistsException(dto.vin());
    }

    if (dto.currentMileageKm() != null && vehicle.getCurrentMileageKm() > dto.currentMileageKm()) {
      throw new VehicleMileageCannotBeDecreasedException(
          vehicle.getCurrentMileageKm(),
          dto.currentMileageKm()
      );
    }

    vehicleMapper.updateEntity(dto, vehicle);
    Vehicle savedVehicle = vehicleRepository.save(vehicle);
    return vehicleMapper.toResponseDto(savedVehicle);
  }

}
