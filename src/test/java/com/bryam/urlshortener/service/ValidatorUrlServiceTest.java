package com.bryam.urlshortener.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import com.bryam.urlshortener.service.impl.ValidatorUrlServiceImpl;

//1. Esta anotacion sirve para decirle a Mockito que antes de cada test
//se encargue de crear los objetos mock y de inyectarlos en la clase que se quiere testear
//2. Tambien limpia los estados de los mocks despues de cada test, para evictar que los resultados de un test afecten a otro
//3. Lo ultimo es administrar el ciclo de vida de los mocks, para que se creen y se destruyan 
//de manera adecuada durante la ejecucion de los tests
@DisplayName("Test of ValidatorUrlServiceTest")
public class ValidatorUrlServiceTest {

    private ValidatorUrlService validatorUrlServiceTest;

    @BeforeEach
    void setUp() {
        validatorUrlServiceTest = new ValidatorUrlServiceImpl();

        // Inyectar valor de baseUrl usando reflection, porque el campo es privado y no
        // tiene un setter
        // Reflection es una tecnica que permite acceder y modificar campos y metodos de
        // una clase
        ReflectionTestUtils.setField(validatorUrlServiceTest, "baseUrl", "http://localhost:8080");
    }
}
