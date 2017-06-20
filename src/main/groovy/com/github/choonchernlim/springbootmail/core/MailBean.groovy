package com.github.choonchernlim.springbootmail.core

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotBlank
import org.hibernate.validator.constraints.NotEmpty
import org.springframework.core.io.InputStreamSource

/**
 * All information regarding the email to be sent.
 */
@Builder(builderClassName = 'MailBeanBuilder')
@EqualsAndHashCode
@ToString(includeNames = true)
class MailBean {
    /**
     * (Required) Sender email.
     */
    @NotBlank
    @Email
    String from

    /**
     * (Required) Main recipient emails.
     */
    @NotEmpty
    @EmailCollection
    Set<String> tos

    /**
     * Blind carbon copy emails.
     */
    @EmailCollection
    Set<String> bccs

    /**
     * Carbon copy emails.
     */
    @EmailCollection
    Set<String> ccs

    /**
     * (Required) Subject line.
     */
    @NotBlank
    String subject

    /**
     * Email for user to reply to.
     */
    String replyTo

    /**
     * Attachments when `key` is the filename and extension and `value` is the file.
     */
    Map<String, InputStreamSource> attachments

    /**
     * `true` if it is HTML text, otherwise `false` if it is plain text.
     */
    boolean isHtmlText

    /**
     * Email message.
     */
    @NotBlank
    String text
}
