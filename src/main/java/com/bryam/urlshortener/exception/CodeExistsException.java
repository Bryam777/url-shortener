package com.bryam.urlshortener.exception;

public class CodeExistsException extends RuntimeException {

    private final String codeAttempted;
    private final boolean isReutilizable;


    public CodeExistsException(String message, String codeAttempted, boolean isReutilizable) {
        super(message);
        this.codeAttempted = codeAttempted;
        this.isReutilizable = isReutilizable;
    }

    public CodeExistsException(String message, String codeAttempted) {
        this(message, codeAttempted, false);
    }

    public String getCodeAttempted() {
        return codeAttempted;
    }

    public boolean isReutilizable() {
        return isReutilizable;
    }
}
