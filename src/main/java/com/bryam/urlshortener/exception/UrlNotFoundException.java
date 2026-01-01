package com.bryam.urlshortener.exception;

//Clase de excepcion personalizada para cuando no se encuentra una url
public class UrlNotFoundException extends RuntimeException {

    //Constructor que recibe un mensaje de error
    public UrlNotFoundException(String message){
        super(message);
    }

    //Constructor que recibe un mensaje de error y una causa
    public UrlNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}
