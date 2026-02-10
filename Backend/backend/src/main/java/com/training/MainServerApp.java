package com.training;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainServerApp {

	public static void main(String[] args) {
		SpringApplication.run(MainServerApp.class, args);
	}
	private static final Logger logger = LoggerFactory.getLogger(MainServerApp.class);

	@PostConstruct
	public void init() {
		logger.info("Application started successfully!");
	}

}

