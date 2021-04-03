package com.felixhoner.schichtplaner.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@Profile(value = ["integration-test"])
class SecurityTestConfiguration {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.authorizeExchange()
            .pathMatchers("/**")
            .permitAll()
            .and()
            .httpBasic().disable()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .build()
    }

}
