package dev.liauchuk.garagetrack.vehicle.repository;

import dev.liauchuk.garagetrack.vehicle.FuelType;
import dev.liauchuk.garagetrack.vehicle.Vehicle;
import dev.liauchuk.garagetrack.vehicle.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.List;

import static dev.liauchuk.garagetrack.vehicle.VehicleTestFactory.vehicle;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Testcontainers
class VehicleRepositoryTest {

  private static final Logger log =
      LoggerFactory.getLogger(VehicleRepositoryTest.class);

  private static final String FIRST_VIN = "WVWZZZ1JZXW000001";
  private static final String SECOND_VIN = "WVWZZZ1JZXW000002";
  private static final String UNKNOWN_VIN = "WVWZZZ1JZXW999999";

  @Container
  @ServiceConnection
  private static final PostgreSQLContainer postgres =
      new PostgreSQLContainer(
          DockerImageName.parse("postgres:18.1-alpine")
      );

  @Autowired
  private VehicleRepository vehicleRepository;

  @Test
  void existsByVinReturnsTrueOnlyForPersistedVin() {
    vehicleRepository.saveAndFlush(testVehicle(FIRST_VIN, true));

    log.info("Checking persisted VIN: {}", FIRST_VIN);

    assertTrue(vehicleRepository.existsByVin(FIRST_VIN));
    assertFalse(vehicleRepository.existsByVin(UNKNOWN_VIN));
  }

  @Test
  void existsByVinAndIdNotExcludesCurrentVehicle() {
    Vehicle first = vehicleRepository.saveAndFlush(
        testVehicle(FIRST_VIN, true)
    );
    Vehicle second = vehicleRepository.saveAndFlush(
        testVehicle(SECOND_VIN, true)
    );

    assertFalse(
        vehicleRepository.existsByVinAndIdNot(FIRST_VIN, first.getId())
    );
    assertTrue(
        vehicleRepository.existsByVinAndIdNot(FIRST_VIN, second.getId())
    );
    assertFalse(
        vehicleRepository.existsByVinAndIdNot(UNKNOWN_VIN, first.getId())
    );
  }

  @Test
  void findAllByActiveTrueReturnsActiveAndExcludesArchivedVehicles() {
    Vehicle active = vehicleRepository.saveAndFlush(
        testVehicle(FIRST_VIN, true)
    );
    Vehicle archived = vehicleRepository.saveAndFlush(
        testVehicle(SECOND_VIN, false)
    );

    List<Vehicle> result = vehicleRepository.findAllByActiveTrue();

    assertTrue(result.stream().allMatch(Vehicle::isActive));
    assertTrue(result.stream().anyMatch(vehicle ->
        vehicle.getId().equals(active.getId())
    ));
    assertFalse(result.stream().anyMatch(vehicle ->
        vehicle.getId().equals(archived.getId())
    ));
  }

  @Test
  void saveWhenVinIsDuplicatedViolatesDatabaseConstraint() {
    vehicleRepository.saveAndFlush(testVehicle(FIRST_VIN, true));

    assertThrows(
        DataIntegrityViolationException.class,
        () -> vehicleRepository.saveAndFlush(testVehicle(FIRST_VIN, true))
    );
  }

  @ParameterizedTest(name = "{0} can be persisted")
  @EnumSource(FuelType.class)
  void everyFuelTypeCanBePersisted(FuelType fuelType) {
    Vehicle entity = testVehicle(vinFor(fuelType), true);
    entity.setFuelType(fuelType);

    vehicleRepository.saveAndFlush(entity);
  }

  private String vinFor(FuelType fuelType) {
    return "WVWZZZ1JZXW" + String.format(
        "%06d",
        fuelType.ordinal() + 1
    );
  }

  private Vehicle testVehicle(String vin, boolean active) {
    Vehicle entity = vehicle(vin, 10000);
    entity.setMake("Test Make");
    entity.setModel("Test Model");
    entity.setProductionYear(2020);
    entity.setLicensePlate(null);
    entity.setFuelType(FuelType.OTHER);
    entity.setPurchaseDate(LocalDate.of(2020, 1, 1));
    entity.setPurchasePrice(null);
    entity.setActive(active);
    return entity;
  }
}
