package com.github.choonchernlim.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest

@Service
class EmailService {

    static final int MAX_KEY_WIDTH = 50
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

    String getText(final Map<String, Object> dataMap, final Integer indent = 0) {
        return dataMap.
                collect {
                    final String value = it.value instanceof Map ?
                            '\n' + getText((Map<String, Object>) it.value, indent + 5) :
                            ': ' + it.value

                    return (' ' * indent) + it.key.padRight(MAX_KEY_WIDTH - indent) + value
                }.
                join("\n")
    }

    static void main(String[] args) {
        println '###################'
        println new EmailService(null, null).getText(
                [
                        'key1': 'value1',
                        'key2': [
                                'subkey3': 'value3',
                                'subkey4': [
                                        'subkey3': 'value3',
                                        'subkey4': 'value4'
                                ]
                        ],
                        'key3': 'value3'

                ]
        )
        println '###################'
    }
}
