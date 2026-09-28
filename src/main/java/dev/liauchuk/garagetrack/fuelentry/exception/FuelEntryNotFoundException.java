package dev.liauchuk.garagetrack.fuelentry.exception;

public class FuelEntryNotFoundException extends RuntimeException {
  public FuelEntryNotFoundException(long id) {
    super("Fuel entry with id " + id + " not found");
  }
}
