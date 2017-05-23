package com.github.choonchernlim.service

import org.springframework.core.io.InputStreamSource

class EmailBean {
    final String from
    final Set<String> tos
    final Set<String> bccs
    final Set<String> ccs
    final String subjectPrefix
    final String subject
    final String replyTo
    final Map<String, InputStreamSource> attachments
    final String textVelocityTemplatePath
    final Map<String, Object> textVelocityModel
    final Boolean isHtmlText
}
