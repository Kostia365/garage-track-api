package dev.liauchuk.garagetrack.fuelentry;

import dev.liauchuk.garagetrack.fuelentry.dto.CreateFuelEntryRequestDto;
import dev.liauchuk.garagetrack.fuelentry.dto.FuelEntryResponseDto;
import dev.liauchuk.garagetrack.fuelentry.dto.UpdateFuelEntryRequestDto;
import dev.liauchuk.garagetrack.fuelentry.exception.FuelEntryNotFoundException;
import dev.liauchuk.garagetrack.vehicle.VehicleRepository;
import dev.liauchuk.garagetrack.vehicle.exception.VehicleNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FuelEntryService {
  private final FuelEntryRepository fuelEntryRepository;
  private final FuelEntryMapper fuelEntryMapper;
  private final VehicleRepository vehicleRepository;

  public FuelEntryService(FuelEntryRepository fuelEntryRepository, FuelEntryMapper fuelEntryMapper, VehicleRepository vehicleRepository) {
    this.fuelEntryRepository = fuelEntryRepository;
    this.fuelEntryMapper = fuelEntryMapper;
    this.vehicleRepository = vehicleRepository;
  }

  @Transactional
  public FuelEntryResponseDto create(CreateFuelEntryRequestDto dto) {
    if (dto.vehicleId() == null) {
      throw new IllegalArgumentException("Gas ID is required");
    }
    if (!vehicleRepository.existsByIdAndActiveTrue(dto.vehicleId())) {
      throw new VehicleNotFoundException(dto.vehicleId());
    }
    FuelEntry fuelEntry = fuelEntryMapper.toEntity(dto);
    FuelEntry savedFuelEntry = fuelEntryRepository.save(fuelEntry);
    return fuelEntryMapper.toResponseDto(savedFuelEntry);
  }

  @Transactional
  public void delete(long id) {
    fuelEntryRepository.deleteById(id);
  }

  @Transactional
  public FuelEntryResponseDto edit(long id, UpdateFuelEntryRequestDto dto) {
    FuelEntry fuelEntry = fuelEntryRepository.findById(id).orElseThrow(() -> new FuelEntryNotFoundException(id));
    fuelEntryMapper.updateEntity(dto, fuelEntry);
    return fuelEntryMapper.toResponseDto(fuelEntryRepository.save(fuelEntry));
  }
}
