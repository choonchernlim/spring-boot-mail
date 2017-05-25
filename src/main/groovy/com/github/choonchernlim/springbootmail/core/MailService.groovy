package com.github.choonchernlim.springbootmail.core

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

import javax.mail.internet.MimeMessage
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolation
import javax.validation.Validator

@Service
class MailService {

    final JavaMailSender javaMailSender
    final DataExtractorService dataExtractorService
    final TextOutputService textOutputService
    final Validator validator

    @Autowired
    MailService(final JavaMailSender javaMailSender,
                final DataExtractorService dataExtractorService,
                final TextOutputService textOutputService,
                final Validator validator) {
        this.javaMailSender = javaMailSender
        this.dataExtractorService = dataExtractorService
        this.textOutputService = textOutputService
        this.validator = validator
    }

    void send(final MailBean mailBean) {
        assert mailBean

        handle(mailBean)
    }

    void sendException(final MailBean mailBean, final Exception exception) {
        assert mailBean
        assert exception

        handle(mailBean, exception)
    }

    void sendWebException(final MailBean mailBean, final Exception exception) {
        assert mailBean
        assert exception

        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.requestAttributes).request

        assert request

        handle(mailBean, exception, request)
    }

    private void handle(final MailBean mailBean,
                        final Exception exception = null,
                        final HttpServletRequest request = null) {
        final Set<ConstraintViolation<MailBean>> violations = validator.validate(mailBean)

        println "Total violations: ${violations.size()}"

        violations.each {
            println it
        }

        final Map<String, Object> generalInfoMap = dataExtractorService.getGeneralInfo()
        final Map<String, Object> exceptionMap = exception ? dataExtractorService.getExceptionMap(exception) : [:]
        final Map<String, Object> requestMap = request ? dataExtractorService.getRequestMap(request) : [:]
        final Map<String, Object> dataMap = (generalInfoMap + requestMap + exceptionMap).asImmutable()

        final String text = mailBean.text + '\n\n' + textOutputService.getMessage(dataMap, mailBean.isHtmlText)

        MimeMessage mimeMessage = javaMailSender.createMimeMessage()
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true)

        mimeMessageHelper.from = mailBean.from

        mailBean.tos.each {
            mimeMessageHelper.addTo(it)
        }

        mimeMessageHelper.subject = mailBean.subject
        mimeMessageHelper.setText(text, mailBean.isHtmlText)

        mailBean.ccs?.each {
            mimeMessageHelper.addCc(it)
        }

        mailBean.bccs?.each {
            mimeMessageHelper.addBcc(it)
        }

        if (mailBean.replyTo?.trim()) {
            mimeMessageHelper.replyTo = mailBean.replyTo
        }

        mailBean.attachments?.each {
            mimeMessageHelper.addAttachment(it.key, it.value)
        }

        javaMailSender.send(mimeMessage)

        println text
    }
}
