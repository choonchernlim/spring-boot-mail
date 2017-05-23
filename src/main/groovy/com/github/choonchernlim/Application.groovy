package com.github.choonchernlim

import com.github.choonchernlim.service.HelloWorldService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@Slf4j
@SpringBootApplication
class Application implements CommandLineRunner {

    final HelloWorldService helloWorldService

    @Autowired
    Application(final HelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService
    }

    @Override
    void run(final String... strings) {
        log.info(helloWorldService.message)
    }

    static void main(String[] args) {
        SpringApplication.run(Application, args)
    }
}