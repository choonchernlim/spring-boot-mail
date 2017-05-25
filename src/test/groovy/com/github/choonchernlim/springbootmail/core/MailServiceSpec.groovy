package com.github.choonchernlim.springbootmail.core

import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import spock.lang.Specification

import javax.validation.Validation
import java.nio.file.attribute.UserPrincipal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class MailServiceSpec extends Specification {

    def clock = Clock.fixed(Instant.parse('2015-08-04T10:11:00Z'), ZoneId.systemDefault())

    def javaMailSender = new JavaMailSenderImpl()
    def dataExtractorService = new DataExtractorService(clock)

    def textOutputService = new TextOutputService()

    def validator = Validation.buildDefaultValidatorFactory().validator
    def mailService = new MailService(javaMailSender, dataExtractorService, textOutputService, validator)

    def request = new MockHttpServletRequest()

    def setup() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request))
    }

    def "test"() {
        given:
        request.userPrincipal = new UserPrincipal() {
            String getName() {
                return 'limc'
            }
        }

        request.addParameter('param1', 'value1')
        request.addParameter('param2', 'value2')
        request.addParameter('param3', '')

        request.setContent('body'.bytes)

        // @formatter:off
        request.addHeader('x-xsrf-token','dd4b82fa-a0b3-4d11-afb7-90bfc914b6e1')
        request.addHeader('Accept- Language','en-US,en;q=0.8')
        request.addHeader('Cookie','JSESSIONID=78d2rdv6rx3i1pc6j9811cuhv; XSRF-TOKEN=dd4b82fa-a0b3-4d11-afb7-90bfc914b6e1')
        request.addHeader('Host','localhost:8080')
        request.addHeader('Content-Length','1103')
        request.addHeader('origin','https://localhost:8080')
        request.addHeader('Referer','https://localhost:8080/app')
        request.addHeader('Accept-Encoding','gzip, deflate, br')
        request.addHeader('User-Agent','Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36')
        request.addHeader('Connection','close')
        request.addHeader('Content-Type','application/json')
        request.addHeader('Accept','*/*')
        request.addHeader('no-value','')
        // @formatter:on

        expect:
        mailService.sendWebException(
                new MailMessage(text: 'this is a body!',
                                isHtmlText: true,
                                from: 'from',
                                subject: 'subject'), // Subject must not be null
                new Exception('error'))
    }
}
