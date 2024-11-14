package com.zyp.ssl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.requiresChannel(channel ->
                        channel.anyRequest().requiresSecure() // 强制使用HTTPS
                )
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated() // 需要认证
                )
                .csrf(csrf ->
                        csrf.disable() // 根据需求启用或禁用CSRF保护
                );

        return http.build();
    }
}



// 在Spring Security 5.7之后，WebSecurityConfigurerAdapter已被弃用在Spring Security 5.7之后，WebSecurityConfigurerAdapter已被弃用
// 现在推荐使用SecurityFilterChain和@Bean注解来配置Spring Security。Spring官方文档建议将安全配置类重构为使用SecurityFilterChain的方式。
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//            .requiresChannel()
//                .anyRequest().requiresSecure() // 强制HTTPS
//            .and()
//            .authorizeRequests()
//                .anyRequest().authenticated() // 需要认证
//            .and()
//            .csrf().disable(); // 根据需求启用或禁用CSRF保护
//    }
//}
