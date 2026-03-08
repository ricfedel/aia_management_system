package it.grandimolini.aia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiaManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiaManagementApplication.class, args);
    }
}
