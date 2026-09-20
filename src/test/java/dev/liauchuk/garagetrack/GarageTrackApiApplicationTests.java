package dev.liauchuk.garagetrack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class GarageTrackApiApplicationTests {

  @Container
  @ServiceConnection
  private static final PostgreSQLContainer postgres =
      new PostgreSQLContainer(
          DockerImageName.parse("postgres:18.1-alpine")
      );

  @Test
  void contextLoads() {
  }

}
