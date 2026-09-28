package dev.liauchuk.garagetrack.fuelentry;

import dev.liauchuk.garagetrack.fuelentry.dto.CreateFuelEntryRequestDto;
import dev.liauchuk.garagetrack.fuelentry.dto.FuelEntryResponseDto;
import dev.liauchuk.garagetrack.fuelentry.dto.UpdateFuelEntryRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fuel-entries")
public class FuelEntryController {
  private final FuelEntryService fuelEntryService;
  private final FuelEntryRepository fuelEntryRepository;

  public FuelEntryController(FuelEntryService fuelEntryService, FuelEntryRepository fuelEntryRepository) {
    this.fuelEntryService = fuelEntryService;
    this.fuelEntryRepository = fuelEntryRepository;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public FuelEntryResponseDto create(@Valid @RequestBody CreateFuelEntryRequestDto dto) {
    return fuelEntryService.create(dto);
  }

  @PatchMapping("/{id}")
  public FuelEntryResponseDto update(
      @PathVariable("id") long id,
      @Valid @RequestBody UpdateFuelEntryRequestDto dto
  ) {
    return fuelEntryService.edit(id, dto);
  }


  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") long id) {
    fuelEntryService.delete(id);
  }
}
