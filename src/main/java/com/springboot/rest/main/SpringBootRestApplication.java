package com.springboot.rest.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sun.istack.internal.logging.Logger;

@SpringBootApplication(scanBasePackages = {"com.springboot"})
public class SpringBootRestApplication {
	private static final Logger logger = Logger.getLogger(SpringBootRestApplication.class);
	
	public static void main(String[] args) {
		logger.info("SpringBoot application starting by hitting run() of SpringApplication for automatic container initialization");
		
		SpringApplication.run(SpringBootRestApplication.class, args);
	}
}
