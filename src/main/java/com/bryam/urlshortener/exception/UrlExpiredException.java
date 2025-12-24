package com.bryam.urlshortener.exception;

import java.time.LocalDateTime;

public class UrlExpiredException extends RuntimeException{

    private final LocalDateTime expirationDateTime;
    private final boolean isPerzonalized;
    
    public UrlExpiredException(String message, LocalDateTime expirationDateTime, boolean isPerzonalized) {
        super(message);
        this.expirationDateTime = expirationDateTime;
        this.isPerzonalized = isPerzonalized;
    }

    public UrlExpiredException(String message, LocalDateTime expirationDateTime) {
        this(message, expirationDateTime, false);
    }

    public LocalDateTime getExpirationDateTime() {
        return expirationDateTime;
    }

    public boolean isPerzonalized() {
        return isPerzonalized;
    }
}
