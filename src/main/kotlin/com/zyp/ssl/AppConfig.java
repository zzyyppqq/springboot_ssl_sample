package com.zyp.ssl;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

// 实际测试不生效
@Configuration
@PropertySource("classpath:templates/application.properties")
public class AppConfig {
}