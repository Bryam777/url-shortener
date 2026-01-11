package com.bryam.urlshortener.service.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bryam.urlshortener.exception.InvalidUrlException;
import com.bryam.urlshortener.service.ValidatorUrlService;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ValidatorUrlServiceImpl implements ValidatorUrlService{

    //Obtener el valor del dominio base desde application.properties
    @Value("${app.base-url}")
    private String baseUrl;
    
    //Lista de dominios bloqueados
    private static final Set<String> BLOCKED_DOMAINS = Set.of(
        "malicious.com",
        "phishing.com",
        "spamdomain.com",
        "spam-site.com"
    );

    //Lista de palabras reservadas o bloqueadas
    private static final Set<String> FORBIDDEN_WORDS = Set.of(
        "api", "admin", "login", "register", "health",
        "swagger", "docs", "actuator", "management"
    );



    @Override
    public void validateUrlFormat(String url) {
        //Validación de que la url no sea vacía o nula, si lo esta detener el hilo con una excepción
        if (url == null || url.trim().isEmpty()) {
            throw new InvalidUrlException("The URL cannot be empty.");
        }

        //Validación que tenga un formato valido, si es diferente detener el hilo
        if (!url.matches("^https?://.*")) {
            throw new InvalidUrlException("The URL must start with http:// or https://");
            
        }

        //Validar que sea una longitud maxima de 2048 caracteres, si es mayor a lo permitido detener el hilo
        if (url.length() > 2048) {
            throw new InvalidUrlException( "The URL is too long maximum 2048 characters");
        }

        //Validación del formato con java.net.URL
        try {
            
            // Crear el URI 
            URI uriObj = new URI(url);
            // Convertir el URI a URL
            URL urlObj = uriObj.toURL();

            //Validar que tenga el host, obteniéndolo del objeto Url
            //Validar que el host no sea vació o nulo, si lo es detener el hilo
            if (urlObj.getHost() == null || urlObj.getHost().isEmpty()) {
                throw new InvalidUrlException("The URL does not have a valid domain.");
            }

            //Evitar acortar las ulr del mismo servicio o dominio
            if (url.startsWith(baseUrl)) {
                throw new InvalidUrlException("You cannot shorten URLs from this same domain.");
            }

            //Verificar que el dominio no este en la lista de dominios bloqueados
            String host = urlObj.getHost().toLowerCase();
            if (BLOCKED_DOMAINS.contains(host)) {
                throw new InvalidUrlException("This domain is locked for security reasons.");
            }

            //Validar que la url no contenga palabras reservadas
            String path = urlObj.getPath().toLowerCase();
            for (String word : FORBIDDEN_WORDS) {
                if (path.contains("/" + word) || host.contains(word)) {
                    throw new InvalidUrlException("The URL contains restricted words.");
                }
            }

            //Si pasa todas las validaciones registrar en el log
            log.debug("URL successfully validated: {}", url);

        } catch (MalformedURLException  e) {
            throw new InvalidUrlException("Invalid URL format: " + e.getMessage());

        } catch (URISyntaxException  e) {
            throw new InvalidUrlException("The URL format is invalid: " + e.getMessage());
        }
    }

    @Override
    public String normalizeUrl(String url) {
        // Validar si la URL es nula
        if (url == null) {
            return null;
        }

        // Normalizar la URL eliminando espacios en blanco y asegurando un formato consistente
        try {
            //Instanciando URI para acceder a las diferentes partes de la URL
            URI uri = new URI(url.trim());

            //Validar con ternarios las diferentes expresiones para reconstruir la url o normalizar
            String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase() : "http";
            String host = uri.getHost() != null ? uri.getHost().toLowerCase() : "";
            int port = uri.getPort();
            String path = uri.getPath() != null ? uri.getPath() : "";
            String query = uri.getQuery() != null ? "?" + uri.getQuery() : "";

            // Reconstruir la URL normalizada
            StringBuilder normalizedUrl = new StringBuilder();
            normalizedUrl.append(scheme).append("://").append(host);

            //Verificar si la URL tiene un puerto especifico
            if (port != -1 && port != uri.toURL().getDefaultPort()) {
                normalizedUrl.append(":").append(port);
            }
            
            //URL normalizada
            normalizedUrl.append(path).append(query);

            //Registrar la correcta normalización en el log
            log.debug("Normalized URL: {} -> {}", url, normalizedUrl);
            //Ratonar la URL normalizada, y convertida a String
            return normalizedUrl.toString();

            
        } catch (URISyntaxException | MalformedURLException e) {
            //Registrar la falla en log y retornar devuelta la url original
                log.warn("URL could not be normalized: {}", url);
            return url;
        }
    }

    @Override
    public void validateSlug(String slug) {

        //Validar que el slug no sea nulo o vació, si lo es detener el hilo
        if (slug == null || slug.trim().isEmpty()) {
            throw new InvalidUrlException("The slug cannot be empty");
        }

        //Validar el máximo y mínimo de caracteres del slug, si no se cumple detener el hilo
        if (slug.length() < 3 || slug.length() > 50)  {
            throw new InvalidUrlException("The slug must be between 3 and 50 characters long");
        }

        //Validar que el slug no contenga caracteres especialicen si lo contiene detener el hilo
        if (!slug.matches("^[a-zA-Z0-9-]+$")) {
            throw new InvalidUrlException("The slug can only contain letters, numbers, and hyphens.");
        }

        //Validar que el slug no empiece y termine con guiones
        if (slug.startsWith("-") || slug.endsWith("-")) {
            throw new InvalidUrlException("A slug cannot begin or end with a hyphen.");
        }

        //Validar que el slug contenga las palabras prohibidas
        String slugLower = slug.toLowerCase();
        if (FORBIDDEN_WORDS.contains(slugLower)) {
            throw new InvalidUrlException(
                "The slug '" + slug + "' It is reserved and cannot be used.");
        }

        log.debug("Slug successfully validated: {}", slug);
    }

}
