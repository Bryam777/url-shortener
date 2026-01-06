package com.bryam.urlshortener.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.bryam.urlshortener.exception.RateLimitExceededException;
import com.bryam.urlshortener.repository.UrlRepository;
import com.bryam.urlshortener.service.RateLimitService;
import com.bryam.urlshortener.util.IpUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final UrlRepository urlRepository;
    //Se utiliza un ConcurrentHashMap para que no haya sobre escritura por multiples hilos
    private final Map<String, RateLimitInfo> cacheAnonymous = new ConcurrentHashMap<>();

    private static final int LIMIT_ANONYMOUS_TIME = 5;
    private static final int LIMIT_USERS_DAY = 100;
    private static final long WINDOW_HOURS = 1;

    @Override
    public void verifyAnonymousLimit(HttpServletRequest request) {

        //SE llama la clase IpUtil con el metodoto para obtener la ip
        //del usuario mediante la peticion que envia
        String ip = IpUtil.getClientIpAdress(request);

        //Se utiliza la funcion computeIfAbsent de Map para crear el mapa
        RateLimitInfo info = cacheAnonymous.computeIfAbsent(ip, k -> new RateLimitInfo());

        LocalDateTime time = LocalDateTime.now();

        //Verificar si la ventana de tiempo expiro
        if (Duration.between(info.getWindowInit(), time).toHours() >= WINDOW_HOURS) {
            //Se resetea el contador
            info.setCount(0);
            info.setWindowInit(time);
            log.debug("Rate limit window reset for IP: {}", IpUtil.obfuscateIP(ip));
        }

        //Verificar el limite
        if (info.getCount() >= LIMIT_ANONYMOUS_TIME) {
            long secondsRemaining = calculateTimeRemaining(info, WINDOW_HOURS);

            log.warn("Rate limit exceeded for IP: {} (contador: {}",
                IpUtil.obfuscateIP(ip),
                info.getCount()
            );

            throw new RateLimitExceededException(
            "You have exceeded the request limit",
             LIMIT_ANONYMOUS_TIME,
            "hour",
            secondsRemaining);
        }

        //Incrementar contador
        info.increase();
        log.debug("Rate Limit for IP {}: {}/{}",
            IpUtil.obfuscateIP(ip),
            info.getCount(),
            LIMIT_ANONYMOUS_TIME
        );
        
    }

    @Override
    public void verifyUserLimit(Long id) {

        //Tomando la fecha y formateando la hora
        LocalDateTime homeToday = LocalDateTime.now().toLocalDate().atStartOfDay();

        //
        long urlsToday = urlRepository.countByUserIdAndCreationDateTimeAfter(id, homeToday);

        if (urlsToday >= LIMIT_USERS_DAY) {
            long secondsUntilMidnight = calculateSecondsUntilMidnight();

            log.warn("Rate limit exceeded for user:  {} (URLs today: {}",
                id, homeToday
            );

            throw new RateLimitExceededException(
                "You have exceeded the daily URL limit",
                LIMIT_USERS_DAY,
                "Day",
                secondsUntilMidnight
            );
        }

        log.debug("Rate limit for user {}: {}/{}",
            id,
            urlsToday,
            LIMIT_USERS_DAY
        );
    }

    @Override
    public void resetLimit(String ip) {
        cacheAnonymous.remove(ip);
        log.info("Rate limit reset for IP: {}", IpUtil.obfuscateIP(ip));
    }

    //Calcula segundos hasta fin de periodo o ventana de tiempo
    private long calculateTimeRemaining(RateLimitInfo info, long windowHour){
        //info el inicio, se agrega la hora a terminar
        LocalDateTime endWindow = info.getWindowInit().plusHours(windowHour);
        //el tiempo inicio y final, se muestra en segundos
        return Duration.between(LocalDateTime.now(), endWindow).getSeconds();
    }

    //Calcula los segundos hasta medianoche
    private long calculateSecondsUntilMidnight(){
        //Tiempo inicio
        LocalDateTime nowTime = LocalDateTime.now();
        //tiempo fin, al dia siguiente, formateado a media noche
        LocalDateTime midnight = nowTime.toLocalDate().plusDays(1).atStartOfDay();
        //tiempo inicio y tiempo final, se muestra en segundos
        return Duration.between(nowTime, midnight).getSeconds();
    }

    //Clase interna para contar los limites y fecha de limites
    private static class RateLimitInfo {

        private int count = 0;
        private LocalDateTime windowInit = LocalDateTime.now();

        public void increase() {
            this.count++;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public LocalDateTime getWindowInit() {
            return windowInit;
        }

        public void setWindowInit(LocalDateTime windowInit) {
            this.windowInit = windowInit;
        }
    }
}
