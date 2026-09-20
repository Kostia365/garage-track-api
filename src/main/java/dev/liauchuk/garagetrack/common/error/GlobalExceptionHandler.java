package dev.liauchuk.garagetrack.common.error;

import dev.liauchuk.garagetrack.vehicle.exception.VehicleMileageCannotBeDecreasedException;
import dev.liauchuk.garagetrack.vehicle.exception.VehicleNotFoundException;
import dev.liauchuk.garagetrack.vehicle.exception.VehicleVinAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(VehicleNotFoundException.class)
  public ResponseEntity<ApiErrorResponseDto> handleVehicleNotFoundException(VehicleNotFoundException e, HttpServletRequest request) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    ApiErrorResponseDto body = new ApiErrorResponseDto(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        e.getMessage(),
        request.getRequestURI()
    );
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(VehicleVinAlreadyExistsException.class)
  public ResponseEntity<ApiErrorResponseDto> handleVehicleVinAlreadyExistsException(VehicleVinAlreadyExistsException e, HttpServletRequest request) {
    HttpStatus status = HttpStatus.CONFLICT;
    ApiErrorResponseDto body = new ApiErrorResponseDto(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        e.getMessage(),
        request.getRequestURI()
    );
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String message = e.getBindingResult().getFieldErrors().stream().map(fieldError -> fieldError.getField() + ":" + fieldError.getDefaultMessage()).distinct().sorted().collect(Collectors.joining("; "));
    ApiErrorResponseDto body = new ApiErrorResponseDto(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI()
    );
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(VehicleMileageCannotBeDecreasedException.class)
  public ResponseEntity<ApiErrorResponseDto> handleMileageCannotBeDecreased(
      VehicleMileageCannotBeDecreasedException exception,
      HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.CONFLICT;

    ApiErrorResponseDto body = new ApiErrorResponseDto(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        exception.getMessage(),
        request.getRequestURI()
    );

    return ResponseEntity
        .status(status)
        .body(body);
  }

}
