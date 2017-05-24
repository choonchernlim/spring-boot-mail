package com.github.choonchernlim.springbootmail.core

import groovy.transform.PackageScope
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@PackageScope
class DataExtractorService {
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern('EEEE, MMMM dd, yyyy hh:mm a')

    final Clock clock

    @Autowired
    DataExtractorService(final Clock clock = Clock.systemDefaultZone()) {
        this.clock = clock
    }

    Map<String, Object> getGeneralInfo() {
        return [
                (FieldConstant.DATETIME): DATE_TIME_FORMATTER.format(LocalDateTime.now(clock))
        ].asImmutable()
    }

    Map<String, Object> getExceptionMap(final Exception exception) {
        assert exception

        final StringWriter stringWriter = new StringWriter()
        final PrintWriter printWriter = new PrintWriter(stringWriter)

        printWriter.println(exception.message)
        exception.printStackTrace(printWriter)

        return [
                (FieldConstant.EXCEPTION): stringWriter.toString()
        ].asImmutable()
    }

    Map<String, Object> getRequestMap(final HttpServletRequest request) {
        assert request

        return [
                (FieldConstant.REQUEST_USER_ID)       : request.userPrincipal?.name,
                (FieldConstant.REQUEST_USER_HOST_NAME): request.remoteHost,
                (FieldConstant.REQUEST_REMOTE_ADDRESS): request.remoteAddr,
                (FieldConstant.REQUEST_URL)           : request.requestURL.toString(),
                (FieldConstant.REQUEST_PARAMETERS)    : getRequestParameterMap(request),
                (FieldConstant.REQUEST_BODY)          : request.reader.text,
                (FieldConstant.REQUEST_HEADERS)       : getRequestHeaderMap(request)
        ].asImmutable()
    }

    private Map<String, String> getRequestHeaderMap(final HttpServletRequest request) {
        return request.headerNames.iterator().
                collectEntries { [(it): request.getHeader(it)] }.
                asImmutable()
    }

    private Map<String, String> getRequestParameterMap(final HttpServletRequest request) {
        return request.parameterNames.iterator().
                collectEntries { [(it): request.getParameter(it)] }.
                asImmutable()
    }
}
