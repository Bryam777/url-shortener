package com.bryam.urlshortener.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tests of HashUtil")
public class HashUtilTest {

    // Test para verificar que el hash generado tenga la longitud de 64 caracteres y
    // que no sea null lo que devuelve
    @Test
    @DisplayName("Test of the hash SHA-256 of characters")
    void testGeneratedHash_lengthCorrect() {

        // Arrange
        String url = "https://www.youtube.com/watch?v=1IgOZaQqB58&list=RD1IgOZaQqB58&start_radio=1";

        // Act
        String hash = HashUtil.generateHash(url);

        // Assert
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    // Test para verificar que la misma url genere el mismo hash
    @Test
    @DisplayName("Try generating the same hash for the same URL")
    void testGeneratedHash_sameUrlSameHash() {

        // Arrange
        String url = "https://www.youtube.com/watch?v=1IgOZaQqB58&list=RD1IgOZaQqB58&start_radio=1";

        // Act
        String hash1 = HashUtil.generateHash(url);
        String hash2 = HashUtil.generateHash(url);

        // Assert
        assertEquals(hash1, hash2);
    }

    // Test para verificar que diferentes urls generan diferentes hash
    @Test
    @DisplayName("Test to generate different hashes for different URLs")
    void testGeneratedHash_differentUrlsDifferentHashes() {

        // Arrange
        String url1 = "https://www.youtube.com/watch?v=1IgOZaQqB58&list=RD1IgOZaQqB58&start_radio=1";
        String url2 = "https://www.youtube.com/watch?v=av3wkasS-WQ&list=RDav3wkasS-WQ&start_radio=1";

        // Act
        String hash1 = HashUtil.generateHash(url1);
        String hash2 = HashUtil.generateHash(url2);

        // Assert
        assertNotEquals(hash1, hash2);
    }

    // Test para verificar que solo contenga caracteres hexadecimales en el hash
    // generado
    @Test
    @DisplayName("test to verify that it only has hexadecimal characters in the generated hash")
    void testGeneratedHahs_onlyHexadecimalCharacters() {

        // Arrange
        String url = "https://www.youtube.com/watch?v=1IgOZaQqB58&list=RD1IgOZaQqB58&start_radio=1";

        // Act
        String hash = HashUtil.generateHash(url);

        // Assert
        assertTrue(hash.matches("^[a-f0-9]{64}$"));
    }

    // Test para verificar que se lance una excepción cuando el valor es nulo
    @Test
    @DisplayName("test to throw an exception when it is null")
    void testGeneratedHash_nullValue() {

        // Arrange
        String url = null;

        // Act y Assert
        assertThrows(IllegalArgumentException.class, () -> HashUtil.generateHash(url));
    }

    // Test para verificar que se lance una excepción cuando el valor es una cadena
    // vacía
    @Test
    @DisplayName("Test to throw an exception when the string is empty")
    void testGeneratedHash_emptyString() {
        // Arrange
        String url1 = "";
        String url2 = " ";

        // Act y Assert
        assertThrows(IllegalArgumentException.class, () -> HashUtil.generateHash(url1));

        assertThrows(IllegalArgumentException.class, () -> HashUtil.generateHash(url2));
    }

    // Test para verificar que si se extrae el código o 7 caracteres del hash
    // completo
    @Test
    @DisplayName("test to extract the code from the hash")
    void testExtractCodeFromHash() {

        // Arrange
        String hashComplete = "a3f5b9c2d8e1f4a7b6c9d2e5f8a1b4c7e3f6a9b2c5d8e1f4a7b6c9d2e5f8a1b4";

        // Act
        String expectedCode = HashUtil.extractCode(hashComplete, 7);

        // Assert
        assertEquals("a3f5b9c", expectedCode);
    }

    // Test para verificar que se lance una excepción cuando se intente extraer un
    // código mayor a la longitud requerida
    @Test
    @DisplayName("Test to throw an exception if the length exceeds the hash")
    void testExtractCodeFromHash_LengthExceedsHash() {

        // Arrange
        String hashComplete = "abc1234";

        // Act y Assert
        assertThrows(IllegalArgumentException.class, () -> HashUtil.extractCode(hashComplete, 10));
    }

    // Test para verificar que genere un código de longitud 7 directamente
    @Test
    @DisplayName("Test to generate code directly with length 7")
    void testGenerateCode_directlyWithLength() {

        // Arrange
        String url = "https://www.youtube.com/watch?v=1IgOZaQqB58&list=RD1IgOZaQqB58&start_radio=1";

        // Act
        String code = HashUtil.extractCode(url, 7);

        // Assert
        assertNotNull(code);
        assertEquals(7, code.length());
    }

    // Test para verificar las concurrencias al generar el hash de una url
    @Test
    @DisplayName("test for concurrency")
    void testConcurrency() throws InterruptedException {

        // Arrange
        String url = "https://www.youtube.com/watch?v=1IgOZaQqB58&list=RD1IgOZaQqB58&start_radio=1";
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        String[] hashes = new String[threadCount];

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                hashes[index] = HashUtil.generateHash(url);
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Assert
        for (String hash : hashes) {
            assertNotNull(hash);
            assertEquals(64, hash.length());
            assertEquals(hashes[0], hash);
        }
    }

}
