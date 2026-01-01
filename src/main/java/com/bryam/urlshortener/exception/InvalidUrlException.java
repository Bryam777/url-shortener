package com.bryam.urlshortener.exception;

//Clase de exepcion personalizada para urls invalidas
public class InvalidUrlException extends RuntimeException{

    //constructor que recibe solo el mensaje de error de RunTimeException
    public InvalidUrlException(String message){
        super(message);
    }
    //construtuor que recibe mensaje y la causa del error de RunTimeException
    public InvalidUrlException(String message, Throwable cause){
        super(message, cause);
    }
}
