package com.db.ayce.be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AyceBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(AyceBeApplication.class, args);
	}

}
