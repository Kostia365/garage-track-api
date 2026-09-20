package dev.liauchuk.garagetrack.common.error;

import java.time.Instant;

public record ApiErrorResponseDto(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path
) {

}
