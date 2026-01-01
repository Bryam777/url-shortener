package com.bryam.urlshortener.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.bryam.urlshortener.dto.response.ErrorResponseDTO;

import jakarta.servlet.http.HttpServletRequest;

//Manejo global de excepciones para los contralladores Rest o endpoints
//RestControllerAdvice indica que esta clase maneja excepciones para controladores, es la
//que se encarga de capturar y procesar las excepciones lanzadas por los controladores
@RestControllerAdvice
public class GlobalExceptionHandler {

        //Excepciones personalizadas
    
    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUrlNotFounfdException(
            UrlNotFoundException ex,
            HttpServletRequest request) {
                
                ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponseDTO> handleUrlExpiredException(
        UrlExpiredException ex,
        HttpServletRequest request) {

            // Se obtiene el mensaje de la clase ThroWable
            String detailMessage = ex.getMessage();
            if (ex.isPerzonalized()) {
                detailMessage += "This URL can be reactivated by shortening it again.";
            }

            ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.GONE.value())
                .error(ex.getMessage())
                .message(detailMessage)
                .path(request.getRequestURI())
                .build();

            return ResponseEntity.status(HttpStatus.GONE).body(errorResponse);
            }

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidUrlException(
        InvalidUrlException ex,
        HttpServletRequest request) {

            ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error(ex.getMessage())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
            }

    @ExceptionHandler(CodeExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleCodeExistsException(
        CodeExistsException ex,
        HttpServletRequest request) {
            // Se obtiene el mensaje de la clase ThroWable
            String detailMessage = ex.getMessage();
            if (ex.isReutilizable()) {
                detailMessage += "However, you can reuse it if the previous URL was removed.";
            }
            ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(ex.getMessage())
                .message(detailMessage)
                .path(request.getRequestURI())
                .build();

            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleRateLimitExceededException(
        RateLimitExceededException ex,
        HttpServletRequest request) {

            Long timeMinutes = ex.getTimeoutSeconds() / 60;  // Se combierten los segundos a minutos
            String detailMessage = String.format(           //Un mensaje detallado con formato
                "%s. Limit: %d por %s. Try again on %d minutes.",
                ex.getMessage(),
                ex.getLimit(),
                ex.getPeriod(),
                timeMinutes
            );

            ErrorResponseDTO erroeResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error(ex.getMessage())
                .message(detailMessage)
                .path(request.getRequestURI())
                .build();
            
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(erroeResponse);
        }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
        MethodArgumentNotValidException ex,
        HttpServletRequest request) {

            String detailError = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ":" + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

            ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation failed")
                .message("Validation errors:" + detailError)
                .path(request.getRequestURI())
                .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
        Exception ex,
        HttpServletRequest request) {

            ex.printStackTrace();

            ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred.")
                .path(request.getRequestURI())
                .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        // Futuramente para Url eliminadas

       /* @ExceptionHandler(UrlDeletedException.class)
        public ResponseEntity<ErrorResponseDTO> handleUrlDeletedException(
            UrlDeletedException ex,
            HttpServletRequest request) {

                ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.GONE.value())
                    .error(ex.getMessage())
                    .message(ex.getMessage())
                    .path(request.getRequestURI())
                    .build();

                return ResponseEntity.status(HttpStatus.GONE).body(errorResponse);
            }*/

}