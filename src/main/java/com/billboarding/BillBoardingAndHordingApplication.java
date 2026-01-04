package com.billboarding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BillBoardingAndHordingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillBoardingAndHordingApplication.class, args);
	}

}
