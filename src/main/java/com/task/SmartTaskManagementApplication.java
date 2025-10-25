package com.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SmartTaskManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartTaskManagementApplication.class, args);
	}

}
