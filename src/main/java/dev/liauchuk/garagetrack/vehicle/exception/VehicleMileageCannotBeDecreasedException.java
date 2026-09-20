package dev.liauchuk.garagetrack.vehicle.exception;

public class VehicleMileageCannotBeDecreasedException
    extends RuntimeException {

  public VehicleMileageCannotBeDecreasedException(
      int currentMileageKm,
      int requestedMileageKm
  ) {
    super("Current mileage cannot be decreased from " + currentMileageKm + " to " + requestedMileageKm);
  }
}
