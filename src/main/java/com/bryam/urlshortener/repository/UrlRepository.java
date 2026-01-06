package com.bryam.urlshortener.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
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
    List<Url> findByTypeUrlAndStateUrlAndExpirationDateTimeBefore(TypeUrl typeUrl, StateUrl stateUrl, LocalDateTime dateTime);

    //Eliminar Urls expiradas
    @Modifying
    @Query("DELETE FROM Url u WHERE u.stateUrl = :stateUrl AND u.expirationDate < :dateTime")
    void deleteByStateUrlAndExpirationDateTimeBefore(
        @Param("stateUrl") StateUrl stateUrl, 
        @Param("dateTime") LocalDateTime dateTime
    );

    // Para limpieza fisica selectiva
    List<Url> findByStateUrlAndExpirationDateTimeBeforeAndCounterClicksTotalLessThan(
        StateUrl stareUrlRepo,
        LocalDateTime dateTimeRepo,
        Integer counterClicksTotalRepo
    );

    //Traer urls que han sido activadas, se utiliza Pageable, para que lo traiga por pagina
    @Query("SELECT u FROM Url u WHERE u.TimesReactivated > 0 ORDER BY u.timesReactivated DESC")
    Page<Url> findUrlsMoreReused(Pageable pageable);

    // Listar todas las URLs de un usuario (para dashboard)
    List<Url> findByUserIdOrderByCreationDateTimeDesc(Long userId);
    
    // Contar URLs creadas por un usuario hoy
    long countByUserIdAndCreationDateTimeAfter(Long userId, LocalDateTime date);
    
    // Contar URLs creadas por un usuario en total
    long countByUserId(Long userId);
    
    // Buscar URL por código Y usuario (para verificar propiedad)
    Optional<Url> findByShortCodeAndUserId(String shortCode, Long userId);
    
    // Eliminar URL por código Y usuario (para que solo el dueño pueda eliminar)
    @Modifying
    @Query("DELETE FROM Url u WHERE u.shortCode = :shortCode AND u.userId = :userId")
    void deleteByShortCodeAndUserId(
        @Param("shortCode") String shortCode, 
        @Param("userId") Long userId
    );
    
    // Total de URLs en el sistema
    @Query("SELECT COUNT(u) FROM Url u WHERE u.stateUrl = :stateUrl")
    long countByStateUrl(@Param("stateUrl") StateUrl stateUrl);
    
    // URLs más clickeadas o mas usadas top 10
    List<Url> findTop10ByOrderByCounterClicksTotalDesc();
    
    // Total de clicks en todas las URLs
    @Query("SELECT SUM(u.contadorClicks) FROM Url u")
    Long sumAllCounterClicksTotal();

}
