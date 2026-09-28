package dev.liauchuk.garagetrack.fuelentry;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "fuel_entries")
public class FuelEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, name = "vehicle_id")
  private long vehicleId;
  @Column(nullable = false, name = "filled_at")
  private Instant filledAt;
  @Column(nullable = false, name = "odometer_km")
  private Integer odometer;
  @Column(nullable = false, name = "liters")
  private BigDecimal amountLiters;
  @Column(nullable = false, name = "price_per_liter")
  private BigDecimal pricePerLiter;
  @Column(nullable = false, name = "total_cost", precision = 12, scale = 2)
  private BigDecimal totalPrice;
  @Column(name = "notes", length = 300)
  private String notes;
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
  @Column(nullable = false)
  private boolean fullTank;
  @Column(name = "fuel_station", length = 100)
  private String fuelStation;

  @PrePersist
  private void beforeInsert() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
    if (filledAt == null) {
      filledAt = now;
    }
  }

  @PreUpdate
  private void beforeUpdate() {
    updatedAt = Instant.now();
  }
}
