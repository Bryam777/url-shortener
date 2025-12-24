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
public class UrlDetailsUserResponseDTO {

    private String urlCodeUserResponse;
    private String shortUrlUserResponse;
    private String urlOriginalUserResponse;
    private LocalDateTime createDateTimeUserResponse;
    private Integer counterClicksUserResponse;
    private Boolean isPersonalizedUserResponse;

    // Acciones disponibles
    private String urlEditUserResponse;      // "/api/links/{codigo}"
    private String urlRemoveUserResponse;    // "/api/links/{codigo}"
    private String urlStatisticsUserResponse; // "/api/links/{codigo}/stats"
}
