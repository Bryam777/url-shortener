package com.bryam.urlshortener.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponseDTO {

    private LocalDateTime timestamp; //Captura la fecha y hora del error
    private int status;              // Código HTTP
    private String error;            // Tipo de error
    private String message;          // Mensaje descriptivo
    private String path;             // Endpoint donde ocurrió el error o pwd
}
