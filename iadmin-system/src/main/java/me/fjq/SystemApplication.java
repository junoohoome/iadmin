package me.fjq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }

//    @Bean
//    public SpringContextHolder springContextHolder() {
//        return new SpringContextHolder();
//    }

}
