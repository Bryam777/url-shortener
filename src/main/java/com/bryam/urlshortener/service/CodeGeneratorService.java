package com.bryam.urlshortener.service;


public interface CodeGeneratorService {

    //Generar el código hash para una url para usuarios anónimos
    String[] generateCodeHash(String originalUrl);

    //Generar el código base62 para una url para usuarios
    String generateCodeBase62(Long id);

    //Verificar que el código o url no exista
    boolean isValidCode(String code);

    //calcular cuantos caracteres se llevara la base62
    int calculateBaseLength62(Long id);
}
