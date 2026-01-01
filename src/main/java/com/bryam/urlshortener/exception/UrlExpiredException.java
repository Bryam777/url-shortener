package com.bryam.urlshortener.exception;

import java.time.LocalDateTime;

//Clase de excepcion personalizada para cuando una url ha expirado
public class UrlExpiredException extends RuntimeException{

    private final LocalDateTime expirationDateTime; //fecha y hora de expiracion de la url
    private final boolean isPerzonalizedUrlExpired; //indica si la url personalizada ha expirado
    
    public UrlExpiredException(String message, LocalDateTime expirationDateTime, boolean isPerzonalizedUrlExpired) {
        super(message);
        this.expirationDateTime = expirationDateTime;
        this.isPerzonalizedUrlExpired = isPerzonalizedUrlExpired;
    }

    public UrlExpiredException(String message, LocalDateTime expirationDateTime) {
        this(message, expirationDateTime, false);
    }

    public LocalDateTime getExpirationDateTime() {
        return expirationDateTime;
    }

    public boolean isPerzonalized() {
        return isPerzonalizedUrlExpired;
    }
}
