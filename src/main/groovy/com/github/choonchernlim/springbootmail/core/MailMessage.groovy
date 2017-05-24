package com.github.choonchernlim.springbootmail.core

import groovy.transform.Immutable
import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.springframework.core.io.InputStreamSource

@Builder
@Immutable
@ToString(includeNames = true)
class MailMessage {
    String from
    Set<String> tos
    Set<String> bccs
    Set<String> ccs
    String subject
    String replyTo
    Map<String, InputStreamSource> attachments
    boolean isHtmlText = false
    String text
}
