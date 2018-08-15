package com.github.choonchernlim.springbootmail.core

import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.attribute.UserPrincipal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class DataExtractorServiceSpec extends Specification {

    def clock = Clock.fixed(Instant.parse('2015-08-04T10:11:00Z'), ZoneId.of('UTC'))

    def service = new DataExtractorService(clock)

    @Unroll
    def "#method - given null, should throw exception"() {
        when:
        service."$method"(null)

        then:
        thrown AssertionError

        where:
        method << ['getExceptionMap', 'getRequestMap']
    }

    def "getGeneralInfoMap - given invocation, should return datetime"() {
        when:
        def map = service.getGeneralInfoMap()

        then:
        map == [
                (FieldConstant.DATETIME): 'Tuesday, August 04, 2015 10:11 AM'
        ]
    }

    def "getExceptionMap - given exception, should return exception string"() {
        when:
        def map = service.getExceptionMap(new Exception('error'))

        then:
        map.size() == 1
        (map[FieldConstant.EXCEPTION] as String).trim().startsWith('error\njava.lang.Exception: error')
    }

    def "getRequestMap - given all info, should return fully populated request map"() {
        given:
        def request = new MockHttpServletRequest(
                userPrincipal: new UserPrincipal() {
                    String getName() {
                        return 'name'
                    }
                },
                remoteHost: 'remoteHost',
                remoteAddr: 'remoteAddr',
                requestURI: '/abc',
                method: 'GET',
                serverName: 'serverName',
                serverPort: 1234,
                parameters: ['param1': 'value1', 'param2': 'value2'],
                content: 'body'.bytes
        )

        request.addHeader('header2', 'value2')
        request.addHeader('header1', 'value1')

        when:
        def map = service.getRequestMap(request)

        then:
        map == [
                (FieldConstant.REQUEST_USER_ID)       : 'name',
                (FieldConstant.REQUEST_USER_HOST_NAME): 'remoteHost',
                (FieldConstant.REQUEST_REMOTE_ADDRESS): 'remoteAddr',
                (FieldConstant.REQUEST_METHOD)        : 'GET',
                (FieldConstant.REQUEST_URL)           : 'http://serverName:1234/abc',
                (FieldConstant.REQUEST_PARAMETERS)    : ['param1': 'value1', 'param2': 'value2'],
                (FieldConstant.REQUEST_BODY)          : 'body',
                (FieldConstant.REQUEST_HEADERS)       : ['header1': 'value1', 'header2': 'value2']
        ]
    }


    def "getRequestMap - given no additional info, should return partially populated request map"() {
        given:
        def request = new MockHttpServletRequest()

        when:
        def map = service.getRequestMap(request)

        then:
        map == [
                (FieldConstant.REQUEST_USER_ID)       : null,
                (FieldConstant.REQUEST_USER_HOST_NAME): 'localhost',
                (FieldConstant.REQUEST_REMOTE_ADDRESS): '127.0.0.1',
                (FieldConstant.REQUEST_METHOD)        : '',
                (FieldConstant.REQUEST_URL)           : 'http://localhost',
                (FieldConstant.REQUEST_PARAMETERS)    : [:],
                (FieldConstant.REQUEST_BODY)          : null,
                (FieldConstant.REQUEST_HEADERS)       : [:]
        ]
    }
}
