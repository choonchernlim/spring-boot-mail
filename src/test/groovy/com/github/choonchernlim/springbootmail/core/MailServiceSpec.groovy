package com.github.choonchernlim.springbootmail.core

import org.springframework.core.io.ByteArrayResource
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import spock.lang.Specification
import spock.lang.Unroll

import javax.mail.Address
import javax.mail.Message.RecipientType
import javax.mail.Multipart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.validation.Validation
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class MailServiceSpec extends Specification {
    static class ExpectedContent {
        String content
        String contentType
        String fileName
    }

    def javaMailSender = new JavaMailSenderImpl() {
        MimeMessage mimeMessage

        void send(final MimeMessage mimeMessage) throws org.springframework.mail.MailException {
            this.mimeMessage = mimeMessage

            if (mimeMessage.subject == 'force exception') {
                throw new Exception('force exception')
            }
        }
    }

    def clock = Clock.fixed(Instant.parse('2015-08-04T10:11:00Z'), ZoneId.systemDefault())
    def dataExtractorService = new DataExtractorService(clock)
    def textOutputService = new TextOutputService()
    def validator = Validation.buildDefaultValidatorFactory().validator
    def mailService = new MailService(javaMailSender, dataExtractorService, textOutputService, validator)

    def request = new MockHttpServletRequest()

    def setup() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request))
    }

    @Unroll
    def "send - given #label, should throw #expectedException"() {
        when:
        mailService.send(mailBean)

        then:
        thrown expectedException

        where:
        label          | mailBean       | expectedException
        'null bean'    | null           | AssertionError
        'invalid bean' | new MailBean() | MailException
    }

    @Unroll
    def "sendException - given #label, should throw #expectedException"() {
        when:
        mailService.sendException(mailBean, exception)

        then:
        thrown expectedException

        where:
        label            | mailBean       | exception       | expectedException
        'null bean'      | null           | new Exception() | AssertionError
        'null exception' | new MailBean() | null            | AssertionError
        'invalid bean'   | new MailBean() | new Exception() | MailException
    }

    @Unroll
    def "sendWebException - given #label, should throw #expectedException"() {
        given:
        def requestAttributes = httpServletRequest ? new ServletRequestAttributes(httpServletRequest) : null
        RequestContextHolder.setRequestAttributes(requestAttributes)

        when:
        mailService.sendWebException(mailBean, exception)

        then:
        thrown expectedException

        where:
        label            | mailBean       | exception       | httpServletRequest           | expectedException
        'null bean'      | null           | new Exception() | new MockHttpServletRequest() | AssertionError
        'null exception' | new MailBean() | null            | new MockHttpServletRequest() | AssertionError
        'null request'   | new MailBean() | new Exception() | null                         | AssertionError
        'invalid bean'   | new MailBean() | new Exception() | new MockHttpServletRequest() | MailException
    }

    def "send - given mail bean with all valid fields, should send email"() {
        when:
        mailService.send(new MailBean(
                from: 'from@github.com',
                replyTo: 'replyTo@github.com',
                tos: ['to@github.com'] as Set,
                ccs: ['cc@github.com'] as Set,
                bccs: ['bcc@github.com'] as Set,
                subject: 'subject',
                text: 'text',
                attachments: [
                        'file.pdf': new ByteArrayResource('file-pdf'.bytes),
                        'file.doc': new ByteArrayResource('file-doc'.bytes)
                ]
        ))

        then:
        assertMimeMessage(
                'from@github.com',
                'replyTo@github.com',
                ['to@github.com'],
                ['cc@github.com'],
                ['bcc@github.com'],
                'subject',
                [new ExpectedContent(content: 'text', contentType: 'text/plain'),
                 new ExpectedContent(content: 'file-pdf', contentType: 'application/pdf', fileName: 'file.pdf'),
                 new ExpectedContent(content: 'file-doc', contentType: 'application/msword', fileName: 'file.doc')
                ])
    }

    def "send - given mail bean with only valid fields, should send email"() {
        when:
        mailService.send(new MailBean(
                from: 'from@github.com',
                tos: ['to@github.com'] as Set,
                subject: 'subject',
                text: 'text'
        ))

        then:
        assertMimeMessage(
                'from@github.com',
                'from@github.com',
                ['to@github.com'],
                [],
                [],
                'subject',
                [new ExpectedContent(content: 'text', contentType: 'text/plain')])
    }

    def "send - given unexpected error when sending mail, should throw exception"() {
        when:
        mailService.send(new MailBean(
                from: 'from@github.com',
                tos: ['to@github.com'] as Set,
                subject: 'force exception',
                text: 'text'
        ))

        then:
        thrown MailException
    }


    def "sendException - given mail bean with exception, should have text with user text and exception"() {
        when:
        mailService.sendException(
                new MailBean(
                        from: 'from@github.com',
                        tos: ['to@github.com'] as Set,
                        subject: 'subject',
                        text: 'my message'
                ),
                new Exception('my exception'))

        then:

        def text = getText()

        text.startsWith('my message')
        text.contains('my exception')
        text.contains(FieldConstant.DATETIME)
        text.contains(FieldConstant.EXCEPTION)
        !text.contains(FieldConstant.REQUEST_USER_ID)
    }

    def "sendWebException - given mail bean with exception, should have text with user text, exception and request"() {
        when:
        mailService.sendWebException(
                new MailBean(
                        from: 'from@github.com',
                        tos: ['to@github.com'] as Set,
                        subject: 'subject',
                        text: 'my message'
                ),
                new Exception('my exception'))

        then:

        def text = getText()

        text.startsWith('my message')
        text.contains('my exception')
        text.contains(FieldConstant.DATETIME)
        text.contains(FieldConstant.EXCEPTION)
        text.contains(FieldConstant.REQUEST_USER_ID)
    }

    String getText() {
        def message = javaMailSender.mimeMessage
        def parentPart = (Multipart) message.content
        def bodyPart = parentPart.getBodyPart(0)
        def content = bodyPart.content
        def childBodyPart = ((Multipart) content).getBodyPart(0)

        return childBodyPart.content
    }

    void assertMimeMessage(String from,
                           String replyTo,
                           List<String> tos,
                           List<String> ccs,
                           List<String> bccs,
                           String subject,
                           List<ExpectedContent> expectedContents) {
        def message = javaMailSender.mimeMessage

        assert getEmails(message.from) == [from]
        assert getEmails(message.replyTo) == [replyTo]
        assert getEmails(message, RecipientType.TO) == tos
        assert getEmails(message, RecipientType.CC) == ccs
        assert getEmails(message, RecipientType.BCC) == bccs
        assert message.subject == subject

        def parentPart = (Multipart) message.content

        assert parentPart.count == expectedContents.size()

        expectedContents.eachWithIndex { ExpectedContent expectedContent, int i ->
            assertContent(parentPart, i, expectedContent)
        }
    }

    void assertContent(Multipart parentPart, int partIndex, ExpectedContent expectedContent) {
        def bodyPart = parentPart.getBodyPart(partIndex)
        def content = bodyPart.content

        // non-attachment
        if (content instanceof MimeMultipart) {
            assert bodyPart.disposition == null
            assert bodyPart.dataHandler.name == expectedContent.fileName

            def childBodyPart = ((Multipart) content).getBodyPart(0)

            assert childBodyPart.contentType.contains(expectedContent.contentType)
            assert childBodyPart.content == expectedContent.content
        }
        // attachment
        else if (content instanceof ByteArrayInputStream) {
            assert bodyPart.disposition == 'attachment'
            assert bodyPart.dataHandler.contentType == expectedContent.contentType
            assert bodyPart.dataHandler.name == expectedContent.fileName
            assert ((ByteArrayInputStream) content).text == expectedContent.content
        }
        else {
            assert false: "${content.class} not handled"
        }
    }

    def getEmails(MimeMessage mimeMessage, RecipientType recipientType) {
        return getEmails(mimeMessage.getRecipients(recipientType))
    }

    def getEmails(Address[] addresses) {
        return addresses.collect { it.toString() }
    }
}
