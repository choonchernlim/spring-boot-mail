package com.github.choonchernlim.springbootmail.core

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * Validator to validate email values in non-empty collection.
 */
class EmailCollectionValidator implements ConstraintValidator<EmailCollection, Collection<String>> {
    final EmailValidator validator = new EmailValidator()

    @Override
    void initialize(final EmailCollection emailCollection) {
    }

    @Override
    boolean isValid(final Collection<String> emails, final ConstraintValidatorContext constraintValidatorContext) {
        return !emails || emails.every { validator.isValid(it, constraintValidatorContext) }
    }
}