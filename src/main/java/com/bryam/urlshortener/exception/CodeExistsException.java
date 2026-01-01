package com.bryam.urlshortener.exception;

//Clase de excepcion perzonalida para cuando el codigo corto ya exista
// para no repetir duplicados para usuarios anonimos
//Clase que extiende de RunTimeException para excepciones no verificadas
public class CodeExistsException extends RuntimeException {

    
    private final String codeAttempted;               //codigo corto que se intento usar o reutilizar
    private final boolean isReutilizableCodeExists;   //Indica si el condigo existe y se puede reutilizar


    public CodeExistsException(String message, String codeAttempted, boolean isReutilizableCodeExists) {
        //llamada al constructor de la clase padre RuntimeException
        //Se pasa el mensaje del error
        super(message);
        this.codeAttempted = codeAttempted;
        this.isReutilizableCodeExists = isReutilizableCodeExists;
    }

    //constructor sobrecargado para casos donde no se especifica si es reutilizable
    //por defecto se asume que no es reutilizable
    public CodeExistsException(String message, String codeAttempted) {
        this(message, codeAttempted, false);
    }

    public String getCodeAttempted() {
        return codeAttempted;
    }

    public boolean isReutilizable() {
        return isReutilizableCodeExists;
    }
}
