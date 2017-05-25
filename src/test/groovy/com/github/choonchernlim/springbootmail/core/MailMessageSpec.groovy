package com.github.choonchernlim.springbootmail.core

import spock.lang.Specification

import javax.validation.Validation

class MailMessageSpec extends Specification {

    def validator = Validation.buildDefaultValidatorFactory().validator

    def "test"() {
        given:
        def mailMessage = MailMessage.builder().
                from('from@github.com').
                tos(['to@github.com'] as Set).
                subject('subject').
                text('text').
                build()

        when:
        def errors = validator.validate(mailMessage)

        then:
        errors.isEmpty()
    }
}
