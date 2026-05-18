package com.bryam.urlshortener.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bryam.urlshortener.repository.UrlRepository;
import com.bryam.urlshortener.service.impl.CodeGeneratorServiceImpl;

//1. Esta anotacion sirve para decirle a Mockito que antes de cada test
//se encargue de crear los objetos mock y de inyectarlos en la clase que se quiere testear
//2. Tambien limpia los estados de los mocks despues de cada test, para evictar que los resultados de un test afecten a otro
//3. Lo ultimo es administrar el ciclo de vida de los mocks, para que se creen y se destruyan 
//de manera adecuada durante la ejecucion de los tests
@ExtendWith(MockitoExtension.class)
@DisplayName("Test of CodeGeneratorServiceTest")
public class CodeGeneratorServiceTest {

    // Mock es una anotacion para crear un objeto simulado de una clase o interfaz
    @Mock
    private UrlRepository urlRepository;

    // Es una anotacion para crear una instacia de la clase que se quiere testear, y
    // para inyectar los mocks
    @InjectMocks
    private CodeGeneratorServiceImpl codeGeneratorServiceTest;

    @Test
    @DisplayName("Test for Must generate a unique hash code without collisions")
    void testGenerateHash_withoutCollisions() {
        // Arrange
        // Simula que el codigo corto generado aún no existe en la base de datos.
        // se usa anyString() porque el código se genera dinámicamente.
        String url = "https://www.youtube.com/watch?v=fBrX8ym3dBM&list=RDfBrX8ym3dBM&start_radio=1";
        when(urlRepository.existsByShortCode(anyString())).thenReturn(false);

        // Act
        String[] result = codeGeneratorServiceTest.generateCodeHash(url);

        // Assert
        // Verifica que el resultado no se nulo y que el codigo corto tenga la longitud
        // esperada
        assertNotNull(result);
        assertEquals(2, result.length);

        String fullHash = result[0];
        String shortCode = result[1];

        assertEquals(64, fullHash.length(), "Full hash must be 64 characters long");
        assertEquals(7, shortCode.length(), "short code must be 7 characters long");

        // Verificar que el codico corto sea un substring del hash completo
        // ya que el hash completo empiza con el codigo corto, se utiliza startsWith()
        // para verificalo
        assertTrue(fullHash.startsWith(shortCode));

        // Verificar que se consulto la BD
        verify(urlRepository, atLeastOnce()).existsByShortCode(anyString());
    }

    @Test
    @DisplayName("Test for Must handle collision by increasing code length")
    void testGenerateHashCode_withCollision() {

        // Arrange
        String url = "https://www.youtube.com/watch?v=fBrX8ym3dBM&list=RDfBrX8ym3dBM&start_radio=1";

        // Simular una colision en la longitud 7, disponible para la longitud 8
        // En el primer llamado se simula el choque
        // En el segundo llamado se simula que es unico
        // Esto para verificar que se maneja correctamente las colisiones
        when(urlRepository.existsByShortCode(anyString()))
                .thenReturn(true) // Primera llamada: colision en longitud 7
                .thenReturn(false); // Segunda llamada: codigo unico en longitud 8

        // Act
        // Geberar el hash
        String[] result = codeGeneratorServiceTest.generateCodeHash(url);

        // Assert
        // Se espera que la longitud del codigo corto sea de 8 caracteres
        // ya que simulo una colision en la longitud de 7 caracteres
        String shortCode = result[1];
        assertEquals(8, shortCode.length(), "Code must have 8 characters after collision");

        // Verificar que se hallan realizado 2 consultas a la BD
        verify(urlRepository, times(2)).existsByShortCode(anyString());
    }

    @Test
    @DisplayName("Test for Should throw exception when max attempts are exceeded")
    void testGenerateHashCode_exceedsMaxAttempts() {

        // Arrange
        String url = "https://www.youtube.com/watch?v=1IgOZaQqB58&list=RD1IgOZaQqB58&start_radio=1";

        // Simular que los codigos ya existen en la base de datos para todas las
        // longitudes probadas
        when(urlRepository.existsByShortCode(anyString())).thenReturn(true);

        // Act & Assert
        // Se espera que lance una excepcion despues de agotar los intetos maximos
        assertThrows(IllegalStateException.class, () -> {
            codeGeneratorServiceTest.generateCodeHash(url);
        });

        // Verificar que se intento en varias ocasiones generar un codido unico
        verify(urlRepository, atLeast(5)).existsByShortCode(anyString());
    }

