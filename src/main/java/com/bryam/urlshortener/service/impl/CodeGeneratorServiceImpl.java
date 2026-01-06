package com.bryam.urlshortener.service.impl;

import org.springframework.stereotype.Service;

import com.bryam.urlshortener.repository.UrlRepository;
import com.bryam.urlshortener.service.CodeGeneratorService;
import com.bryam.urlshortener.util.Base62Util;
import com.bryam.urlshortener.util.HashUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CodeGeneratorServiceImpl implements CodeGeneratorService {

    private final UrlRepository urlRepository;
    
    private static final int INITIAL_LENGTH = 7;
    private static final int MAX_LENGTH = 12;
    private static final int MAX_COLLISION_ATTEMPTS = 5;

    @Override
    public String[] generateCodeHash(String originalUrl) {
        log.debug("Generating hash code for URL: {}", originalUrl);

        //Generar el hash completo de 64 caracteres
        String fullHash = HashUtil.generateHash(originalUrl);

        //Recortar longitud de hash para encontrar un codigo unico
        for(int length = INITIAL_LENGTH; length <= MAX_LENGTH; length++) {
            String shortCode = HashUtil.extractCode(fullHash, length);

            //Verficar si el codigo ya existe para que no haya colisiones
            if (!urlRepository.existsByShortCode(shortCode)) {
                log.debug("Generated unique code: {} for URL: {}", shortCode, originalUrl);
                return new String[] {shortCode, fullHash};
            }
            //Si hay colision, se intenta con un codigo mas largo y se registar en un log
            log.debug("Collision detected in length {} for code {}. Trying longer code.", length, shortCode);
        }
        //Se captura el error con un log
        log.error("A unique code could not be generated after {} attempts", MAX_COLLISION_ATTEMPTS);
        //Se lanza una excepcion para detener el hilo
        throw new IllegalStateException("Could not generate unique code after maximum attempts");
    }

    @Override
    public String generateCodeBase62(Long id) {
        //Validacion de que el id no sea nulo o negativo
        if (id == null || id <= 0) {
            // Detener el hilo con una excepcion
            throw new IllegalArgumentException("ID must be a positive non-null value");
        }
        //Generacion del codigo en base 62 usando la utilidad Base62Util
        String code = Base62Util.encode(id);
        log.debug("code Base62 generated: {} for ID: {}", code, id);
        return code;
    }

    @Override
    public boolean isValidCode(String code) {
        //Validar que el codigo no sea nulo o vacio
        if (code == null || code.isEmpty()) {
            return false;
        }

        //Validar que el codigo generado este dentro del rango
        if (code.length() < 1 || code.length() > 12) {
            return false;
        }

        //Validar que el codigo solo contenga caracteres validos en base62 y retornar
        return code.matches("^[a-zA-Z0-9-]+$");
    }

    @Override
    public int calculateBaseLength62(Long id) {
        //Calculo de la longitud del codigo en base 62 usando la utilidad Base62Util
        return Base62Util.calculateLength(id);
    }

}
