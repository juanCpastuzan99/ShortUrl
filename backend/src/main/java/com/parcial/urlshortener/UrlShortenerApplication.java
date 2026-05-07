package com.parcial.urlshortener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableAsync
public class UrlShortenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApplication.class, args);
    }

    @Component
    static class StartupBanner {
        @Value("${server.port:8080}")
        private int port;

        @EventListener(ApplicationReadyEvent.class)
        public void onReady() {
            String url = "http://localhost:" + port;
            String line = "=".repeat(60);
            System.out.println();
            System.out.println(line);
            System.out.println("  Acortador de Enlaces — listo");
            System.out.println("  Acceso:  " + url);
            System.out.println("  Reporte: " + url + "/report.html");
            System.out.println(line);
            System.out.println();
        }
    }
}
