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

    //Verificar si existe una Url por su codigo corto
    boolean existsByShortCode(String shortCode);

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

    /*// ========== MÉTODOS PARA FASE 3 (Usuarios Registrados) ==========
    
    // Listar todas las URLs de un usuario (para dashboard)
    List<Url> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    
    // Contar URLs creadas por un usuario HOY (rate limiting)
    long countByUsuarioIdAndFechaCreacionAfter(Long usuarioId, LocalDateTime fecha);
    
    // Contar URLs creadas por un usuario en total
    long countByUsuarioId(Long usuarioId);
    
    // Buscar URL por código Y usuario (para verificar propiedad)
    Optional<Url> findByCodigoCortoAndUsuarioId(String codigoCorto, Long usuarioId);
    
    // Eliminar URL por código Y usuario (para que solo el dueño pueda eliminar)
    @Modifying
    @Query("DELETE FROM Url u WHERE u.codigoCorto = :codigo AND u.usuarioId = :usuarioId")
    void deleteByCodigoCortoAndUsuarioId(
        @Param("codigo") String codigoCorto, 
        @Param("usuarioId") Long usuarioId
    );
    
    
    // ========== MÉTODOS ESTADÍSTICOS (Opcional - Futuro) ==========
    
    // Total de URLs en el sistema
    @Query("SELECT COUNT(u) FROM Url u WHERE u.estado = :estado")
    long countByEstado(@Param("estado") EstadoUrl estado);
    
    // URLs más clickeadas (top 10)
    List<Url> findTop10ByOrderByContadorClicksDesc();
    
    // Total de clicks en todas las URLs
    @Query("SELECT SUM(u.contadorClicks) FROM Url u")
    Long sumAllClicks();*/
}
