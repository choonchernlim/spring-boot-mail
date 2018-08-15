package com.github.choonchernlim.springbootmail.core

import groovy.transform.PackageScope
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * This class extracts selected info from the given object and returns them as a map.
 */
@Service
@PackageScope
@SuppressWarnings("GrMethodMayBeStatic")
class DataExtractorService {
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern('EEEE, MMMM dd, yyyy hh:mm a')

    final Clock clock

    @Autowired
    DataExtractorService(final Clock clock) {
        this.clock = clock
    }

    /**
     * Returns general info.
     *
     * @return General info map
     */
    Map<String, Object> getGeneralInfoMap() {
        return [
                (FieldConstant.DATETIME): DATE_TIME_FORMATTER.format(LocalDateTime.now(clock))
        ].asImmutable()
    }

    /**
     * Returns exception info.
     *
     * @param exception Exception
     * @return Exception map
     */
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

    /**
     * Returns request specific info.
     *
     * @param request Request
     * @return Request map
     */
    Map<String, Object> getRequestMap(final HttpServletRequest request) {
        assert request

        return [
                (FieldConstant.REQUEST_USER_ID)       : request.userPrincipal?.name,
                (FieldConstant.REQUEST_USER_HOST_NAME): request.remoteHost,
                (FieldConstant.REQUEST_REMOTE_ADDRESS): request.remoteAddr,
                (FieldConstant.REQUEST_METHOD)        : request.method,
                (FieldConstant.REQUEST_URL)           : request.requestURL.toString(),
                (FieldConstant.REQUEST_PARAMETERS)    : getRequestParameterMap(request),
                (FieldConstant.REQUEST_BODY)          : getRequestBody(request),
                (FieldConstant.REQUEST_HEADERS)       : getRequestHeaderMap(request)
        ].asImmutable()
    }

    /**
     * Returns request body.
     *
     * @param request Request
     * @return Request body if exist, otherwise null
     */
    private String getRequestBody(final HttpServletRequest request) {
        try {
            return request.reader.text ?: null
        }
        catch (Exception ignored) {
            return null
        }
    }

    /**
     * Returns all request headers.
     *
     * @param request Request
     * @return Request header map
     */
    private Map<String, String> getRequestHeaderMap(final HttpServletRequest request) {
        return request.headerNames.iterator().
                collectEntries { [(it): request.getHeader(it)] }.
                asImmutable()
    }

    /**
     * Returns all request parameters.
     *
     * @param request Request
     * @return Request parameter map
     */
    private Map<String, String> getRequestParameterMap(final HttpServletRequest request) {
        return request.parameterNames.iterator().
                collectEntries { [(it): request.getParameter(it)] }.
                asImmutable()
    }
}
