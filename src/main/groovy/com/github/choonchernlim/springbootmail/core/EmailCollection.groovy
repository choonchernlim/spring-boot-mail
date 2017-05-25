package com.github.choonchernlim.springbootmail.core

import javax.validation.Constraint
import javax.validation.Payload
import java.lang.annotation.*

@Target([ElementType.FIELD, ElementType.PARAMETER])
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailCollectionValidator)
@Documented
@interface EmailCollection {
    String message() default 'At least one item does not have a well-formed email address'

    Class<?>[] groups() default []

    Class<? extends Payload>[] payload() default []
}