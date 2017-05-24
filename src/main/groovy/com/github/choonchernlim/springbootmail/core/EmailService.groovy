package com.github.choonchernlim.springbootmail.core

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

import javax.mail.internet.MimeMessage
import javax.servlet.http.HttpServletRequest

@Service
class EmailService {

    final JavaMailSender javaMailSender
    final DataExtractorService dataExtractorService
    final TextOutputService messageService

    @Autowired
    EmailService(final JavaMailSender javaMailSender,
                 final DataExtractorService dataExtractorService,
                 final TextOutputService messageService) {
        this.javaMailSender = javaMailSender
        this.dataExtractorService = dataExtractorService
        this.messageService = messageService
    }

    void send(final MailMessage mailMessage) {
        assert mailMessage

        handle(mailMessage)
    }

    void sendException(final MailMessage mailMessage, final Exception exception) {
        assert mailMessage
        assert exception

        handle(mailMessage, exception)
    }

    void sendWebException(final MailMessage mailMessage, final Exception exception) {
        assert mailMessage
        assert exception

        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.requestAttributes).request

        assert request

        handle(mailMessage, exception, request)
    }

    private void handle(final MailMessage mailMessage,
                        final Exception exception = null,
                        final HttpServletRequest request = null) {

        final Map<String, Object> generalInfoMap = dataExtractorService.getGeneralInfo()
        final Map<String, Object> exceptionMap = exception ? dataExtractorService.getExceptionMap(exception) : [:]
        final Map<String, Object> requestMap = request ? dataExtractorService.getRequestMap(request) : [:]
        final Map<String, Object> dataMap = (generalInfoMap + requestMap + exceptionMap).asImmutable()

        final String text = mailMessage.text + '\n\n' + messageService.getMessage(dataMap, mailMessage.isHtmlText)

        MimeMessage mimeMessage = javaMailSender.createMimeMessage()
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true)

        mimeMessageHelper.from = mailMessage.from
        mimeMessageHelper.to = mailMessage.tos
        mimeMessageHelper.subject = mailMessage.subject
        mimeMessageHelper.setText(text, mailMessage.isHtmlText)

        mailMessage.ccs?.each {
            mimeMessageHelper.addCc(it)
        }

        mailMessage.bccs?.each {
            mimeMessageHelper.addBcc(it)
        }

        if (mailMessage.replyTo?.trim()) {
            mimeMessageHelper.replyTo = mailMessage.replyTo
        }

        mailMessage.attachments?.each {
            mimeMessageHelper.addAttachment(it.key, it.value)
        }

        javaMailSender.send(mimeMessage)

        println text
    }
}
