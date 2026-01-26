package com.walkdoro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class WalkDoroApplication {

	public static void main(String[] args) {
		SpringApplication.run(WalkDoroApplication.class, args);
	}

}
