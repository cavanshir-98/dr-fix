package tech.masterfix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tech.masterfix.config.EnvLocalBootstrap;

@SpringBootApplication
public class MasterFixApplication {
    public static void main(String[] args) {
        EnvLocalBootstrap.load();
        SpringApplication.run(MasterFixApplication.class, args);
    }
}
