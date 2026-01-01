package com.bryam.urlshortener.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class HashUtil {

    //Metodo para crear una instancia de MessageDigest para SHA-256
    private static final ThreadLocal<MessageDigest> DIGEST = ThreadLocal.withInitial(() ->{
        //Manejar la excepcion en caso de que el algoritmo no este disponible
        try {
            //Crear una instancia de MessageDigest con el algoritmo SHA-256
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException  e) {
            //Lanzar una excepcion para parar el hilo si no se encuentra el algoritmo
            throw new IllegalStateException(
                "SHA-256 not available in the JVM", e
            );
        }
    });

    //Metodo para generar el hash de una url
    public static String generateHash(String originalUrl){
        //Verificar que la url no sea nula o vacia
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            //Lanzar una excepcion para parar el hilo si la url es invalidad
            throw new IllegalArgumentException("The original URL cannot be null or empty");
        }
        //Obtener la instancia de MessageDigest para el hilo actual
        MessageDigest digest = DIGEST.get();
        //Formatear digest para asegurar que este limpio entre usos
        digest.reset();
        //Generar el hash de la url original
        byte[] hasBytes = digest.digest(originalUrl.getBytes(StandardCharsets.UTF_8));
        //Convertir el hash a una representacion hexadecimal y retornarlo
        return HexFormat.of().formatHex(hasBytes);
    }

    //Metodo para  extraer el codigo hash generado de una url
    //Recibe el hahs completo y la longitud del codigo a extraer
    public static String extractCode(String hashComplete, int codeLength){
        if (hashComplete == null || hashComplete.length() < codeLength) {
            throw new IllegalArgumentException("The hash is invalid or too short");
        }
        return hashComplete.substring(0, codeLength);
    }
}
