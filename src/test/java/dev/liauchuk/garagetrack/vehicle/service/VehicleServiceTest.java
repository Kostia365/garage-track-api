package dev.liauchuk.garagetrack.vehicle.service;

import dev.liauchuk.garagetrack.vehicle.FuelType;
import dev.liauchuk.garagetrack.vehicle.Vehicle;
import dev.liauchuk.garagetrack.vehicle.VehicleMapper;
import dev.liauchuk.garagetrack.vehicle.VehicleRepository;
import dev.liauchuk.garagetrack.vehicle.VehicleService;
import dev.liauchuk.garagetrack.vehicle.dto.CreateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.UpdateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.VehicleResponseDto;
import dev.liauchuk.garagetrack.vehicle.exception.VehicleMileageCannotBeDecreasedException;
import dev.liauchuk.garagetrack.vehicle.exception.VehicleNotFoundException;
import dev.liauchuk.garagetrack.vehicle.exception.VehicleVinAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static dev.liauchuk.garagetrack.vehicle.VehicleTestFactory.vehicle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

  private static final Logger log =
      LoggerFactory.getLogger(VehicleServiceTest.class);

  private static final long VEHICLE_ID = 2L;
  private static final long MISSING_ID = 999L;
  private static final String VIN = "1G1YB2D70H5100001";
  private static final String NEW_VIN = "1G1YB2D70H5100002";

  @Mock
  private VehicleRepository vehicleRepository;

  @Mock
  private VehicleMapper vehicleMapper;

  @InjectMocks
  private VehicleService vehicleService;

  @Test
  void createWhenVinIsUniqueSavesVehicleAndReturnsMappedResponse() {
    CreateVehicleRequestDto request = createRequest(VIN);
    Vehicle entity = existingVehicle();
    VehicleResponseDto expectedResponse = responseDto(VEHICLE_ID, VIN, true);

    when(vehicleRepository.existsByVin(VIN)).thenReturn(false);
    when(vehicleMapper.toEntity(request)).thenReturn(entity);
    when(vehicleRepository.save(entity)).thenReturn(entity);
    when(vehicleMapper.toResponseDto(entity)).thenReturn(expectedResponse);

    log.info("Creating vehicle with unique VIN: {}", VIN);

    VehicleResponseDto actualResponse = vehicleService.create(request);

    assertSame(expectedResponse, actualResponse);
    verify(vehicleRepository).existsByVin(VIN);
    verify(vehicleMapper).toEntity(request);
    verify(vehicleRepository).save(entity);
    verify(vehicleMapper).toResponseDto(entity);
  }

  @Test
  void createWhenVinAlreadyExistsThrowsConflictWithoutMappingOrSaving() {
    CreateVehicleRequestDto request = createRequest(VIN);
    when(vehicleRepository.existsByVin(VIN)).thenReturn(true);

    VehicleVinAlreadyExistsException exception = assertThrows(
        VehicleVinAlreadyExistsException.class,
        () -> vehicleService.create(request)
    );

    assertEquals(
        "Vehicle with VIN " + VIN + " already exists",
        exception.getMessage()
    );
    verify(vehicleRepository).existsByVin(VIN);
    verify(vehicleRepository, never()).save(any(Vehicle.class));
    verifyNoInteractions(vehicleMapper);
  }

  @Test
  void createWhenVinIsNullSkipsUniquenessCheck() {
    CreateVehicleRequestDto request = createRequest(null);
    Vehicle entity = vehicle(null, 66000);
    VehicleResponseDto expectedResponse = responseDto(VEHICLE_ID, null, true);

    when(vehicleMapper.toEntity(request)).thenReturn(entity);
    when(vehicleRepository.save(entity)).thenReturn(entity);
    when(vehicleMapper.toResponseDto(entity)).thenReturn(expectedResponse);

    VehicleResponseDto actualResponse = vehicleService.create(request);

    assertSame(expectedResponse, actualResponse);
    verify(vehicleRepository, never()).existsByVin(any());
    verify(vehicleRepository).save(entity);
  }

  @Test
  void getByIdWhenVehicleExistsReturnsMappedResponse() {
    Vehicle entity = existingVehicle();
    VehicleResponseDto expectedResponse = responseDto(VEHICLE_ID, VIN, true);

    when(vehicleRepository.findById(VEHICLE_ID))
        .thenReturn(Optional.of(entity));
    when(vehicleMapper.toResponseDto(entity))
        .thenReturn(expectedResponse);

    VehicleResponseDto actualResponse = vehicleService.getById(VEHICLE_ID);

    assertSame(expectedResponse, actualResponse);
    verify(vehicleRepository).findById(VEHICLE_ID);
    verify(vehicleMapper).toResponseDto(entity);
  }

  @Test
  void getByIdWhenVehicleDoesNotExistThrowsNotFound() {
    when(vehicleRepository.findById(MISSING_ID))
        .thenReturn(Optional.empty());

    VehicleNotFoundException exception = assertThrows(
        VehicleNotFoundException.class,
        () -> vehicleService.getById(MISSING_ID)
    );

    assertEquals(
        "Vehicle with id 999 not found",
        exception.getMessage()
    );
    verify(vehicleRepository).findById(MISSING_ID);
    verifyNoInteractions(vehicleMapper);
  }

  @Test
  void getAllReturnsMappedActiveAndArchivedVehicles() {
    Vehicle activeVehicle = existingVehicle();
    Vehicle archivedVehicle = vehicle(NEW_VIN, 40000);
    archivedVehicle.setId(3L);
    archivedVehicle.setActive(false);

    VehicleResponseDto activeResponse = responseDto(VEHICLE_ID, VIN, true);
    VehicleResponseDto archivedResponse = responseDto(3L, NEW_VIN, false);

    when(vehicleRepository.findAll())
        .thenReturn(List.of(activeVehicle, archivedVehicle));
    when(vehicleMapper.toResponseDto(activeVehicle))
        .thenReturn(activeResponse);
    when(vehicleMapper.toResponseDto(archivedVehicle))
        .thenReturn(archivedResponse);

    List<VehicleResponseDto> result = vehicleService.getAll();

    assertEquals(List.of(activeResponse, archivedResponse), result);
    verify(vehicleRepository).findAll();
    verify(vehicleRepository, never()).findAllByActiveTrue();
  }

  @Test
  void getAllWhenRepositoryIsEmptyReturnsEmptyList() {
    when(vehicleRepository.findAll()).thenReturn(List.of());

    List<VehicleResponseDto> result = vehicleService.getAll();

    assertTrue(result.isEmpty());
    verify(vehicleRepository).findAll();
    verifyNoInteractions(vehicleMapper);
  }

  @Test
  void findAllActiveReturnsMappedVehiclesFromActiveQuery() {
    Vehicle entity = existingVehicle();
    VehicleResponseDto expectedResponse = responseDto(VEHICLE_ID, VIN, true);

    when(vehicleRepository.findAllByActiveTrue())
        .thenReturn(List.of(entity));
    when(vehicleMapper.toResponseDto(entity))
        .thenReturn(expectedResponse);

    List<VehicleResponseDto> result = vehicleService.findAllActive();

    assertEquals(List.of(expectedResponse), result);
    verify(vehicleRepository).findAllByActiveTrue();
    verify(vehicleRepository, never()).findAll();
  }

  @Test
  void findAllActiveWhenRepositoryIsEmptyReturnsEmptyList() {
    when(vehicleRepository.findAllByActiveTrue()).thenReturn(List.of());

    List<VehicleResponseDto> result = vehicleService.findAllActive();

    assertTrue(result.isEmpty());
    verify(vehicleRepository).findAllByActiveTrue();
    verifyNoInteractions(vehicleMapper);
  }

  @Test
  void updateWhenRequestIsValidUpdatesSavesAndReturnsMappedResponse() {
    Vehicle entity = existingVehicle();
    UpdateVehicleRequestDto request = updateRequest(NEW_VIN, 67000);
    VehicleResponseDto expectedResponse = responseDto(VEHICLE_ID, NEW_VIN, true);

    when(vehicleRepository.findById(VEHICLE_ID))
        .thenReturn(Optional.of(entity));
    when(vehicleRepository.existsByVinAndIdNot(NEW_VIN, VEHICLE_ID))
        .thenReturn(false);
    when(vehicleRepository.save(entity)).thenReturn(entity);
    when(vehicleMapper.toResponseDto(entity)).thenReturn(expectedResponse);

    log.info(
        "Updating vehicle: id={}, requestedVin={}, requestedMileage={}",
        VEHICLE_ID,
        NEW_VIN,
        request.currentMileageKm()
    );

    VehicleResponseDto actualResponse = vehicleService.update(VEHICLE_ID, request);

    assertSame(expectedResponse, actualResponse);
    verify(vehicleRepository).findById(VEHICLE_ID);
    verify(vehicleRepository).existsByVinAndIdNot(NEW_VIN, VEHICLE_ID);
    verify(vehicleMapper).updateEntity(request, entity);
    verify(vehicleRepository).save(entity);
    verify(vehicleMapper).toResponseDto(entity);
  }

  @Test
  void updateWhenOptionalFieldsAreAbsentSkipsBusinessChecksAndUpdates() {
    Vehicle entity = existingVehicle();
    UpdateVehicleRequestDto request = updateRequest(null, null);
    VehicleResponseDto expectedResponse = responseDto(VEHICLE_ID, VIN, true);

    when(vehicleRepository.findById(VEHICLE_ID))
        .thenReturn(Optional.of(entity));
    when(vehicleRepository.save(entity)).thenReturn(entity);
    when(vehicleMapper.toResponseDto(entity)).thenReturn(expectedResponse);

    VehicleResponseDto actualResponse = vehicleService.update(VEHICLE_ID, request);

    assertSame(expectedResponse, actualResponse);
    verify(vehicleRepository, never()).existsByVinAndIdNot(any(), any());
    verify(vehicleMapper).updateEntity(request, entity);
    verify(vehicleRepository).save(entity);
  }

  @Test
  void updateWhenVinBelongsToAnotherVehicleThrowsConflictWithoutMutation() {
    Vehicle entity = existingVehicle();
    UpdateVehicleRequestDto request = updateRequest(NEW_VIN, null);

    when(vehicleRepository.findById(VEHICLE_ID))
        .thenReturn(Optional.of(entity));
    when(vehicleRepository.existsByVinAndIdNot(NEW_VIN, VEHICLE_ID))
        .thenReturn(true);

    VehicleVinAlreadyExistsException exception = assertThrows(
        VehicleVinAlreadyExistsException.class,
        () -> vehicleService.update(VEHICLE_ID, request)
    );

    assertEquals(
        "Vehicle with VIN " + NEW_VIN + " already exists",
        exception.getMessage()
    );
    assertEquals(VIN, entity.getVin());
    verify(vehicleMapper, never()).updateEntity(any(), any());
    verify(vehicleRepository, never()).save(any(Vehicle.class));
  }

  @Test
  void updateWhenMileageIsEqualToCurrentMileageIsAllowed() {
    Vehicle entity = existingVehicle();
    UpdateVehicleRequestDto request = updateRequest(null, 66000);
    VehicleResponseDto expectedResponse = responseDto(VEHICLE_ID, VIN, true);

    when(vehicleRepository.findById(VEHICLE_ID))
        .thenReturn(Optional.of(entity));
    when(vehicleRepository.save(entity)).thenReturn(entity);
    when(vehicleMapper.toResponseDto(entity)).thenReturn(expectedResponse);

    VehicleResponseDto actualResponse = vehicleService.update(VEHICLE_ID, request);

    assertSame(expectedResponse, actualResponse);
    verify(vehicleMapper).updateEntity(request, entity);
    verify(vehicleRepository).save(entity);
  }

  @Test
  void updateWhenMileageIsDecreasedThrowsConflictWithoutMutation() {
    Vehicle entity = existingVehicle();
    UpdateVehicleRequestDto request = updateRequest(null, 65000);

    when(vehicleRepository.findById(VEHICLE_ID))
        .thenReturn(Optional.of(entity));

    VehicleMileageCannotBeDecreasedException exception = assertThrows(
        VehicleMileageCannotBeDecreasedException.class,
        () -> vehicleService.update(VEHICLE_ID, request)
    );

    assertEquals(
        "Current mileage cannot be decreased from 66000 to 65000",
        exception.getMessage()
    );
    assertEquals(66000, entity.getCurrentMileageKm());
    verify(vehicleMapper, never()).updateEntity(any(), any());
    verify(vehicleRepository, never()).save(any(Vehicle.class));
  }

  @Test
  void updateWhenVehicleDoesNotExistThrowsNotFoundBeforeOtherChecks() {
    UpdateVehicleRequestDto request = updateRequest(NEW_VIN, 67000);
    when(vehicleRepository.findById(MISSING_ID))
        .thenReturn(Optional.empty());

    VehicleNotFoundException exception = assertThrows(
        VehicleNotFoundException.class,
        () -> vehicleService.update(MISSING_ID, request)
    );

    assertEquals(
        "Vehicle with id 999 not found",
        exception.getMessage()
    );
    verify(vehicleRepository).findById(MISSING_ID);
    verify(vehicleRepository, never()).existsByVinAndIdNot(any(), any());
    verify(vehicleRepository, never()).save(any(Vehicle.class));
    verifyNoInteractions(vehicleMapper);
  }

  @Test
  void softDeleteWhenVehicleExistsMarksItInactiveAndSavesIt() {
    Vehicle entity = existingVehicle();
    when(vehicleRepository.findById(VEHICLE_ID))
        .thenReturn(Optional.of(entity));
    when(vehicleRepository.save(entity)).thenReturn(entity);

    log.info("Soft deleting vehicle: id={}", VEHICLE_ID);

    vehicleService.softDelete(VEHICLE_ID);

    assertFalse(entity.isActive());
    verify(vehicleRepository).findById(VEHICLE_ID);
    verify(vehicleRepository).save(entity);
    verifyNoInteractions(vehicleMapper);
  }

  @Test
  void softDeleteWhenVehicleDoesNotExistThrowsNotFoundWithoutSaving() {
    when(vehicleRepository.findById(MISSING_ID))
        .thenReturn(Optional.empty());

    VehicleNotFoundException exception = assertThrows(
        VehicleNotFoundException.class,
        () -> vehicleService.softDelete(MISSING_ID)
    );

    assertEquals(
        "Vehicle with id 999 not found",
        exception.getMessage()
    );
    verify(vehicleRepository).findById(MISSING_ID);
    verify(vehicleRepository, never()).save(any(Vehicle.class));
    verifyNoMoreInteractions(vehicleRepository);
    verifyNoInteractions(vehicleMapper);
  }

  private Vehicle existingVehicle() {
    Vehicle entity = vehicle(VIN, 66000);
    entity.setId(VEHICLE_ID);
    entity.setMake("Chevrolet");
    entity.setModel("Corvette Stingray");
    entity.setProductionYear(2017);
    entity.setLicensePlate("NR777CC");
    entity.setFuelType(FuelType.OTHER);
    entity.setPurchaseDate(LocalDate.of(2026, 8, 12));
    entity.setPurchasePrice(new BigDecimal("44999.90"));
    return entity;
  }

  private CreateVehicleRequestDto createRequest(String vin) {
    return new CreateVehicleRequestDto(
        "Chevrolet",
        "Corvette Stingray",
        2017,
        vin,
        "NR777CC",
        FuelType.OTHER,
        66000,
        LocalDate.of(2026, 8, 12),
        new BigDecimal("44999.90")
    );
  }

  private UpdateVehicleRequestDto updateRequest(
      String vin,
      Integer currentMileageKm
  ) {
    return new UpdateVehicleRequestDto(
        null,
        null,
        null,
        vin,
        null,
        null,
        currentMileageKm,
        null,
        null
    );
  }

  private VehicleResponseDto responseDto(
      long id,
      String vin,
      boolean active
  ) {
    return new VehicleResponseDto(
        id,
        "Chevrolet",
        "Corvette Stingray",
        2017,
        vin,
        "NR777CC",
        FuelType.OTHER,
        66000,
        LocalDate.of(2026, 8, 12),
        new BigDecimal("44999.90"),
        active,
        Instant.parse("2026-09-08T22:56:26Z"),
        Instant.parse("2026-09-08T23:07:39Z")
    );
  }
}
