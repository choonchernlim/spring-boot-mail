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

/**
 * Service class to send email.
 */
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

    /**
     * Sends email.
     *
     * @param mailBean Mail bean
     */
    void send(final MailBean mailBean) {
        assert mailBean

        sendMail(mailBean)
    }

    /**
     * Sends email with exception info.
     *
     * @param mailBean Mail bean
     * @param exception Exception 
     */
    void sendException(final MailBean mailBean, final Exception exception) {
        assert mailBean
        assert exception

        sendMail(mailBean, exception)
    }

    /**
     * Sends email with exception info and additional request-related info.
     *
     * @param mailBean Mail bean
     * @param exception Exception
     */
    void sendWebException(final MailBean mailBean, final Exception exception) {
        assert mailBean
        assert exception

        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.requestAttributes)?.request

        assert request

        sendMail(mailBean, exception, request)
    }

    /**
     * Constructs email and send it.
     *
     * @param mailBean Mail bean
     * @param exception Exception if any
     * @param request Request if any
     */
    private void sendMail(final MailBean mailBean,
                          final Exception exception = null,
                          final HttpServletRequest request = null) {
        final Set<ConstraintViolation<MailBean>> violations = validator.validate(mailBean)

        if (!violations.isEmpty()) {
            throw new MailException(
                    "Errors found [${violations.size()}] when validating mail bean:-\n\n" +
                    violations.collect { "- ${it.propertyPath.toString()} - ${it.message}" }.join('\n')
            )
        }

        try {
            final MimeMessage mimeMessage = javaMailSender.createMimeMessage()
            final MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true)

            mimeMessageHelper.from = mailBean.from

            mailBean.tos.each {
                mimeMessageHelper.addTo(it)
            }

            mimeMessageHelper.subject = mailBean.subject

            mimeMessageHelper.setText(getText(exception, request, mailBean.text, mailBean.isHtmlText),
                                      mailBean.isHtmlText)

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
        }
        catch (Exception e) {
            throw new MailException("Unable to send mail [${mailBean}]", e)
        }
    }

    /**
     * Returns fully constructed email message.
     *
     * @param exception Exception
     * @param request Request
     * @param userText User text
     * @param isHtmlText Whether it is HTML or plain text
     * @return Email message
     */
    String getText(final Exception exception,
                   final HttpServletRequest request,
                   final String userText,
                   final boolean isHtmlText) {
        if (!exception) {
            return userText
        }

        final Map<String, Object> generalInfoMap = dataExtractorService.getGeneralInfoMap()
        final Map<String, Object> exceptionMap = dataExtractorService.getExceptionMap(exception)
        final Map<String, Object> requestMap = request ? dataExtractorService.getRequestMap(request) : [:]

        final Map<String, Object> dataMap = (generalInfoMap + requestMap + exceptionMap).asImmutable()
        final String generatedText = textOutputService.getText(dataMap, isHtmlText)

        return userText?.trim() ?
                userText + '\n\n' + generatedText :
                generatedText
    }
}
