package dev.liauchuk.garagetrack.vehicle.exception;

public class VehicleVinAlreadyExistsException extends RuntimeException {
  public VehicleVinAlreadyExistsException(String vin) {
    super("Vehicle with VIN " + vin + " already exists");
  }
}
