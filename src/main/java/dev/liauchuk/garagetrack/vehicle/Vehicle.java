package dev.liauchuk.garagetrack.vehicle;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "vehicles")
public class Vehicle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, length = 100)
  private String make;
  @Column(nullable = false, length = 100)
  private String model;
  @Column(name = "production_year", nullable = false)
  private Integer productionYear;
  @Column(unique = true, length = 17)
  private String vin;
  @Column(name = "license_plate", length = 20)
  private String licensePlate;
  @Enumerated(EnumType.STRING)
  @Column(name = "fuel_type", nullable = false, length = 20)
  private FuelType fuelType;
  @Column(name = "current_mileage_km", nullable = false)
  private Integer currentMileageKm;
  @Column(name = "purchase_date")
  private LocalDate purchaseDate;
  @Column(name = "purchase_price", precision = 12, scale = 2)
  private BigDecimal purchasePrice;
  @Column(nullable = false)
  private boolean active = true;
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  private void beforeInsert() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  private void beforeUpdate() {
    updatedAt = Instant.now();
  }
}
