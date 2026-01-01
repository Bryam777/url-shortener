package com.bryam.urlshortener.exception;

//Clase de excepcion personalizada para cuando se excede el limite de peticiones
public class RateLimitExceededException extends RuntimeException {

    private final int limit;            //limite de peticiones permitidas
    private final String period;        //periodo de tiempo para el limite 
    private final Long timeoutSeconds;  //tiempo en segundos hasta que se resetea el limite
    
    public RateLimitExceededException(String message, int limit, String period, Long timeoutSeconds) {
        //llamada al construtor de la clase padre RunTimeException
        super(message);
        this.limit = limit;
        this.period = period;
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getLimit() {
        return limit;
    }

    public String getPeriod() {
        return period;
    }

    public Long getTimeoutSeconds() {
        return timeoutSeconds;
    }

}
