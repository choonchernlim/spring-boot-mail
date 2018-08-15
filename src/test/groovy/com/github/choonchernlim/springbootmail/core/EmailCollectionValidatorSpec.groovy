package com.github.choonchernlim.springbootmail.core

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl
import org.hibernate.validator.internal.engine.path.PathImpl
import spock.lang.Specification
import spock.lang.Unroll

class EmailCollectionValidatorSpec extends Specification {

    def context = new ConstraintValidatorContextImpl(null, null, PathImpl.createRootPath(), null, null)
    def validator = new EmailCollectionValidator()

    @Unroll
    def "isValid - given #label, should be #expectedBool"() {
        when:
        def actualBool = validator.isValid(collection, context)

        then:
        actualBool == expectedBool

        where:
        label                 | collection             | expectedBool
        'null value'          | null                   | true
        'empty value'         | []                     | true
        'one valid email'     | ['a@a.com']            | true
        'all valid emails'    | ['a@a.com', 'b@b.com'] | true
        'one invalid email'   | ['aaaa']               | false
        'some invalid emails' | ['aaaa', 'a@a.com']    | false
    }
}
