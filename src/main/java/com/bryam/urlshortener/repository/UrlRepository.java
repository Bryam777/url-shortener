package com.bryam.urlshortener.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bryam.urlshortener.model.Url;
import com.bryam.urlshortener.model.enums.StateUrl;
import com.bryam.urlshortener.model.enums.TypeUrl;


@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    //Buscar una Url por su codido corto
    Optional<Url> findByShortCode(String shortCode);

    //Buscar una Url por su codigo corto y estado
    Optional<Url> findByShortCodeAndStateUrl(String shortCode, StateUrl stateUrl);

    //Buscar hash competo
    Optional<Url> findByFullHash(String fullHash);

    //Verificar si existe una Url por su codigo corto
    boolean existsByShortCode(String shortCode);

    //Verificar si existe una url por su hash completo
    boolean existsByFullHash(String fullHash);

    //Buscar Urls expiradas para marcalas
    List<Url> findByTypeUrlAndStateUrlAndExpirationDateBefore(TypeUrl typeUrl, StateUrl stateUrl, LocalDateTime dateTime);

    //Eliminar Urls expiradas
    @Modifying
    @Query("DELETE FROM Url u WHERE u.stateUrl = :stateUrl AND u.expirationDate < :dateTime")
    void deleteByStateUrlAndExpirationDateBefore(
        @Param("stateUrl") StateUrl stateUrl, 
        @Param("dateTime") LocalDateTime dateTime
    );

    // Para limpieza fisica selectiva
    List<Url> findByStateUrlAndExpirationDateTimeBeforeAndCounterClicksTotalLessThan(
        StateUrl stareUrlRepo,
        LocalDateTime dateTimeRepo,
        Integer counterClicksTotalRepo
    );

    @Query("SELECT u FROM Url u WHERE u.TimesReactivated > 0 ORDER BY u.timesReactivated DESC")
    List<Url> findUrlsMoreReused(Pageable pageable);

}
