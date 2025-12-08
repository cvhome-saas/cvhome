package com.asrevo.cvhome.checkout;

import org.springframework.boot.SpringApplication;

public class TestCheckoutApplication {

	public static void main(String[] args) {
		SpringApplication.from(CheckoutApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
