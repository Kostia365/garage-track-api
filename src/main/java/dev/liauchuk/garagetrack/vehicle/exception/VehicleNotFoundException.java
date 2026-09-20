package dev.liauchuk.garagetrack.vehicle.exception;

public class VehicleNotFoundException extends RuntimeException {
  public VehicleNotFoundException(long id) {
    super("Vehicle with id " + id + " not found");
  }
}
