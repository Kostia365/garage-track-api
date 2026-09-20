package dev.liauchuk.garagetrack.vehicle;

public final class VehicleTestFactory {

  private VehicleTestFactory() {
  }

  public static Vehicle vehicle(
      String vin,
      int currentMileageKm
  ) {
    Vehicle vehicle = new Vehicle();
    vehicle.setVin(vin);
    vehicle.setCurrentMileageKm(currentMileageKm);
    vehicle.setActive(true);
    return vehicle;
  }
}
