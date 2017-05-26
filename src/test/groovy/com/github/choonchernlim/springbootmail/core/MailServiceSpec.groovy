package com.github.choonchernlim.springbootmail.core

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
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
import javax.mail.util.SharedByteArrayInputStream
import javax.validation.Validation
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class MailServiceSpec extends Specification {
    static class ExpectedContent {
        String content
        String contentType
    }

    def smtpServer = new GreenMail(new ServerSetup(65438, 'localhost', 'smtp'))
    def javaMailSender = new JavaMailSenderImpl(port: 65438, host: 'localhost')

    def clock = Clock.fixed(Instant.parse('2015-08-04T10:11:00Z'), ZoneId.systemDefault())
    def dataExtractorService = new DataExtractorService(clock)
    def textOutputService = new TextOutputService()
    def validator = Validation.buildDefaultValidatorFactory().validator
    def mailService = new MailService(javaMailSender, dataExtractorService, textOutputService, validator)

    def request = new MockHttpServletRequest()

    def setup() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request))
        smtpServer.start()
    }

    def cleanup() {
        smtpServer.stop()
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

    def "send - given valid mail bean, should send email"() {
        when:
        mailService.send(new MailBean(
                from: 'from@github.com',
                replyTo: 'replyTo@github.com',
                tos: ['to@github.com'] as Set,
                ccs: ['cc@github.com'] as Set,
                bccs: ['bcc@github.com'] as Set,
                subject: 'subject',
                text: 'text',
                attachments: ['filename.pdf': new ByteArrayResource('file-data'.bytes)]
        ))

        then:
        assertMimeMessage(
                'from@github.com',
                'replyTo@github.com',
                ['to@github.com'],
                ['cc@github.com'],
                ['bcc@github.com'],
                'subject',
                [
                        new ExpectedContent(content: 'text', contentType: 'text/plain'),
                        new ExpectedContent(content: 'file-data', contentType: 'application/pdf; name=filename.pdf')
                ])
    }

    void assertMimeMessage(String from,
                           String replyTo,
                           List<String> tos,
                           List<String> ccs,
                           List<String> bccs,
                           String subject,
                           List<ExpectedContent> expectedContents) {
        def messages = smtpServer.getReceivedMessages()

        messages.size() == 1

        def message = messages[0]

        assert getEmails(message.from) == [from]
        assert getEmails(message.replyTo) == [replyTo]
        assert getEmails(message, RecipientType.TO) == tos
        assert getEmails(message, RecipientType.CC) == ccs

        // TODO LIMC ERROR!
        assert getEmails(message, RecipientType.BCC) == bccs
        assert message.subject == subject

        def parentPart = (Multipart) message.content

        assert parentPart.count == expectedContents.size()

        expectedContents.eachWithIndex { ExpectedContent expectedContent, int i ->
            assertContent(parentPart, i, expectedContent.content, expectedContent.contentType)
        }
    }

    void assertContent(Multipart parentPart, int partIndex, String expectedContent, String expectedContentType) {
        def bodyPart = parentPart.getBodyPart(partIndex)
        def content = bodyPart.content

        if (content instanceof MimeMultipart) {
            def childBodyPart = ((Multipart) content).getBodyPart(0)

            assert childBodyPart.contentType.contains(expectedContentType)
            assert childBodyPart.content == expectedContent
        }
        else if (content instanceof SharedByteArrayInputStream) {
            assert bodyPart.contentType.contains(expectedContentType)
            assert ((SharedByteArrayInputStream) content).text == expectedContent
        }
        else {
            assert false
        }
    }

    def getEmails(MimeMessage mimeMessage, RecipientType recipientType) {
        return getEmails(mimeMessage.getRecipients(recipientType))
    }

    def getEmails(Address[] addresses) {
        return addresses.collect { it.toString() }
    }
}
