package com.bryam.urlshortener.service;


public interface CodeGeneratorService {

    //Generar el codigo hash para una url para usuarios aninimos
    String[] generateCodeHash(String originalUrl);

    //Generar el codigo base62 para una url para usuarios
    String generateCodeBase62(Long id);

    //Verificar que el codigo o url no exista
    boolean isValidCode(String code);

    //calcular cuantos caracteres se llevara la base62
    int calculateBaseLength62(Long id);
}
