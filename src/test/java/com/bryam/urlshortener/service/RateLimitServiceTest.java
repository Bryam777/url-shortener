package com.bryam.urlshortener.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

//1. Esta anotacion sirve para decirle a Mockito que antes de cada test
//se encargue de crear los objetos mock y de inyectarlos en la clase que se quiere testear
//2. Tambien limpia los estados de los mocks despues de cada test, para evictar que los resultados de un test afecten a otro
//3. Lo ultimo es administrar el ciclo de vida de los mocks, para que se creen y se destruyan 
//de manera adecuada durante la ejecucion de los tests
@ExtendWith(MockitoExtension.class)
@DisplayName("Test of RateLimitServiceTest")
public class RateLimitServiceTest {

}
