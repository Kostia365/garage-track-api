package dev.liauchuk.garagetrack.vehicle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
  boolean existsByVin(String vin);

  boolean existsByVinAndIdNot(String vin, Long id);

  Optional<Vehicle> findById(Long id);

  List<Vehicle> findAllByActiveTrue();

}
