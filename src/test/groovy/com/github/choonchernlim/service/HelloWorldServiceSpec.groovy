package com.github.choonchernlim.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class HelloWorldServiceSpec extends Specification {
    @Autowired
    HelloWorldService helloWorldService

    def "getMessage"() {
        expect:
        'Hello World' == helloWorldService.message
    }
}
