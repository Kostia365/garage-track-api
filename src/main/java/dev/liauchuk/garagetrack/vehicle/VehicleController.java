package dev.liauchuk.garagetrack.vehicle;

import dev.liauchuk.garagetrack.vehicle.dto.CreateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.UpdateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.VehicleResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
  private final VehicleService vehicleService;

  public VehicleController(VehicleService vehicleService) {
    this.vehicleService = vehicleService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public VehicleResponseDto create(
      @Valid @RequestBody CreateVehicleRequestDto dto
  ) {
    return vehicleService.create(dto);
  }

  @GetMapping()
  @ResponseStatus(HttpStatus.OK)
  public List<VehicleResponseDto> getAll() {
    return vehicleService.getAll();
  }

  @GetMapping("/active")
  @ResponseStatus(HttpStatus.OK)
  public List<VehicleResponseDto> getAllActive() {
    return vehicleService.findAllActive();
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void softDelete(@PathVariable("id") long id) {
    vehicleService.softDelete(id);
  }

  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public VehicleResponseDto getById(@PathVariable("id") long id) {
    return vehicleService.getById(id);
  }

  @PatchMapping("/{id}")
  public VehicleResponseDto update(
      @PathVariable("id") long id,
      @Valid @RequestBody UpdateVehicleRequestDto dto
  ) {
    return vehicleService.update(id, dto);
  }

}
