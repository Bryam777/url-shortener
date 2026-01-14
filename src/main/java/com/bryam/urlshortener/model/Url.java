package com.bryam.urlshortener.model;

import java.time.LocalDateTime;

import com.bryam.urlshortener.model.enums.StateUrl;
import com.bryam.urlshortener.model.enums.TypeUrl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "urls")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_url", nullable = false, length = 10)
    private TypeUrl typeUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "state_url", nullable = false, length = 10)
    private StateUrl stateUrl;

    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDateTime;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDateTime;

    @Column(name = "last_activation_date")
    private LocalDateTime lastActivationDateTime;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "full_hash", nullable = true, unique = true, length = 64)
    private String fullHash;

    @Column(name = "counter_clicks_total")
    private Integer counterClicksTotal;

    @Column(name = "counter_clicks_session")
    private Integer counterClicksSession;

    @Column(name = "times_reactivated")
    private Integer timesReactivated;

    @Column(name = "is_perzonalized", nullable = false)
    private Boolean isPerzonalized;

    @PrePersist
    protected void onCreate() {
        creationDateTime = LocalDateTime.now();
        lastActivationDateTime = LocalDateTime.now();

        if (stateUrl == null) {
            stateUrl = StateUrl.ACTIVE;
        }
        if (counterClicksTotal == null) {
            counterClicksTotal = 0;
        }
        if (counterClicksSession == null) {
            counterClicksSession = 0;
        }
        if (timesReactivated == null) {
            timesReactivated = 0;
        }
        if (isPerzonalized == null) {
            isPerzonalized = false;
        }
    }
}
