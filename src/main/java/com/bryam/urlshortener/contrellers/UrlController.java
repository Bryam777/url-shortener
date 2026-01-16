package com.bryam.urlshortener.contrellers;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.bryam.urlshortener.dto.request.ShortenUrlRequestDTO;
import com.bryam.urlshortener.dto.response.ShortenUrlResponseDTO;
import com.bryam.urlshortener.model.Url;
import com.bryam.urlshortener.service.UrlService;
import com.bryam.urlshortener.util.IpUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UrlController {

        private final UrlService urlService;

        // Acortar la url para un usuario anónimo
        @PostMapping("/api/shorten")
        public ResponseEntity<ShortenUrlResponseDTO> shortenUrlAnonymous(
                        @Valid @RequestBody ShortenUrlRequestDTO request, HttpServletRequest httpRequest) {

                String ip = IpUtil.getClientIpAdress(httpRequest);
                log.info("URL shortening request from IP: {} - URL: {}", IpUtil.obfuscateIP(ip),
                                request.getOriginalUrlRequest());

                ShortenUrlResponseDTO responseDTO = urlService.shortenAnonymousUrl(request, httpRequest);

                log.info("URL successfully shortened: {} → {}", responseDTO.getUrlOriginalResponse(),
                                responseDTO.getShortUrlResponse());

                return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        }

        // Redirigir a la URL original a partir del código corto(shortCode)
        @GetMapping("{code }")
        @SuppressWarnings("null")
        public ResponseEntity<ShortenUrlResponseDTO> redirect(@PathVariable String code) {

                log.info("Redirection request for code: {}", code);

                Url url = urlService.getUrlForRedirection(code);

                log.info("Redirecting {} → {} (clicks: {})", url.getShortCode(), url.getOriginalUrl(),
                                url.getCounterClicksTotal());

                // Ocurre la magia de redirigir de una url acortada
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url.getOriginalUrl())).build();
        }

        // Acortar una ulr de un usuario registrado
        @PostMapping("/api/shorten/authenticated")
        public ResponseEntity<ShortenUrlResponseDTO> shortenAuthenticatedUrl(
                        @Valid @RequestBody ShortenUrlRequestDTO requestDTO, @RequestHeader("X-User-Id") Long userId) {

                log.info("Authenticated URL shortening request - User: {} - URL: {}", userId,
                                requestDTO.getOriginalUrlRequest());

                ShortenUrlResponseDTO responseDTO = urlService.shortenRegisteredUrl(requestDTO, userId);

                log.info("URL registrada acortada: {} → {}", requestDTO.getOriginalUrlRequest(),
                                responseDTO.getUrlCodeResponse());

                return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        }

        // Obtener todas las urls de un usuario registrado
        @GetMapping("/api/my-links")
        public ResponseEntity<List<ShortenUrlResponseDTO>> getUrlRegisteredUser(
                        @RequestHeader("X-User-Id") Long userId) {

                log.info("Obtaining URLs from the user: {}", userId);

                List<ShortenUrlResponseDTO> urls = urlService.getUserUrls(userId);

                return ResponseEntity.ok(urls);
        }

        // Eliminar una url de un usuario registrado
        @DeleteMapping("/api/links/{code}")
        public ResponseEntity<Void> deleteUrl(@PathVariable String code, @RequestHeader("X-User-Id") Long userId) {

                log.info("Removing URLs: {} by user: {}", code, userId);

                urlService.deleteUrl(code, userId);

                log.info("URL successfully removed: {}", code);

                return ResponseEntity.noContent().build();
        }

        // Actualizar el destino de una url
        @PutMapping("/api/links/{code}")
        public ResponseEntity<Void> updateUrlUser(@PathVariable String code,
                        @Valid @RequestBody ShortenUrlRequestDTO requestDTO, @RequestHeader("X-User-Id") Long userId) {

                log.info("Updating URL: {} by user: {} - New URL: {}", code, userId,
                                requestDTO.getOriginalUrlRequest());

                urlService.updateDestinationUrl(code, requestDTO.getOriginalUrlRequest(), userId);

                log.info("URL successfully updated: {}", code);

                return ResponseEntity.noContent().build();
        }

        // Estadísticas básicas para una Url
        @GetMapping("/api/links/{code}/stats")
        public ResponseEntity<ShortenUrlResponseDTO> getStatistics(@PathVariable String code,
                        @RequestHeader("X-User-Id") Long userId) {

                log.info("Obtaining URL statistics: {} per user: {}", code, userId);

                // En el momento solo se obtendrá información básica
                List<ShortenUrlResponseDTO> urls = urlService.getUserUrls(userId);

                ShortenUrlResponseDTO url = urls.stream()
                                .filter(u -> u.getShortUrlResponse().equals(code))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("URL not found"));

                return ResponseEntity.ok(url);
        }
}
