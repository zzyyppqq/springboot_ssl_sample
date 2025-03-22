package com.zyp.ssl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


/**
 * 经测试Get请求，重写securityFilterChain，无需关闭crsf，即可访问；否则无法访问
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Spring Security 默认启用了 CSRF 保护，它会要求所有非幂等的 HTTP 请求（如 POST、PUT、DELETE 等）必须携带一个 CSRF Token。
    // 如果请求未携带有效的 CSRF Token，服务器会拒绝请求并返回 401 Unauthorized 错误。
    // 在你的场景中，关闭 CSRF 保护后，请求成功返回 200，这表明请求本身是合法的，但未通过 CSRF 验证。
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // 根据需求启用或禁用CSRF保护
                .requiresChannel(channel ->
                        channel.anyRequest().requiresSecure() // 强制使用HTTPS
                );
//                .authorizeRequests(authorizeRequests ->
//                        authorizeRequests.anyRequest().authenticated() // 需要认证
//                );
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
