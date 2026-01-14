package com.bryam.urlshortener.util;

public class Base62Util {

    private static final String BASE62_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = BASE62_CHARACTERS.length();

    //Se espera un numero como parámetro, que seria el id de la url en la base de datos
    public static String encode(long value) {
        //Validación de que el numero no sea negativo, si lo es comparamos el hilo con una excepción
        if (value < 0) {
            throw new IllegalArgumentException("Value must be non-negative");
        }
        //Si el valor es cero, retornamos un cero como cadena
        if (value == 0) {
            return "0";
        }
        //Proceso de conversion a base 62
        //Usamos un StringBuilder para construir la cadena resultante
        //El StringBuilder es para tener cadenas mutables y eficientes
        //Recorreremos el numero dividiéndolo por la base 62 y obtenemos el residuo
        //Se agrega a StringBuilder y se selecciona el caracteres en la constante BASE62_CHARACTERS
        StringBuilder encoded = new StringBuilder();
        while (value > 0) {
            int remainder = (int) (value % BASE);
            encoded.append(BASE62_CHARACTERS.charAt(remainder));    
            value /= BASE;
        }
        return encoded.reverse().toString();
    }

    //Conversion de una cadena base 62 a un numero decimal
    public static long decode(String encoded) {
        //Verificaciones de que la cadena no sea nula o vacía
        if (encoded == null || encoded.isEmpty()) {
            throw new IllegalArgumentException("Encoded string cannot be null or empty");
        }
        //Proceso de conversion de base 62 a decimal
        long value = 0;
        for (int i = 0; i < encoded.length(); i++) {
            //Se recorre hasta el caracteres y se obtiene su indice en la constante BASE62_CHARACTERS
            int charValue = BASE62_CHARACTERS.indexOf(encoded.charAt(i));
            if (charValue == -1) {
                throw new IllegalArgumentException("Invalid character in encoded string: " + encoded.charAt(i));
            }
            value = value * BASE + charValue;
        }
        return value;
    }

    //Método para validar si una cadena es una cadena de base 62
    public static boolean isValidBase62(String encoded){
        //Verificaciones de que una cadena no sea nula o vacía
        if (encoded == null || encoded.isEmpty()) {
            return false;
        }
        //Recorrer la cadena y verificar que cada caracteres este en la constante BASE62_CHARACTERS
        //Se convierte el string a un arreglo de caracteres
        for (char character : encoded.toCharArray()) {
            //Se obtiene cada caracteres de la cadena y se valida
            if (BASE62_CHARACTERS.indexOf(character) == -1) {
                return false;
            }
        }
        return true;
    }

    //Método para predecir cuantos caracteres tendrá una cadena de base 62 a partir de un numero decimal
    public static int calculateLength(Long number){
        //Verificaciones de que el numero no sea negativo  
        if (number <= 0) {
            return 1;
        }

        return (int) Math.ceil(Math.log(number+ 1) / Math.log(BASE));
    }
}
