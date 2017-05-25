package com.github.choonchernlim.springbootmail.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

import javax.validation.Validator
import java.time.Clock

@Configuration
@ComponentScan('com.github.choonchernlim.springbootmail.core')
class SpringBootMailConfig {
    @Bean
    Clock clock() {
        return Clock.systemDefaultZone()
    }

    @Bean
    Validator validator() {
        return new LocalValidatorFactoryBean()
    }
}
