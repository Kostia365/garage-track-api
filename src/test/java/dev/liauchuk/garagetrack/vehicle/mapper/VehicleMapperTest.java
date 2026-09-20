package dev.liauchuk.garagetrack.vehicle.mapper;

import dev.liauchuk.garagetrack.vehicle.FuelType;
import dev.liauchuk.garagetrack.vehicle.Vehicle;
import dev.liauchuk.garagetrack.vehicle.VehicleMapper;
import dev.liauchuk.garagetrack.vehicle.dto.CreateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.UpdateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.VehicleResponseDto;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static dev.liauchuk.garagetrack.vehicle.VehicleTestFactory.vehicle;
import static org.junit.jupiter.api.Assertions.*;

class VehicleMapperTest {
  private static final Logger log =
      LoggerFactory.getLogger(VehicleMapperTest.class);

  private final VehicleMapper vehicleMapper =
      new VehicleMapper();

  public static UpdateVehicleRequestDto emptyUpdateRequest() {
    return new UpdateVehicleRequestDto(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
  }

  @Test
  void updateEntityWhenAllFieldsProvidedUpdatesAllFields() {
    Vehicle existingVehicle = vehicle(
        "OLDVIN12345678901",
        10000
    );

    UpdateVehicleRequestDto dto =
        new UpdateVehicleRequestDto(
            "Chevrolet",
            "Corvette Stingray",
            2017,
            "1G1YB2D70H5100001",
            "NR777CC",
            FuelType.OTHER,
            66000,
            LocalDate.of(2026, 8, 12),
            new BigDecimal("44999.90")
        );

    log.info(
        "Testing full vehicle update: oldVin={}, oldMileage={}",
        existingVehicle.getVin(),
        existingVehicle.getCurrentMileageKm()
    );

    vehicleMapper.updateEntity(dto, existingVehicle);

    log.info(
        "Vehicle updated: make={}, model={}, year={}, vin={}, "
            + "licensePlate={}, fuelType={}, mileage={}, "
            + "purchaseDate={}, purchasePrice={}",
        existingVehicle.getMake(),
        existingVehicle.getModel(),
        existingVehicle.getProductionYear(),
        existingVehicle.getVin(),
        existingVehicle.getLicensePlate(),
        existingVehicle.getFuelType(),
        existingVehicle.getCurrentMileageKm(),
        existingVehicle.getPurchaseDate(),
        existingVehicle.getPurchasePrice()
    );

    assertAll(
        () -> assertEquals(
            "Chevrolet",
            existingVehicle.getMake()
        ),
        () -> assertEquals(
            "Corvette Stingray",
            existingVehicle.getModel()
        ),
        () -> assertEquals(
            2017,
            existingVehicle.getProductionYear()
        ),
        () -> assertEquals(
            "1G1YB2D70H5100001",
            existingVehicle.getVin()
        ),
        () -> assertEquals(
            "NR777CC",
            existingVehicle.getLicensePlate()
        ),
        () -> assertEquals(
            FuelType.OTHER,
            existingVehicle.getFuelType()
        ),
        () -> assertEquals(
            66000,
            existingVehicle.getCurrentMileageKm()
        ),
        () -> assertEquals(
            LocalDate.of(2026, 8, 12),
            existingVehicle.getPurchaseDate()
        ),
        () -> assertEquals(
            new BigDecimal("44999.90"),
            existingVehicle.getPurchasePrice()
        )
    );

    log.info("Full vehicle update mapping test passed");
  }

  @Test
  void toEntityMapsAllRequestFields() {
    CreateVehicleRequestDto dto =
        new CreateVehicleRequestDto(
            "Chevrolet",
            "Corvette Stingray",
            2017,
            "1G1YB2D70H5100001",
            "NR777CC",
            FuelType.OTHER,
            66000,
            LocalDate.of(2026, 8, 12),
            new BigDecimal("44999.90")
        );

    log.info(
        "Mapping CreateVehicleRequestDto to Vehicle: vin={}",
        dto.vin()
    );

    Vehicle vehicle = vehicleMapper.toEntity(dto);

    assertAll(
        () -> assertNotNull(vehicle),
        () -> assertEquals(
            "Chevrolet",
            vehicle.getMake()
        ),
        () -> assertEquals(
            "Corvette Stingray",
            vehicle.getModel()
        ),
        () -> assertEquals(
            2017,
            vehicle.getProductionYear()
        ),
        () -> assertEquals(
            "1G1YB2D70H5100001",
            vehicle.getVin()
        ),
        () -> assertEquals(
            "NR777CC",
            vehicle.getLicensePlate()
        ),
        () -> assertEquals(
            FuelType.OTHER,
            vehicle.getFuelType()
        ),
        () -> assertEquals(
            66000,
            vehicle.getCurrentMileageKm()
        ),
        () -> assertEquals(
            LocalDate.of(2026, 8, 12),
            vehicle.getPurchaseDate()
        ),
        () -> assertEquals(
            new BigDecimal("44999.90"),
            vehicle.getPurchasePrice()
        )
    );

    log.info(
        "CreateVehicleRequestDto mapped successfully: vin={}, mileage={}",
        vehicle.getVin(),
        vehicle.getCurrentMileageKm()
    );
  }

  @Test
  void toResponseDtoMapsAllEntityFields() {
    Instant createdAt =
        Instant.parse("2026-09-08T22:56:26Z");
    Instant updatedAt =
        Instant.parse("2026-09-08T23:07:39Z");

    Vehicle vehicle = vehicle(
        "1G1YB2D70H5100001",
        66000
    );

    vehicle.setId(2L);
    vehicle.setMake("Chevrolet");
    vehicle.setModel("Corvette Stingray");
    vehicle.setProductionYear(2017);
    vehicle.setLicensePlate("NR777CC");
    vehicle.setFuelType(FuelType.OTHER);
    vehicle.setPurchaseDate(
        LocalDate.of(2026, 8, 12)
    );
    vehicle.setPurchasePrice(
        new BigDecimal("44999.90")
    );
    vehicle.setActive(false);
    vehicle.setCreatedAt(createdAt);
    vehicle.setUpdatedAt(updatedAt);

    log.info(
        "Mapping Vehicle to VehicleResponseDto: id={}, vin={}",
        vehicle.getId(),
        vehicle.getVin()
    );

    VehicleResponseDto dto =
        vehicleMapper.toResponseDto(vehicle);

    assertAll(
        () -> assertNotNull(dto),
        () -> assertEquals(2L, dto.id()),
        () -> assertEquals(
            "Chevrolet",
            dto.make()
        ),
        () -> assertEquals(
            "Corvette Stingray",
            dto.model()
        ),
        () -> assertEquals(
            2017,
            dto.productionYear()
        ),
        () -> assertEquals(
            "1G1YB2D70H5100001",
            dto.vin()
        ),
        () -> assertEquals(
            "NR777CC",
            dto.licensePlate()
        ),
        () -> assertEquals(
            FuelType.OTHER,
            dto.fuelType()
        ),
        () -> assertEquals(
            66000,
            dto.currentMileageKm()
        ),
        () -> assertEquals(
            LocalDate.of(2026, 8, 12),
            dto.purchaseDate()
        ),
        () -> assertEquals(
            new BigDecimal("44999.90"),
            dto.purchasePrice()
        ),
        () -> assertFalse(dto.active()),
        () -> assertEquals(
            createdAt,
            dto.createdAt()
        ),
        () -> assertEquals(
            updatedAt,
            dto.updatedAt()
        )
    );

    log.info(
        "Vehicle mapped successfully to response DTO: id={}, active={}",
        dto.id(),
        dto.active()
    );
  }

  @Test
  void updateEntityWhenAllFieldsAreNullDoesNotChangeVehicle() {
    Vehicle existingVehicle = vehicle(
        "1G1YB2D70H5100001",
        66000
    );

    existingVehicle.setMake("Chevrolet");
    existingVehicle.setModel("Corvette Stingray");
    existingVehicle.setProductionYear(2017);
    existingVehicle.setLicensePlate("NR777CC");
    existingVehicle.setFuelType(FuelType.OTHER);
    existingVehicle.setPurchaseDate(
        LocalDate.of(2026, 8, 12)
    );
    existingVehicle.setPurchasePrice(
        new BigDecimal("44999.90")
    );

    UpdateVehicleRequestDto dto =
        emptyUpdateRequest();

    log.info(
        "Testing empty PATCH for vehicle: vin={}",
        existingVehicle.getVin()
    );

    vehicleMapper.updateEntity(dto, existingVehicle);

    assertAll(
        () -> assertEquals(
            "Chevrolet",
            existingVehicle.getMake()
        ),
        () -> assertEquals(
            "Corvette Stingray",
            existingVehicle.getModel()
        ),
        () -> assertEquals(
            2017,
            existingVehicle.getProductionYear()
        ),
        () -> assertEquals(
            "1G1YB2D70H5100001",
            existingVehicle.getVin()
        ),
        () -> assertEquals(
            "NR777CC",
            existingVehicle.getLicensePlate()
        ),
        () -> assertEquals(
            FuelType.OTHER,
            existingVehicle.getFuelType()
        ),
        () -> assertEquals(
            66000,
            existingVehicle.getCurrentMileageKm()
        ),
        () -> assertEquals(
            LocalDate.of(2026, 8, 12),
            existingVehicle.getPurchaseDate()
        ),
        () -> assertEquals(
            new BigDecimal("44999.90"),
            existingVehicle.getPurchasePrice()
        )
    );

    log.info(
        "Empty PATCH did not modify vehicle: vin={}",
        existingVehicle.getVin()
    );
  }

}
