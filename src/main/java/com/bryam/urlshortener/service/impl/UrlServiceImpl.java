package com.bryam.urlshortener.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bryam.urlshortener.dto.request.ShortenUrlRequestDTO;
import com.bryam.urlshortener.dto.response.ShortenUrlResponseDTO;
import com.bryam.urlshortener.exception.CodeExistsException;
import com.bryam.urlshortener.exception.UrlExpiredException;
import com.bryam.urlshortener.exception.UrlNotFoundException;
import com.bryam.urlshortener.model.Url;
import com.bryam.urlshortener.model.enums.StateUrl;
import com.bryam.urlshortener.model.enums.TypeUrl;
import com.bryam.urlshortener.repository.UrlRepository;
import com.bryam.urlshortener.service.CodeGeneratorService;
import com.bryam.urlshortener.service.RateLimitService;
import com.bryam.urlshortener.service.UrlService;
import com.bryam.urlshortener.service.ValidatorUrlService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final ValidatorUrlService validatorUrlService;
    private final RateLimitService rateLimitService;

    private int daysExpiration = 7;
    private String baseUrl = "http://localhost:8080";

    @Override
    @Transactional
    public ShortenUrlResponseDTO shortenAnonymousUrl(
            ShortenUrlRequestDTO requestDTO,
            HttpServletRequest httpServletResponse) {

        log.info("Initiating anonymous URL shortening: {}", requestDTO.getOriginalUrlRequest());

        // Verificar el limite del usuario anónimo
        rateLimitService.verifyAnonymousLimit(httpServletResponse);

        // Validar y normalizar Url
        validatorUrlService.validateUrlFormat(requestDTO.getOriginalUrlRequest());
        String normalizedUrl = validatorUrlService.normalizeUrl(requestDTO.getOriginalUrlRequest());

        // Generar el código Hash
        String[] hashData = codeGeneratorService.generateCodeHash(normalizedUrl);
        String hashComplete = hashData[0];
        String shortCode = hashData[1];

        log.debug("Generated Hash - Full: {}, Short: {}", hashComplete, shortCode);

        // Se verifica que el hash completo ya exista
        Optional<Url> existingURLByHash = urlRepository.findByFullHash(hashComplete);

        // Validar que la url este presente
        if (existingURLByHash.isPresent()) {
            Url urlExisting = existingURLByHash.get();
            log.debug("URL with same hash found - Status: {}", urlExisting.getStateUrl());

            // Verificar estado, si se encuentra activa retornarla
            if (urlExisting.getStateUrl() == StateUrl.ACTIVE) {
                log.info("The URL already exists and is active, reusing: {}", shortCode);
                return convertADTO(urlExisting);
            }

            // Si la url de usuarios anónimos se encuentra desactivada, reactivarla
            if (urlExisting.getStateUrl() == StateUrl.EXPIRED) {
                log.info("Found expired URL, reactivating: {}", shortCode);
                reactivateUrl(urlExisting);
                return convertADTO(urlExisting);
            }

            // Ver si la url esta eliminada, no se puede reutilizar
            if (urlExisting.getStateUrl() == StateUrl.DELETED) {
                log.warn("Attempt to reuse deleted URL: {}", shortCode);
                throw new CodeExistsException("This URL has been permanently removed", shortCode, false);
            }
        }

        // No existe la url para usuarios anónimos crearla
        Url newUrl = createNewAnonymousUrl(normalizedUrl, hashComplete, shortCode);
        log.info("New anonymous URL created: {} → {}", shortCode, normalizedUrl);
        return convertADTO(newUrl);
    }

    @Override
    @Transactional
    public ShortenUrlResponseDTO shortenRegisteredUrl(ShortenUrlRequestDTO requestDTO, Long userId) {

        log.info("Starting URL shortening for registered user: {}", userId);

        // Verificar el Rate Limit de un usuario o su limite
        rateLimitService.verifyUserLimit(userId);

        // Validar url y normalizar
        validatorUrlService.validateUrlFormat(requestDTO.getOriginalUrlRequest());
        String normalizedUrl = validatorUrlService.normalizeUrl(requestDTO.getOriginalUrlRequest());

        // Determinar el slug o dominio personalizado
        String customSlug = requestDTO.getCustomSlugRequest();
        boolean isPerzonalized = customSlug != null && !customSlug.trim().isEmpty();

        String shortCode;

        // Validar para que el usuario utilice el slug personalizado
        if (isPerzonalized && customSlug != null) {
            validatorUrlService.validateSlug(customSlug);

            shortCode = customSlug.toLowerCase();

            // Verificar que no exista
            if (urlRepository.existsByShortCode(shortCode)) {
                log.warn("Custom slug already exists: {}", shortCode);
                throw new CodeExistsException(
                        "The slug '" + shortCode + "' is already in use",
                        shortCode,
                        false);
            }

            log.debug("Using custom slug: {}", shortCode);

        } else {
            // Generar código corto desde la base62
            // Primero se debe guarda para obtener el id
            Url temporaryUrl = Url.builder()
                    .originalUrl(normalizedUrl)
                    .typeUrl(TypeUrl.REGISTERED)
                    .stateUrl(StateUrl.ACTIVE)
                    .userId(userId)
                    .isPerzonalized(false)
                    .fullHash(null)
                    .build();

            temporaryUrl = urlRepository.save(temporaryUrl);

            // Generar el base62 a partir del id
            shortCode = codeGeneratorService.generateCodeBase62(temporaryUrl.getId());
            temporaryUrl.setShortCode(shortCode);
            urlRepository.save(temporaryUrl);

            log.debug("Generated Base62 code: {} (ID: {})", shortCode, temporaryUrl.getId());

            return convertADTO(temporaryUrl);
        }

        // Crear una url con slug o dominio personalizado
        Url newUrl = createNewRegisteredUrl(normalizedUrl, shortCode, userId, isPerzonalized);
        log.info("New registered URL created: {} → {}", shortCode, normalizedUrl);

        return convertADTO(newUrl);
    }

    @Override
    @Transactional
    public Url getUrlForRedirection(String shortCode) {

        log.debug("Looking for URL for redirection: {}", shortCode);

        // Buscar por el código corto
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.warn("URL not found by shortcode: {}", shortCode);
                    return new UrlNotFoundException("URL not found: " + shortCode);
                });

        // Verificar el estado de la url
        if (url.getStateUrl() == StateUrl.DELETED) {
            log.warn("Attempt to access deleted URL {}", shortCode);
            throw new UrlNotFoundException("This URL has been removed");
        }

        // Verificar si la url esta expirada para los usuarios anónimos
        if (url.getTypeUrl() == TypeUrl.ANONYMOUS) {
            if (url.getStateUrl() == StateUrl.EXPIRED ||
                    (url.getExpirationDateTime() != null
                            && url.getExpirationDateTime().isBefore(LocalDateTime.now()))) {

                // Marcar la url como expirada
                if (url.getStateUrl() != StateUrl.EXPIRED) {
                    url.setStateUrl(StateUrl.EXPIRED);
                    urlRepository.save(url);
                }

                log.info("Accessed expired URL: {} ", shortCode);
                throw new UrlExpiredException("This link expired on" + url.getExpirationDateTime().toLocalDate(),
                        url.getExpirationDateTime(), true);
            }
        }

        // Incrementar los contadores de los clicks
        url.setCounterClicksTotal(url.getCounterClicksTotal() + 1);

        if (url.getCounterClicksSession() != null) {
            url.setCounterClicksSession(url.getCounterClicksSession() + 1);
        }

        urlRepository.save(url);

        log.info("Successful redirection: {} → {} (total clicks: {})",
                shortCode,
                url.getOriginalUrl(),
                url.getCounterClicksTotal());

        return url;
    }

    @Override
    @Transactional
    public List<ShortenUrlResponseDTO> getUserUrls(Long userId) {

        log.debug("Obtaining URLs from the user: {}", userId);

        List<Url> urls = urlRepository.findByUserIdOrderByCreationDateTimeDesc(userId);

        return urls.stream()
                .map(this::convertADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUrl(String shortCode, Long userId) {

        log.info("Removing URLs: {} by user: {}", shortCode, userId);

        // Buscar url
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found:" + shortCode));

        // Validar que sea del usuario la url
        if (!url.getUserId().equals(userId)) {
            log.warn("User {} attempted to delete URL that does not belong to them: {}", userId, shortCode);
            throw new IllegalArgumentException("You do not have permission to delete this URL");
        }

        // Marcar la url como eliminada técnica soft delete
        url.setStateUrl(StateUrl.DELETED);
        urlRepository.save(url);

        log.info("URL marked as deleted: {}", shortCode);
    }

    @Override
    @Transactional
    public void updateDestinationUrl(String shortCode, String newUrl, Long userId) {

        log.info("Updating URL destination: {} by user: {}", shortCode, userId);

        // Validar la nueva url
        validatorUrlService.validateUrlFormat(newUrl);
        String normalizedUrl = validatorUrlService.normalizeUrl(newUrl);

        // Buscar la url existente
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found:" + shortCode));

        if (!url.getUserId().equals(userId)) {
            log.warn("User {} attempted to update a URL that does not belong to them: {}", userId, shortCode);
            throw new IllegalArgumentException("You do not have permission to update this URL");
        }

        // Actualizar la url
        url.setOriginalUrl(normalizedUrl);
        urlRepository.save(url);

        log.info("Updated URL: {} → {}", shortCode, normalizedUrl);
    }

    // Métodos auxiliares para la construcción de los objetos con builder

    // Convertir una clase Url a DTO
    private ShortenUrlResponseDTO convertADTO(Url url) {
        return ShortenUrlResponseDTO.builder()
                .shortUrlResponse(baseUrl + "/" + url.getShortCode())
                .urlCodeResponse(url.getShortCode())
                .urlOriginalResponse(url.getOriginalUrl())
                .createDateTimeResponse(url.getCreationDateTime())
                .expirationDateTimeResponse(url.getExpirationDateTime())
                .isPersonalizedResponse(url.getIsPerzonalized())
                .reusedResponse(url.getTimesReactivated() > 0)
                .timesReactivatedResponse(url.getTimesReactivated())
                .counterClicksTotalResponse(url.getCounterClicksTotal())
                .build();
    }

    // Reactivar una url expirada para usuarios anónimos
    private void reactivateUrl(Url url) {
        // Buscar o investigar otra forma de implementar para que sea inmutable
        url.setStateUrl(StateUrl.ACTIVE);
        url.setLastActivationDateTime(LocalDateTime.now());
        url.setExpirationDateTime(LocalDateTime.now().plusDays(daysExpiration));
        url.setCounterClicksSession(0);
        url.setTimesReactivated(url.getTimesReactivated() + 1);

        urlRepository.save(url);

        log.info("Reactivated URL: {} (reactivation's: {})", url.getShortCode(), url.getTimesReactivated());
    }

    private Url createNewAnonymousUrl(String urlOriginal, String hashComplete, String shortCode) {

        Url url = Url.builder()
                .shortCode(shortCode)
                .fullHash(hashComplete)
                .originalUrl(urlOriginal)
                .typeUrl(TypeUrl.ANONYMOUS)
                .stateUrl(StateUrl.ACTIVE)
                .expirationDateTime(LocalDateTime.now().plusDays(daysExpiration))
                .userId(null)
                .counterClicksTotal(0)
                .counterClicksSession(0)
                .timesReactivated(0)
                .isPerzonalized(false)
                .build();

        return urlRepository.save(url);
    }

    private Url createNewRegisteredUrl(String originalUrl, String shortCode, Long userId, boolean isPerzonalized) {

        Url url = Url.builder()
                .shortCode(shortCode)
                .fullHash(null)
                .originalUrl(originalUrl)
                .typeUrl(TypeUrl.REGISTERED)
                .stateUrl(StateUrl.ACTIVE)
                .expirationDateTime(null)
                .userId(userId)
                .counterClicksTotal(0)
                .counterClicksSession(0)
                .timesReactivated(0)
                .isPerzonalized(isPerzonalized)
                .build();

        return urlRepository.save(url);
    }

}
