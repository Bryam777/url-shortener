package com.bryam.urlshortener.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ShortenUrlRequestDTO {

    // Validaciones para la url original
    @NotBlank( message = "URL is required") // Validacion para que la peticion no sea vacia
    @Size( max = 2048, message = "URL must not exceed 2048 characters") // Maximo tamaño URL
    @Pattern( regexp = "^(https?://).+", message = "URL must start with http:// or https://") // Validacion del formato http https
    private String originalUrlRequest;

    // Validaciones para el dominio personalizado
    @Size(max= 50, message = "The custom slug cannot exceed 50 characters") //Maximo 50 caracteres para el dominio personalizado
    @Pattern( regexp = "^[a-zA-Z0-9-]*$", message = "The custom slug can only contain alphanumeric characters and hyphens") 
    private String customSlugRequest; // pattern para evitar caracteres especiales
}
