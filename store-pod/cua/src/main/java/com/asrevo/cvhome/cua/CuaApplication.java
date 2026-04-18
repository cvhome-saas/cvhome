package com.asrevo.cvhome.cua;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.asrevo.cvhome.s2s.config.CvhomeSharedConfig;

@SpringBootApplication
@Import(CvhomeSharedConfig.class)
public class CuaApplication {

    private CuaApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(CuaApplication.class, args);
    }

}
