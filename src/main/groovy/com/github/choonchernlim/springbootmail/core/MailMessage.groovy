package com.github.choonchernlim.springbootmail.core

import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotBlank
import org.hibernate.validator.constraints.NotEmpty
import org.springframework.core.io.InputStreamSource

@Builder
@ToString(includeNames = true)
class MailMessage {

    @NotBlank
    @Email
    String from

    @NotEmpty
    @EmailCollection
    Set<String> tos = []

    @EmailCollection
    Set<String> bccs = []

    @EmailCollection
    Set<String> ccs = []

    @NotBlank
    String subject

    String replyTo

    Map<String, InputStreamSource> attachments = [:]

    boolean isHtmlText = false

    @NotBlank
    String text
}
