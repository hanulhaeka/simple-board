package com.example.simple_board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class SimpleBoardApplication {


	public static void main(String[] args) {
		SpringApplication.run(SimpleBoardApplication.class, args);
	}

}
