package com.github.choonchernlim.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest

@Service
class EmailService {

    final JavaMailSender javaMailSender
    final DataExtractor dataExtractor

    HttpServletRequest httpServletRequest

    @Autowired
    EmailService(final JavaMailSender javaMailSender, final DataExtractor dataExtractor) {
        this.javaMailSender = javaMailSender
        this.dataExtractor = dataExtractor
    }

    @Autowired(required = false)
    void setHttpServletRequest(final HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest
    }

    void send(final EmailBean emailBean) {
        assert emailBean

        handle(emailBean)
    }

    void sendException(final EmailBean emailBean, final Exception exception) {
        assert emailBean
        assert exception

        handle(emailBean, exception)
    }

    void sendWebException(final EmailBean emailBean, final Exception exception) {
        assert emailBean
        assert exception
        assert httpServletRequest

        handle(emailBean, exception, httpServletRequest)
    }

    private void handle(final EmailBean emailBean,
                        final Exception exception = null,
                        final HttpServletRequest request = null) {

        final Map<String, String> exceptionMap = exception ? dataExtractor.getExceptionMap(exception) : [:]
        final Map<String, Object> requestMap = request ? dataExtractor.getRequestMap(request) : [:]
        final Map<String, Object> dataMap = requestMap << exceptionMap
    }

    String getText(final Map<String, Object> dataMap, final Integer totalIndentations) {
        return dataMap.
                collect {
                    String value = it.value instanceof Map ?
                            getText((Map<String, Object>) it.value, totalIndentations + 1) :
                            it.value

                    return "${it.key}\n${getIndentations(totalIndentations + 1)}${value}"
                }.
                join("\n${getIndentations(totalIndentations)}")
    }

    private String getIndentations(final Integer totalIndentations) {
        return '\t' * totalIndentations
    }

    static void main(String[] args) {
        println '###################'
        println new EmailService(null, null).getText(
                [
                        'test' : 'value',
                        'test2': [
                                'test21': 'value2',
                                'test23': 'value4'
                        ]

                ], 1
        )
        println '###################'
    }
}
