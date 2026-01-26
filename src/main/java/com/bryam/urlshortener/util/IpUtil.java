package com.bryam.urlshortener.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Slf4j
public class IpUtil {

    // Headers a verificar en orden
    private static final String[] HEADERS_IP = {
            "X-Forwarded-For", // Estándar de facto
            "X-Real-IP", // Nginx
            "CF-Connecting-IP", // Cloudflare
            "True-Client-IP", // Akamai, Cloudflare Enterprise
            "X-Client-IP", // Genérico
            "X-Cluster-Client-IP", // Rackspace, Riverbed
            "Forwarded", // RFC 7239
            "Proxy-Client-IP", // Apache
            "WL-Proxy-Client-IP" // WebLogic
    };

    // Extraer la ip de un Header especifico
    public static String extractIpFromHeader(HttpServletRequest request, String headerName) {
        // Se obtiene el header de la petición
        String headerValue = request.getHeader(headerName);
        // Validar que el header no este vació, nulo o desconocido
        if (headerName == null || headerName.isEmpty() || "unknown".equalsIgnoreCase(headerValue)) {
            return null;
        }
        // X-Forwarded-For puede contener múltiples IPs: "cliente, proxy1, proxy2"
        // La primera es la IP real del cliente
        if (headerValue.contains(",")) {
            String[] ips = headerValue.split(",");
            for (String ip : ips) {
                String trimmedIp = ip.trim();
                if (isIpValid(trimmedIp)) {
                    return trimmedIp;
                }
            }
            return null;
        }
        // devolver una cadena sin espacios al comienzo y final
        return headerValue.trim();
    }

    // Método para obtener la direccion ip del cliente desde la petición http
    public static String getClientIpAdress(HttpServletRequest request) {
        // Verificar que la petición no sea nula
        if (request == null) {
            // Si la petición es nula, se guarda un log de advertencia y se retorna una ip
            // por defecto
            log.warn("HttpServletRequest is null, returning unknown IP");
            return "0.0.0.0";
        }

        // Se recorre la constante HEADERS_IP para verificar de cual de todos los
        // proxies viene la petición
        // para obtener la ip del cliente
        for (String header : HEADERS_IP) {
            String ip = extractIpFromHeader(request, header);
            if (ip != null && isIpValid(ip)) {
                return ip;
            }
        }

        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null && isIpValid(remoteAddr)) {
            log.debug("IP address obtained from remoteAddr: {}", remoteAddr);
            return remoteAddr;
        }

        // Si no se pudo obtener la ip del cliente, se guarda un log de advertencia y se
        // retorna una ip por defecto
        log.warn("A valid IP address could not be obtained from the request.");
        return "0.0.0.0";
    }

    public static boolean isIpValid(String ip) {
        // Verificar que la ip exista, que no sea vacía, nula o desconocida
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }
        // Eliminar espacios
        ip = ip.trim();

        // Validar el formato de la ip
        if (!isValidIPFormat(ip)) {
            return false;
        }

        return true;
    }

    public static boolean isValidIPFormat(String ip) {

        // Verificar si es IPv4, permitir formato de Ipv
        if (ip.matches("^(\\d{1,3}\\.){3}\\d{1,3}$")) {
            return isIpValid(ip);
        }

        // Verificar si es IPv6
        if (ip.contains(":")) {
            return isValidIpv6(ip);
        }

        return false;
    }

    // Validar si una cadena es una direccion IPv4
    public static boolean isValidIpv4(String ip) {
        // Dividir la direccion Ip en sus cuatro octetos
        // Se usa \\., porque el punto es un caracter especial en expresiones regulares
        String[] parts = ip.split("\\.");

        // Verificar que haya exactamente cuatro partes
        if (parts.length != 4) {
            return false;
        }

        try {
            // Recorrer cada octeto y verificar que se encuentre entre 0 y 255
            for (String part : parts) {
                int value = Integer.parseInt(part);
                if (value < 0 || value > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }

    }

    public static boolean isValidIpv6(String ip) {
        // Validación simplificada de IPv6
        // Formato, 8 grupos de 4 dígitos hexadecimales separados por :
        // Un ejemplo, 2001:0db8:85a3:0000:0000:8a2e:0370:7334
        // También permite :: para comprimir ceros

        String ipv6Pattern = "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" + // Formato completo
                "^::(?:[0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4}$|" + // Empieza con ::
                "^(?:[0-9a-fA-F]{1,4}:){1,7}:$|" + // Termina con ::
                "^(?:[0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$"; // :: en el medio

        return ip.matches(ipv6Pattern);
    }

    public static boolean isIpPrivate(String ip) {

        if (ip == null || ip.isEmpty()) {
            return false;
        }

        // Localhost
        if (ip.equals("127.0.0.1") ||
                ip.equals("0:0:0:0:0:0:0:1") ||
                ip.equals("::1")) {
            return true;
        }

        // Rangos privados IPv4
        // 10.0.0.0 - 10.255.255.255
        if (ip.startsWith("10.")) {
            return true;
        }

        // Rango de ips 172.16.0.0 - 172.31.255.255
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    if (second >= 16 && second <= 31) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Rango de Ipv4 192.168.0.0 - 192.168.255.255
        if (ip.startsWith("192.168.")) {
            return true;
        }

        // Link-local: 169.254.0.0 - 169.254.255.255
        if (ip.startsWith("169.254.")) {
            return true;
        }

        return false;
    }

    public static String getUnknownIP(HttpServletRequest request) {
        String ip = getClientIpAdress(request);
        return (ip != null && ip.equals("0.0.0.0")) ? ip : "unknown";
    }

    public static String obfuscateIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "***.***.***";
        }

        // IPv4
        if (ip.contains(".")) {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + ".***.**";
            }
        }

        // IPv6 - ofuscar últimos 4 grupos
        if (ip.contains(":")) {
            String[] parts = ip.split(":");
            if (parts.length >= 4) {
                return parts[0] + ":" + parts[1] + ":" + parts[2] + ":****:****:****:****";
            }
        }
        return "***";
    }
}
