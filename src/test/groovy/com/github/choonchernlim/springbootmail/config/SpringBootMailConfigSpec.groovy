package com.github.choonchernlim.springbootmail.config

import com.github.choonchernlim.springbootmail.core.MailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.validation.Validator
import java.time.Clock

@ContextConfiguration
class SpringBootMailConfigSpec extends Specification {

    @Configuration
    @Import(SpringBootMailConfig)
    static class TestConfig {
        @Bean
        JavaMailSender javaMailSender() {
            return new JavaMailSenderImpl()
        }
    }

    @Autowired
    MailService mailService

    @Autowired
    Clock clock

    @Autowired
    Validator validator

    def "given config, should autowire everything properly"() {
        expect:
        mailService
        clock
        validator
    }
}
