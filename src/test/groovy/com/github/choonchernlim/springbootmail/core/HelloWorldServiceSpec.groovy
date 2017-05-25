package com.github.choonchernlim.springbootmail.core

import com.github.choonchernlim.springbootmail.config.SpringBootMailConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import spock.lang.Specification

@ContextConfiguration
class HelloWorldServiceSpec extends Specification {

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

    def setup() {
        MockHttpServletRequest request = new MockHttpServletRequest()
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request))
    }

    def "getMessage"() {
        expect:
        mailService.sendWebException(new MailBean(text: 'body'), new Exception('test'))

        true
    }
}
