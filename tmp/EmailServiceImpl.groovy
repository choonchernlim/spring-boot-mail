package com.github.choonchernlim.service

import static com.google.common.base.Preconditions.checkArgument
import static com.google.common.base.Preconditions.checkNotNull
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSortedMap
import edu.mayo.appdev.core.bean.EmailBean
import edu.mayo.appdev.core.bean.EmailBeanBuilder
import edu.mayo.appdev.core.service.CurrentUserService
import edu.mayo.appdev.core.service.EmailService
import edu.mayo.appdev.core.service.PropertyService
import org.apache.commons.lang3.StringUtils
import org.apache.velocity.app.VelocityEngine
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamSource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.stereotype.Service
import org.springframework.ui.velocity.VelocityEngineUtils

import javax.mail.internet.MimeMessage
import javax.servlet.http.HttpServletRequest
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Enumeration
import java.util.HashMap
import java.util.Map
import java.util.Set

@Service
class EmailServiceImpl {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern(
            "EEEE, MMMM dd, yyyy hh:mm a")
    private final JavaMailSender mailSender
    private final CurrentUserService currentUserService
    private final VelocityEngine velocityEngine
    private final PropertyService propertyService

    @Autowired
    EmailServiceImpl(JavaMailSender mailSender,
                     CurrentUserService currentUserService,
                     VelocityEngine velocityEngine,
                     PropertyService propertyService) {
        this.mailSender = mailSender
        this.currentUserService = currentUserService
        this.velocityEngine = velocityEngine
        this.propertyService = propertyService
    }

    @Override
    void emailDeveloperOnException(final String subjectPrefix, final Exception exception) {
        checkArgument(!StringUtils.isBlank(subjectPrefix), "subjectPrefix cannot be blank")
        //noinspection ThrowableResultOfMethodCallIgnored
        checkNotNull(exception, "Exception cannot be null")

        final StringWriter sw = new StringWriter(10000)
        exception.printStackTrace(new PrintWriter(sw))

        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder()
        builder.put("datetime", LocalDateTime.now().toString(DATE_TIME_FORMATTER))
        builder.put("lanId", currentUserService.getCurrentUser().getLanId())
        builder.put("error", sw.toString())

        sendMail(subjectPrefix,
                 "Unexpected Error has Occurred",
                 propertyService.getDeveloperEmails(),
                 "email-templates/email-developer-on-exception-simple.vm",
                 builder.build(),
                 true)
    }

    @Override
    void emailDeveloperOnException(final String subjectPrefix,
                                   final Exception exception,
                                   final HttpServletRequest request) {
        checkArgument(!StringUtils.isBlank(subjectPrefix), "subjectPrefix cannot be blank")
        checkNotNull(request, "Request cannot be null")
        //noinspection ThrowableResultOfMethodCallIgnored
        checkNotNull(exception, "Exception cannot be null")

        final StringWriter sw = new StringWriter(10000)
        exception.printStackTrace(new PrintWriter(sw))

        /*!
        When passing `Map` as value, using `ImmutableMap` will throw `java.lang.UnsupportedOperationException`,
        So, stick with just `Map` for now.
        */
        final Map<String, Object> map = new HashMap<String, Object>()
        map.put("datetime", LocalDateTime.now().toString(DATE_TIME_FORMATTER))
        map.put("server", request.getServerName() + ":" + request.getServerPort())
        map.put("uri", request.getRequestURI())

        final ImmutableSortedMap.Builder<String, String> parameterMapBuilder = ImmutableSortedMap.naturalOrder()
        final Enumeration parameterNames = request.getParameterNames()
        while (parameterNames.hasMoreElements()) {
            final String key = (String) parameterNames.nextElement()
            final String value = request.getParameter(key)
            parameterMapBuilder.put(key, value)
        }
        map.put("requestParameterMap", parameterMapBuilder.build())

        final ImmutableSortedMap.Builder<String, String> headerMapBuilder = ImmutableSortedMap.naturalOrder()
        final Enumeration headerNames = request.getHeaderNames()
        while (headerNames.hasMoreElements()) {
            final String key = (String) headerNames.nextElement()
            final String value = request.getHeader(key)
            headerMapBuilder.put(key, value)
        }
        map.put("requestHeaderMap", headerMapBuilder.build())

        map.put("remoteAddress", request.getRemoteAddr())
        map.put("lanId", currentUserService.getCurrentUser().getLanId())
        map.put("error", sw.toString())

        sendMail(subjectPrefix,
                 "Unexpected Error has Occurred",
                 propertyService.getDeveloperEmails(),
                 "email-templates/email-developer-on-exception.vm",
                 map,
                 true)
    }

