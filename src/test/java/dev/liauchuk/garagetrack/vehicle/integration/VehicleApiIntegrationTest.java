package dev.liauchuk.garagetrack.vehicle.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class VehicleApiIntegrationTest {

  private static final String VIN = "1G1YB2D70H5100001";

  @Container
  @ServiceConnection
  private static final PostgreSQLContainer postgres =
      new PostgreSQLContainer(
          DockerImageName.parse("postgres:18.1-alpine")
      );

  @Autowired
  private MockMvc mockMvc;

  @Test
  void vehicleLifecyclePersistsAcrossTheCompleteRestStack() throws Exception {
    String createdJson = mockMvc.perform(post("/api/vehicles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validCreateJson()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.vin").value(VIN))
        .andExpect(jsonPath("$.fuelType").value("CNG"))
        .andExpect(jsonPath("$.active").value(true))
        .andReturn()
        .getResponse()
        .getContentAsString();

    Number createdId = JsonPath.read(createdJson, "$.id");
    long vehicleId = createdId.longValue();

    mockMvc.perform(get("/api/vehicles/{id}", vehicleId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(vehicleId))
        .andExpect(jsonPath("$.make").value("Chevrolet"))
        .andExpect(jsonPath("$.model").value("Corvette Stingray"))
        .andExpect(jsonPath("$.currentMileageKm").value(66000));

    mockMvc.perform(patch("/api/vehicles/{id}", vehicleId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "model": "Corvette Stingray Z51",
                  "currentMileageKm": 67000
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(vehicleId))
        .andExpect(jsonPath("$.make").value("Chevrolet"))
        .andExpect(jsonPath("$.model").value("Corvette Stingray Z51"))
        .andExpect(jsonPath("$.vin").value(VIN))
        .andExpect(jsonPath("$.fuelType").value("CNG"))
        .andExpect(jsonPath("$.currentMileageKm").value(67000))
        .andExpect(jsonPath("$.active").value(true));

    mockMvc.perform(delete("/api/vehicles/{id}", vehicleId))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));

    mockMvc.perform(get("/api/vehicles"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(vehicleId))
        .andExpect(jsonPath("$[0].active").value(false));

    mockMvc.perform(get("/api/vehicles/active"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  private String validCreateJson() {
    return """
        {
          "make": "Chevrolet",
          "model": "Corvette Stingray",
          "productionYear": 2017,
          "vin": "1G1YB2D70H5100001",
          "licensePlate": "NR777CC",
          "fuelType": "CNG",
          "currentMileageKm": 66000,
          "purchaseDate": "2026-08-12",
          "purchasePrice": 44999.90
        }
        """;
  }
}
