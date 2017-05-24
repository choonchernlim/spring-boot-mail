package com.github.choonchernlim.service

import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest

@Service
class DataExtractor {

    Map<String, String> getExceptionMap(final Exception exception) {
        assert exception

        final StringWriter stringWriter = new StringWriter()
        final PrintWriter printWriter = new PrintWriter(stringWriter)

        printWriter.println(exception.message)
        exception.printStackTrace(printWriter)

        return [
                (ErrorConstant.JAVA_STACKTRACE): stringWriter.toString()
        ].asImmutable()
    }

    Map<String, Object> getRequestMap(final HttpServletRequest request) {
        assert request

        return [
                (ErrorConstant.USER_ID)           : request.userPrincipal?.name, // TODO LIMC request.remoteUser?
                (ErrorConstant.USER_HOST_NAME)    : request.remoteHost,
                (ErrorConstant.REQUEST_URL)       : request.requestURL.toString(),
                (ErrorConstant.REQUEST_PARAMETERS): getRequestParameterMap(request),
                (ErrorConstant.REQUEST_BODY)      : request.reader.text,
                (ErrorConstant.REQUEST_HEADERS)   : getRequestHeaderMap(request)
        ].asImmutable()
    }

    private static Map<String, String> getRequestHeaderMap(final HttpServletRequest request) {
        return request.headerNames.iterator().
                collectEntries { [(it): request.getHeader(it)] }.
                asImmutable()
    }

    private static Map<String, String> getRequestParameterMap(final HttpServletRequest request) {
        return request.parameterNames.iterator().
                collectEntries { [(it): request.getParameter(it)] }.
                asImmutable()
    }
}
