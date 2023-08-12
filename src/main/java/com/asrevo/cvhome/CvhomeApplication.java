package com.asrevo.cvhome;

import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Getter
public class CvhomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CvhomeApplication.class, args);
    }

}
