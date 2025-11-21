package com.Investube.mvc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.Investube.mvc.model.dao")
public class SsafitApplication {

	public static void main(String[] args) {
		SpringApplication.run(SsafitApplication.class, args);
	}

}