    @Test
    @DisplayName("Test for Should generate same hash for same URL")
    void testGenerateHashCode_isDeterministic() {

        // Arrange
        String url = "https://www.youtube.com/watch?v=1IgOZaQqB58&list=RD1IgOZaQqB58&start_radio=1";
        when(urlRepository.existsByShortCode(anyString())).thenReturn(false);

        // Act
        String[] result1 = codeGeneratorServiceTest.generateCodeHash(url);
        String[] result2 = codeGeneratorServiceTest.generateCodeHash(url);

        // Assert
        // Se espera que el mismo URL genere el mismo hash y codigo corto
        assertEquals(result1[0], result2[0], "Full hash should be the same for the same URL");
        assertEquals(result1[1], result2[1], "Short code should be the same for the same URL");
    }

    @Test
    @DisplayName("Test forShould generate Base62 code correctly")
    void testGenerateBase62Code() {

        // Arrange
        Long id = 1000L;

        // Act
        String base62Code = codeGeneratorServiceTest.generateCodeBase62(id);

        // Assert
        // Se espera que el codugo Base62 generado sea correcto para el ID dado
        assertEquals("G8", base62Code);
    }

    @Test
    @DisplayName("Test for Should generate different Base62 codes for different IDs")
    void testGenerateBase62Code_differentIds() {

        // Arrange
        Long id1 = 1L;
        Long id2 = 1000L;

        // Act
        String base62Code1 = codeGeneratorServiceTest.generateCodeBase62(id1);
        String base62Code2 = codeGeneratorServiceTest.generateCodeBase62(id2);

        // Assert
        // Se espera que diferentes IDs generen codigos Base62 diferentes
        assertNotEquals(base62Code1, base62Code2);
        assertEquals("1", base62Code1);
        assertEquals("G8", base62Code2);
    }

    @Test
    @DisplayName("Test for Should throw exception for null ID in Base62")
    void testGenerateBase62Code_nullId() {

        // Act & Assert
        // Se espera que lance una excepcion al intentar generar un codigo Base62 con un
        // ID nulo
        assertThrows(IllegalArgumentException.class, () -> {
            codeGeneratorServiceTest.generateCodeBase62(null);
        });
    }

    @Test
    @DisplayName("Test for Should throw exception for negative ID in Base62")
    void testGenerateBase62Code_negativeId() {

        // Act & Assert
        // Se espera que lance una excepcion al intentar generar un codigo Base62 con un
        // ID negativo
        assertThrows(IllegalArgumentException.class, () -> {
            codeGeneratorServiceTest.generateCodeBase62(-1L);
        });
    }

    @Test
    @DisplayName("Should validate alphanumeric code correctly")
    void testIsValidCode() {

        // Assert validos
        assertTrue(codeGeneratorServiceTest.isValidCode("abc123"));
        assertTrue(codeGeneratorServiceTest.isValidCode("aB7x9Km"));
        assertTrue(codeGeneratorServiceTest.isValidCode("mi-link")); // Con guion
        assertTrue(codeGeneratorServiceTest.isValidCode("1"));

        // Assert inválidos
        assertFalse(codeGeneratorServiceTest.isValidCode(null));
        assertFalse(codeGeneratorServiceTest.isValidCode(""));
        assertFalse(codeGeneratorServiceTest.isValidCode("abc$123"));
        assertFalse(codeGeneratorServiceTest.isValidCode("abc@xyz"));
        assertFalse(codeGeneratorServiceTest.isValidCode("a".repeat(13)));
    }

    @Test
    @DisplayName("Test for Should calculate Base62 length correctly")
    void testCalculateBase62Length() {

        // Assert
        // Se espera que la longitud del codigo Base62 calculada sea correcta para los
        // IDs dados
        assertEquals(1, codeGeneratorServiceTest.calculateBaseLength62(1L));
        assertEquals(2, codeGeneratorServiceTest.calculateBaseLength62(100L));
        assertEquals(3, codeGeneratorServiceTest.calculateBaseLength62(10000L));
        assertEquals(4, codeGeneratorServiceTest.calculateBaseLength62(1000000L));
    }

}
