package com.bryam.urlshortener.service;

public interface ValidatorUrlService {

    //Validar formato de la url
    void validateUrlFormat(String url);

    //Reconstruir o normalizar la url
    String normalizeUrl(String url);

    //Personalización del dominio o slug de un usuario
    void validateSlug(String slug);
}
