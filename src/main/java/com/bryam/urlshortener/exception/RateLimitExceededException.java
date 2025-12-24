package com.bryam.urlshortener.exception;

public class RateLimitExceededException extends RuntimeException {

    private final int limit;
    private final String period;
    private final Long timeoutSeconds;
    
    public RateLimitExceededException(String message, int limit, String period, Long timeoutSeconds) {
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
