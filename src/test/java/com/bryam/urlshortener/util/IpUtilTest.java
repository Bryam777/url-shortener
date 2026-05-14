package com.bryam.urlshortener.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.servlet.http.HttpServletRequest;

@DisplayName("Test for IpUtil")
public class IpUtilTest {

    @Test
    @DisplayName("Test for Must obtain IP from remoteAddr")
    void testGetClientIp_fromRemoteAddr() {

        //Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        //Act
        String ip = IpUtil.getClientIpAdress(request);

        //Assert
        assertEquals("192.168.1.1", ip);
    }

    @Test
    @DisplayName("Test for Must obtain IP from X-Forwarded-For header")
    void testGetClientIp_fromXForwardedFor() {

        //Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        //Act
        String ip = IpUtil.getClientIpAdress(request);

        //Assert
        assertEquals("192.168.1.1", ip);
        
    }

    @Test
    @DisplayName("Test forMust obtain the first IP from multiple X-Forwarded-For values")
    void testGetClientIp_fromMultipleXForwardedFor() {

        //Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 198.51.100.5, 10.0.0.1");

        //Act
        String ip = IpUtil.getClientIpAdress(request);

        //Assert
        assertEquals("192.168.1.1", ip);
    }

    @Test
    @DisplayName("Test for Must ignore 'unknown' header value")
    void testIgnore_unknownHeader() {

        //Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        //Act
        String ip = IpUtil.getClientIpAdress(request);

        //Assert
        assertEquals("10.0.0.1", ip);
    }

    @Test
    @DisplayName("Test for Must return 0.0.0.0 when request is null")
    void testReturnDefaultIpWhen_requestIsNull() {

        //Act
        String ip = IpUtil.getClientIpAdress(null);

        //Assert
        assertEquals("0.0.0.0", ip);
    }

    @Test
    @DisplayName("Test for Must detect private IPs correctly")
    void testIsPrivateIp() {

        //Assert localhost
        assertTrue(IpUtil.isIpPrivate("10.0.0.1"));
        assertTrue(IpUtil.isIpPrivate("::1"));

        //Assert rangos privados
        assertTrue(IpUtil.isIpPrivate("10.0.0.1"));
        assertTrue(IpUtil.isIpPrivate("172.16.0.1"));
        assertTrue(IpUtil.isIpPrivate("172.31.255.255"));
        assertTrue(IpUtil.isIpPrivate("192.168.1.1"));

        //Assert IPs públicas
        assertFalse(IpUtil.isIpPrivate("8.8.8.8"));
        assertFalse(IpUtil.isIpPrivate("1.1.1.1"));
        assertFalse(IpUtil.isIpPrivate("203.0.113.45"));
    }

    @Test
    @DisplayName("Test for Must obfuscate IPv4 address correctly")
    void testObfuscate_ipv4() {

        //Act
        String obfuscated = IpUtil.obfuscateIP("192.168.1.1");

        //Assert
        assertEquals("192.168.***.***", obfuscated);
    }

    @Test
    @DisplayName("Test for Must obfuscate IPv6 address correctly")
    void testObfuscate_ipv6() {

        //Act
        String obfuscated = IpUtil.obfuscateIP("2001:0db8:85a3:0000:0000:8a2e:0370:7334");

        //Assert
        assertTrue(obfuscated.contains("2001"));
        assertTrue(obfuscated.contains("****"));
        assertEquals("2001:0db8:85a3:0000:****:****:****:****", obfuscated);
    }

    @Test
    @DisplayName("Test for Must handle null IP in obfuscation")
    void testObfuscateIp_whenIpIsNull() {

        //Act
        String obfuscated = IpUtil.obfuscateIP(null);

        //assert
        assertEquals("***.***.***", obfuscated);
    }
}
