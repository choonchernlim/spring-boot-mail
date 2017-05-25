package com.github.choonchernlim.springbootmail.core

import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.Validation

class MailBeanSpec extends Specification {

    def builder = MailBean.builder().
            from('from@github.com').
            tos(['to@github.com'] as Set).
            subject('subject').
            text('text')

    def validator = Validation.buildDefaultValidatorFactory().validator

    def "given no params, should have default values"() {
        when:
        def mailBean = MailBean.builder().build()

        then:

        mailBean.from == null
        mailBean.tos == null
        mailBean.bccs == null
        mailBean.ccs == null
        mailBean.subject == null
        mailBean.replyTo == null
        mailBean.attachments == null
        !mailBean.isHtmlText
        mailBean.text == null
    }

    def "given valid params, should not have validation errors"() {
        given:
        def mailBean = builder.build()

        when:
        def errors = validator.validate(mailBean)

        then:
        errors.isEmpty()
    }

    def "given no params, should have validation errors"() {
        given:
        def mailBean = MailBean.builder().build()

        when:
        def errors = validator.validate(mailBean)

        then:
        errors.size() == 4
        errors.collect { it.propertyPath.toString() }.sort() == ['from', 'subject', 'text', 'tos']
        errors.collect { it.message }.unique() == ['may not be empty']
    }

    @Unroll
    def "given #field with #label, should have validation errors"() {
        given:
        def mailBean = builder."${field}"(value).build()

        when:
        def errors = validator.validate(mailBean)

        then:
        errors.size() == 1
        errors[0].propertyPath.toString() == field
        errors[0].message == expectedError

        where:
        field     | label         | value                 | expectedError
        'from'    | 'blank value' | ''                    | 'may not be empty'
        'from'    | 'null value'  | null                  | 'may not be empty'
        'from'    | 'not email'   | 'from'                | 'not a well-formed email address'
        'tos'     | 'null value'  | null                  | 'may not be empty'
        'tos'     | 'empty set'   | [] as Set             | 'may not be empty'
        'tos'     | 'not email'   | ['to'] as Set         | 'some values do not have well-formed email addresses'
        'tos'     | 'not email'   | ['t@g.c', 't'] as Set | 'some values do not have well-formed email addresses'
        'bccs'    | 'not email'   | ['t@g.c', 't'] as Set | 'some values do not have well-formed email addresses'
        'ccs'     | 'not email'   | ['t@g.c', 't'] as Set | 'some values do not have well-formed email addresses'
        'subject' | 'blank value' | ''                    | 'may not be empty'
        'subject' | 'null value'  | null                  | 'may not be empty'
        'text'    | 'blank value' | ''                    | 'may not be empty'
        'text'    | 'null value'  | null                  | 'may not be empty'

    }
}
