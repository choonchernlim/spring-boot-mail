package com.github.choonchernlim.springbootmail.core

import javax.validation.Constraint
import javax.validation.Payload
import java.lang.annotation.*

/**
 * Annotation for validating a collection of emails.
 */
@Target([ElementType.FIELD, ElementType.PARAMETER])
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailCollectionValidator)
@Documented
@interface EmailCollection {
    String message() default 'some values do not have well-formed email addresses'

    Class<?>[] groups() default []

    Class<? extends Payload>[] payload() default []
}