package dev.liauchuk.garagetrack.fuelentry;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FuelEntryRepository extends JpaRepository<FuelEntry, Long> {
  FuelEntry[] findByVehicleId(Long vehicleId);

  boolean existsById(Long id);

  boolean existsByVehicleId(Long vehicleId);
}
