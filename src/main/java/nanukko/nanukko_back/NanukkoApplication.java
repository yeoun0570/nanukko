package nanukko.nanukko_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NanukkoApplication {

    public static void main(String[] args) {
        SpringApplication.run(NanukkoApplication.class, args);
    }

}
