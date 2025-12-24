package com.bryam.urlshortener.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShortenUrlResponseDTO {

    private String shortUrlResponse;           // URL completa acortada
    private String urlCodeResponse;             // Solo el código (para facilitar copia)
    private String urlOriginalResponse;        // URL original
    private LocalDateTime createDateTimeResponse;
    private LocalDateTime expirationDateTimeResponse;  // null para usuarios registrados
    private Boolean isPersonalizedResponse;             // true si tiene slug custom
    private Boolean reusedResponse;
    private Integer timesReactivatedResponse;
    private Integer counterClicksTotalResponse;    // Para usuarios registrados
}