    @Override
    void sendMail(final String subjectPrefix,
                  final String subject,
                  final Set<String> recipientEmails,
                  final String velocityTemplatePath,
                  final Map<String, Object> velocityModel) {

        sendMail(subjectPrefix,
                 subject,
                 recipientEmails,
                 velocityTemplatePath,
                 velocityModel,
                 false)
    }

    @Override
    void sendMail(final String subjectPrefix,
                  final String subject,
                  final Set<String> recipientEmails,
                  final String velocityTemplatePath,
                  final Map<String, Object> velocityModel,
                  final boolean isHTMLText) {
        sendMail(new EmailBean(
                subjectPrefix: subjectPrefix,
                subject: subject,
                tos: recipientEmails,
                textVelocityTemplatePath: velocityTemplatePath,
                textVelocityModel: velocityModel,
                isHtmlText: isHTMLText
        ))
    }


    @Override
    void sendMail(final EmailBean emailBean) {
        checkArgument(StringUtils.isNotBlank(emailBean.getFrom()), "from cannot be blank")
        checkArgument(StringUtils.isNotBlank(emailBean.getSubject()), "subject cannot be blank")
        checkArgument(StringUtils.isNotBlank(emailBean.getSubjectPrefix()), "subjectPrefix cannot be blank")
        checkNotNull(emailBean.getTos(), "tos cannot be null")
        checkArgument(!emailBean.getTos().isEmpty(), "tos cannot be empty")
        checkArgument(StringUtils.isNotBlank(emailBean.getTextVelocityTemplatePath()),
                      "textVelocityTemplatePath cannot be blank")
        checkNotNull(emailBean.getTextVelocityModel(), "textVelocityModel cannot be null")
        checkNotNull(emailBean.getIsHtmlText(), "isHtmlText cannot be null")

        /*!
        It is important to trim the resolved template string because adding `@vtlvariable` on the template
        causes additional blank lines on the top of the content.
        */
        final String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,
                                                                        emailBean.getTextVelocityTemplatePath(),
                                                                        "UTF-8",
                                                                        emailBean.getTextVelocityModel()).trim()

        /*! Multipart is set only if there is attachment. */
        final boolean isMultipart = !emailBean.getAttachments().isEmpty()

        final MimeMessagePreparator preparator = new MimeMessagePreparator() {
            void prepare(final MimeMessage mimeMessage) throws Exception {
                final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart)
                message.setFrom(emailBean.getFrom())
                message.setSubject(emailBean.getSubjectPrefix() + ": " + emailBean.getSubject())
                message.setText(text, emailBean.getIsHtmlText())

                for (String to : emailBean.getTos()) {
                    message.addTo(to)
                }

                for (String bcc : emailBean.getBccs()) {
                    message.addBcc(bcc)
                }

                for (String cc : emailBean.getCcs()) {
                    message.addCc(cc)
                }

                if (StringUtils.isNotBlank(emailBean.getReplyTo())) {
                    message.setReplyTo(emailBean.getReplyTo())
                }

                for (Map.Entry<String, InputStreamSource> attachment : emailBean.getAttachments().entrySet()) {
                    message.addAttachment(attachment.getKey(), attachment.getValue())
                }
            }
        }

        mailSender.send(preparator)
    }
}
