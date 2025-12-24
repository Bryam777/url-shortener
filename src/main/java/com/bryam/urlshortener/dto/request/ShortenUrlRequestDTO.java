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

    @NotBlank( message = "URL is required")
    @Size( max = 2048, message = "URL must not exceed 2048 characters")
    @Pattern( regexp = "^(https?://).+", message = "URL must start with http:// or https://")
    private String originalUrlRequest;

    @Size(max= 50, message = "The custom slug cannot exceed 50 characters")
    @Pattern( regexp = "^[a-zA-Z0-9-]*$", message = "The custom slug can only contain alphanumeric characters and hyphens")
    private String customSlugRequest;
}
