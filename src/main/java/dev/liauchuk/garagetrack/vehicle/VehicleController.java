package dev.liauchuk.garagetrack.vehicle;

import dev.liauchuk.garagetrack.common.error.ApiErrorResponseDto;
import dev.liauchuk.garagetrack.vehicle.dto.CreateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.UpdateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.VehicleResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Vehicles", description = "Vehicle management operations")
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
  private final VehicleService vehicleService;

  public VehicleController(VehicleService vehicleService) {
    this.vehicleService = vehicleService;
  }

  @Operation(summary = "Create a new vehicle", description = "Create a new vehicle")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Vehicle created successfully", content = @Content(schema = @Schema(implementation = VehicleResponseDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class))),
      @ApiResponse(responseCode = "409", description = "Vehicle with the same VIN already exists", content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class)))
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public VehicleResponseDto create(
      @Valid @RequestBody CreateVehicleRequestDto dto
  ) {
    return vehicleService.create(dto);
  }

  @Operation(summary = "Get all vehicles", description = "Get all vehicles")
  @ApiResponse(responseCode = "200", description = "List of vehicles", content = @Content(schema = @Schema(implementation = VehicleResponseDto[].class)))
  @GetMapping()
  @ResponseStatus(HttpStatus.OK)
  public List<VehicleResponseDto> getAll() {
    return vehicleService.getAll();
  }

  @Operation(summary = "Get all active vehicles", description = "Get all active vehicles")
  @ApiResponse(responseCode = "200", description = "List of active vehicles", content = @Content(schema = @Schema(implementation = VehicleResponseDto[].class)))
  @GetMapping("/active")
  @ResponseStatus(HttpStatus.OK)
  public List<VehicleResponseDto> getAllActive() {
    return vehicleService.findAllActive();
  }

  @Operation(summary = "Get vehicle by ID", description = "Get vehicle by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Vehicle returned successfully", content = @Content(schema = @Schema(implementation = VehicleResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "Vehicle not found", content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class)))
  })
  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public VehicleResponseDto getById(@PathVariable("id") long id) {
    return vehicleService.getById(id);
  }

  @Operation(summary = "Soft delete vehicle by ID", description = "Soft delete vehicle by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Vehicle archived successfully"),
      @ApiResponse(responseCode = "404", description = "Vehicle not found", content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class)))
  })
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void softDelete(@PathVariable("id") long id) {
    vehicleService.softDelete(id);
  }

  @Operation(summary = "Update vehicle by ID", description = "Update vehicle by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Vehicle updated successfully", content = @Content(schema = @Schema(implementation = VehicleResponseDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "Vehicle not found", content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class))),
      @ApiResponse(responseCode = "409", description = "Vehicle with the same VIN already exists or mileage cannot be decreased", content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class)))
  })

  @PatchMapping("/{id}")
  public VehicleResponseDto update(
      @PathVariable("id") long id,
      @Valid @RequestBody UpdateVehicleRequestDto dto
  ) {
    return vehicleService.update(id, dto);
  }

}
