package com.aolda.ojakgyo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class})
public class OjakgyoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OjakgyoApplication.class, args);
    }

}
