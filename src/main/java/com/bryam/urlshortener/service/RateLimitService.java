package com.bryam.urlshortener.service;

import jakarta.servlet.http.HttpServletRequest;


public interface RateLimitService {

    //Verificar que un usuario anónimo no supere el limite de peticiones
    public abstract void verifyAnonymousLimit(HttpServletRequest request);

    //Verificar si un usuario registrado a superado sus limites
    void verifyUserLimit(Long id);

    //Recetar el limite para un usuario en especifico con ip
    void resetLimit(String ip);
}
