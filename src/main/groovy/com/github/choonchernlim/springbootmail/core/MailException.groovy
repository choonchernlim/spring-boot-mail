package com.github.choonchernlim.springbootmail.core

/**
 * Module exception class.
 */
class MailException extends RuntimeException {
    MailException(final String msg) {
        super(msg)
    }

    MailException(final String msg, final Throwable e) {
        super(msg, e)
    }
}
