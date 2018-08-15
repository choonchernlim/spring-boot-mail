# spring-boot-mail [![Build Status](https://travis-ci.org/choonchernlim/spring-boot-mail.svg?branch=master)](https://travis-ci.org/choonchernlim/spring-boot-mail) [![codecov](https://codecov.io/gh/choonchernlim/spring-boot-mail/branch/master/graph/badge.svg)](https://codecov.io/gh/choonchernlim/spring-boot-mail)

Email module for sending emails.
 
* Works with Groovy and Java.
* Works with Spring Boot and regular Spring.

This will be the last email service I will ever write in my whole lifetime.

## Maven Dependency

```xml
<dependency>
    <groupId>com.github.choonchernlim</groupId>
    <artifactId>spring-boot-mail</artifactId>
    <version>0.1.2</version>
</dependency>
```

## MailBean Properties

|Property                  |Required? |Description                                                                                                         |
|--------------------------|----------|--------------------------------------------------------------------------------------------------------------------|
|from                      |Yes       |Sender email.                                                                                                       |
|tos                       |Yes       |Recipient email(s).                                                                                            |
|subject                   |Yes       |Subject line.                                                                                                       |
|text                      |Yes       |Email message.                                                                                                      |
|replyTo                   |No        |Email for user to reply to. <br/><br/>Default is value from `from` property.                                        |
|ccs                       |No        |Carbon copy emails.                                                                                                 |
|bccs                      |No        |Blind carbon copy emails.                                                                                           |
|attachments               |No        |Map of attachments when `key` is the filename and extension and `value` is the file.                                |
|isHtmlText                |No        |`true` to render message as HTML, otherwise `false` to render message as plain text. <br/><br/>Default is `false`.  | 

## MailService API

|Method                                  |Description                                                                                                       |
|----------------------------------------|------------------------------------------------------------------------------------------------------------------|
|`send(MailBean)`                        |Sends email.                                                                                                      |
|`sendException(MailBean, Exception)`    |Sends email where email message contains `mailBean.text` and properly formatted caught exception.                 |
|`sendWebException(MailBean, Exception)` |Sends email where email message contains `mailBean.text`, properly formatted caught exception and pertinent information from `HttpServletRequest` object for debugging purpose. |


The email message will be formatted accordingly based on `mailBean.isHtmlText`.

## Usage

### With Spring Boot

Spring Boot automatically supplies `JavaMailSender` if any `spring.mail` namespace exists.

##### `application.yml`

Typically, all you need is to specify your institution's SMTP hostname:-

```yml
spring.mail.host: [YOUR_SMTP_HOST]
```

... or, if you are using Google Mail:-

```yml
spring.mail:
  host: smtp.gmail.com
  username: [YOUR_EMAIL]@gmail.com
  password: [YOUR_PASSWORD]
  properties.mail.smtp:
    auth: true
    socketFactory:
      port: 465
      class: javax.net.ssl.SSLSocketFactory
      fallback: false
```

... but to get it working properly using Google Mail, go to https://myaccount.google.com/secureaccount first and set "Allow less secure apps" to "ON".

##### Spring Configuration

Create a Spring Configuration that imports `SpringBootMailConfig`:-

```groovy
@Configuration
@Import(SpringBootMailConfig)
class AppConfig {
}
```

### Without Spring Boot

Besides importing `SpringBootMailConfig`, you also have to supply `JavaMailSender` with all the mail properties:-

```groovy
@Configuration
@Import(SpringBootMailConfig)
class AppConfig {
    @Bean
    JavaMailSender javaMailSender() {
        new JavaMailSenderImpl(
                host: [YOUR_SMTP_HOST]
        )
    }
}
```

### Sending Mail

At the simplest level, this is all you need to send an email:-

```groovy
@Autowired
MailService mailService

mailService.send(MailBean.builder().
                     from('from@github.com').
                     tos(['to@github.com'] as Set).
                     subject('subject').
                     text('text').
                     build())
```