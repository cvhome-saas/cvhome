package com.asrevo.cvhome.merchant;

import org.springframework.boot.SpringApplication;

public class TestMerchantApplication {

	public static void main(String[] args) {
		SpringApplication.from(MerchantApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
