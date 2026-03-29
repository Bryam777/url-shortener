package com.bryam.urlshortener.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Base62Util Test")
public class Base62UtilTest {

    @ParameterizedTest
    @DisplayName("Test encode and decode consistency")
    @CsvSource({
            "0, 0",
            "1, 1",
            "9, 9",
            "11, B",
            "36, a",
            "50, o",
            "61, z"

    })

    void testFromBase62_encodeConstency(long number, String expectedEncoded) {

        // Act
        String encodedResult = Base62Util.encode(number);

        // Assert
        assertEquals(expectedEncoded, encodedResult);
    }

    @ParameterizedTest
    @DisplayName("Test encode and decode consistency")
    @CsvSource({
            "0, 0",
            "1, 1",
            "9, 9",
            "B, 11",
            "a, 36",
            "b, 37",
            "z, 61"
    })

    void testFromBase62_decodeConsistency(String encoded, long expectedNumber) {

        // Act
        long decodedResult = Base62Util.decode(encoded);

        // Assert
        assertEquals(expectedNumber, decodedResult);
    }

    @Test
    @DisplayName("Test isValidBase62 with valid and invalid strings")
    void testFromBase62_consistencyRoundAndTrip() {

        // Arrange
        long[] numbers = { 0, 1, 10, 62, 100, 1000, 10000, 1000000, 123456789 };

        // Act y Assert
        for (long number : numbers) {
            String encoded = Base62Util.encode(number);
            long decoded = Base62Util.decode(encoded);
            assertEquals(number, decoded, "Round-trip consistency failed for number: " + number);
        }
    }

    @Test
    @DisplayName("This test validates must throw illegal argument exception when encoding a negative number")
    void testFromBase62_encodeNegativeNumber() {

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> {
            Base62Util.encode(-1);
        });
    }

    @Test
    @DisplayName("This test It should throw an exception for Base62 empty")
    void testFromBase62_empty() {

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> Base62Util.decode(""));
    }

    @Test
    @DisplayName("This test It should throw an exception for Base62 null")
    void testFromBase62_null() {

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> Base62Util.decode(null));
    }

    @Test
    @DisplayName("This test It should throw an exception for Base62 with invalid characters")
    void testFromBase62_invalidCharacters() {

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> Base62Util.decode("abc$123"));

        assertThrows(IllegalArgumentException.class, () -> Base62Util.decode("abc@xyz"));
    }

    @Test
    @DisplayName("This test it must validate Base62 correctly")
    void testFromBase62_isValidBase62() {

        // Assert validos
        assertTrue(Base62Util.isValidBase62("0"));
        assertTrue(Base62Util.isValidBase62("abc123"));
        assertTrue(Base62Util.isValidBase62("g8"));
        assertTrue(Base62Util.isValidBase62("XYZ"));

        // Assert inválidos
        assertFalse(Base62Util.isValidBase62(null));
        assertFalse(Base62Util.isValidBase62("abc@123"));
        assertFalse(Base62Util.isValidBase62(""));
        assertFalse(Base62Util.isValidBase62("abc-123"));
    }

    @Test
    @DisplayName("This test it Codes should be short for small IDs")
    void testFromBase62_shortCodesFromSmallIDs() {

        // Assert
        assertEquals(1, Base62Util.encode(1).length());
        assertEquals(2, Base62Util.encode(100).length());
        assertEquals(3, Base62Util.encode(10000).length());
    }

}
