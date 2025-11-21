package com.Investube.mvc.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.Investube.mvc.model.dao")
public class MyBatisConfig {

}