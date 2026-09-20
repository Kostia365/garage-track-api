package dev.liauchuk.garagetrack.vehicle.controller;

import dev.liauchuk.garagetrack.common.error.GlobalExceptionHandler;
import dev.liauchuk.garagetrack.vehicle.FuelType;
import dev.liauchuk.garagetrack.vehicle.VehicleController;
import dev.liauchuk.garagetrack.vehicle.VehicleService;
import dev.liauchuk.garagetrack.vehicle.dto.CreateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.UpdateVehicleRequestDto;
import dev.liauchuk.garagetrack.vehicle.dto.VehicleResponseDto;
import dev.liauchuk.garagetrack.vehicle.exception.VehicleMileageCannotBeDecreasedException;
import dev.liauchuk.garagetrack.vehicle.exception.VehicleNotFoundException;
import dev.liauchuk.garagetrack.vehicle.exception.VehicleVinAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

  private static final Logger log =
      LoggerFactory.getLogger(VehicleControllerTest.class);

  private static final long VEHICLE_ID = 2L;
  private static final String VIN = "1G1YB2D70H5100001";

  @Mock
  private VehicleService vehicleService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .standaloneSetup(new VehicleController(vehicleService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void createWhenRequestIsValidReturns201AndVehicleJson() throws Exception {
    VehicleResponseDto response = responseDto();
    when(vehicleService.create(any(CreateVehicleRequestDto.class)))
        .thenReturn(response);

    log.info("Checking POST /api/vehicles");

    mockMvc.perform(post("/api/vehicles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validCreateJson()))
        .andExpect(status().isCreated())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(VEHICLE_ID))
        .andExpect(jsonPath("$.make").value("Chevrolet"))
        .andExpect(jsonPath("$.model").value("Corvette Stingray"))
        .andExpect(jsonPath("$.vin").value(VIN))
        .andExpect(jsonPath("$.currentMileageKm").value(66000))
        .andExpect(jsonPath("$.active").value(true));

    ArgumentCaptor<CreateVehicleRequestDto> captor =
        ArgumentCaptor.forClass(CreateVehicleRequestDto.class);
    verify(vehicleService).create(captor.capture());
    assertEquals(VIN, captor.getValue().vin());
    assertEquals(66000, captor.getValue().currentMileageKm());
  }

  @Test
  void createWhenRequestIsInvalidReturns400WithoutCallingService() throws Exception {
    mockMvc.perform(post("/api/vehicles")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "make": "",
                  "model": "Corvette Stingray",
                  "productionYear": 1800,
                  "fuelType": "OTHER",
                  "currentMileageKm": -1
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message", containsString("make:must not be blank")))
        .andExpect(jsonPath("$.path").value("/api/vehicles"));

    verifyNoInteractions(vehicleService);
  }

  @Test
  void createWhenJsonIsMalformedReturns400WithoutCallingService() throws Exception {
    mockMvc.perform(post("/api/vehicles")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"make\":"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(vehicleService);
  }

  @Test
  void createWhenFuelTypeIsUnknownReturns400WithoutCallingService() throws Exception {
    mockMvc.perform(post("/api/vehicles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validCreateJson().replace("OTHER", "STEAM")))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(vehicleService);
  }

  @Test
  void createWhenVinAlreadyExistsReturns409ErrorJson() throws Exception {
    when(vehicleService.create(any(CreateVehicleRequestDto.class)))
        .thenThrow(new VehicleVinAlreadyExistsException(VIN));

    mockMvc.perform(post("/api/vehicles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validCreateJson()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.error").value("Conflict"))
        .andExpect(jsonPath("$.message")
            .value("Vehicle with VIN " + VIN + " already exists"))
        .andExpect(jsonPath("$.path").value("/api/vehicles"));
  }

  @Test
  void getAllReturns200AndVehicleArray() throws Exception {
    when(vehicleService.getAll()).thenReturn(List.of(responseDto()));

    mockMvc.perform(get("/api/vehicles"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(VEHICLE_ID))
        .andExpect(jsonPath("$[0].vin").value(VIN));

    verify(vehicleService).getAll();
  }

  @Test
  void getAllActiveReturns200AndUsesActiveServiceMethod() throws Exception {
    when(vehicleService.findAllActive()).thenReturn(List.of(responseDto()));

    mockMvc.perform(get("/api/vehicles/active"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].active").value(true));

    verify(vehicleService).findAllActive();
    verify(vehicleService, never()).getAll();
  }

  @Test
  void getByIdWhenVehicleExistsReturns200AndVehicleJson() throws Exception {
    when(vehicleService.getById(VEHICLE_ID)).thenReturn(responseDto());

    mockMvc.perform(get("/api/vehicles/{id}", VEHICLE_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(VEHICLE_ID))
        .andExpect(jsonPath("$.vin").value(VIN));

    verify(vehicleService).getById(VEHICLE_ID);
  }

  @Test
  void getByIdWhenVehicleDoesNotExistReturns404ErrorJson() throws Exception {
    when(vehicleService.getById(999L))
        .thenThrow(new VehicleNotFoundException(999L));

    mockMvc.perform(get("/api/vehicles/{id}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message")
            .value("Vehicle with id 999 not found"))
        .andExpect(jsonPath("$.path").value("/api/vehicles/999"));
  }

  @Test
  void updateWhenRequestIsValidReturns200AndVehicleJson() throws Exception {
    when(vehicleService.update(
        org.mockito.ArgumentMatchers.eq(VEHICLE_ID),
        any(UpdateVehicleRequestDto.class)
    )).thenReturn(responseDto());

    mockMvc.perform(patch("/api/vehicles/{id}", VEHICLE_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"currentMileageKm\":66000}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(VEHICLE_ID))
        .andExpect(jsonPath("$.currentMileageKm").value(66000));

    ArgumentCaptor<UpdateVehicleRequestDto> captor =
        ArgumentCaptor.forClass(UpdateVehicleRequestDto.class);
    verify(vehicleService).update(
        org.mockito.ArgumentMatchers.eq(VEHICLE_ID),
        captor.capture()
    );
    assertEquals(66000, captor.getValue().currentMileageKm());
  }

  @Test
  void updateWhenMakeIsBlankReturns400WithoutCallingService() throws Exception {
    mockMvc.perform(patch("/api/vehicles/{id}", VEHICLE_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"make\":\"   \"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message", containsString("make:must not be blank")));

    verifyNoInteractions(vehicleService);
  }

  @Test
  void updateWhenMileageIsDecreasedReturns409ErrorJson() throws Exception {
    when(vehicleService.update(
        org.mockito.ArgumentMatchers.eq(VEHICLE_ID),
        any(UpdateVehicleRequestDto.class)
    )).thenThrow(new VehicleMileageCannotBeDecreasedException(66000, 65000));

    mockMvc.perform(patch("/api/vehicles/{id}", VEHICLE_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"currentMileageKm\":65000}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.message")
            .value("Current mileage cannot be decreased from 66000 to 65000"))
        .andExpect(jsonPath("$.path").value("/api/vehicles/2"));
  }

  @Test
  void softDeleteWhenVehicleExistsReturns204WithoutBody() throws Exception {
    mockMvc.perform(delete("/api/vehicles/{id}", VEHICLE_ID))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));

    verify(vehicleService).softDelete(VEHICLE_ID);
  }

  @Test
  void softDeleteWhenVehicleDoesNotExistReturns404ErrorJson() throws Exception {
    org.mockito.Mockito.doThrow(new VehicleNotFoundException(999L))
        .when(vehicleService)
        .softDelete(999L);

    mockMvc.perform(delete("/api/vehicles/{id}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message")
            .value("Vehicle with id 999 not found"))
        .andExpect(jsonPath("$.path").value("/api/vehicles/999"));
  }

  private String validCreateJson() {
    return """
        {
          "make": "Chevrolet",
          "model": "Corvette Stingray",
          "productionYear": 2017,
          "vin": "1G1YB2D70H5100001",
          "licensePlate": "NR777CC",
          "fuelType": "OTHER",
          "currentMileageKm": 66000,
          "purchaseDate": "2026-08-12",
          "purchasePrice": 44999.90
        }
        """;
  }

  private VehicleResponseDto responseDto() {
    return new VehicleResponseDto(
        VEHICLE_ID,
        "Chevrolet",
        "Corvette Stingray",
        2017,
        VIN,
        "NR777CC",
        FuelType.OTHER,
        66000,
        LocalDate.of(2026, 8, 12),
        new BigDecimal("44999.90"),
        true,
        Instant.parse("2026-09-08T22:56:26Z"),
        Instant.parse("2026-09-08T23:07:39Z")
    );
  }
}
