package com.github.choonchernlim.springbootmail.core

import groovy.transform.PackageScope
import org.springframework.stereotype.Service

@Service
@PackageScope
class TextOutputService {

    static final int MAX_KEY_WIDTH = 50
    static final String SEPARATOR = '-' * MAX_KEY_WIDTH + '\n'

    String getMessage(final Map<String, Object> dataMap, final boolean isHtmlText) {
        assert dataMap

        return isHtmlText ? toHtmlText(dataMap) : toPlainText(dataMap)
    }

    private String toPlainText(final Map<String, Object> dataMap) {
        return dataMap.collect { SEPARATOR + it.key + '\n' + SEPARATOR + getTextValue(it.value) }.join('\n\n')
    }

    private String getTextValue(final Object value) {
        return setDefaultValueIfBlank(
                value instanceof Map ?
                        (value as TreeMap<String, Object>).
                                collect {
                                    it.key.padRight(MAX_KEY_WIDTH / 2) + ' : ' + setDefaultValueIfBlank(it.value)
                                }.
                                join('\n') :
                        value
        )
    }

    private String toHtmlText(final Map<String, Object> dataMap) {
        return dataMap.collect { "<h2>${it.key}</h2>\n" + getHtmlValue(it.value) }.join('\n\n')
    }

    private String getHtmlValue(final Object value) {
        if (value instanceof Map) {
            final Map<String, Object> valueMap = value as TreeMap

            return valueMap.isEmpty() ?
                    "<p>${setDefaultValueIfBlank(value)}</p>" :
                    '<table>\n' +
                    valueMap.
                            collect {
                                "<tr><td width='250px'>${it.key}</td><td>${setDefaultValueIfBlank(it.value)}</td></tr>"
                            }.
                            join('\n') +
                    '\n</table>'
        }

        final String valueString = setDefaultValueIfBlank(value)

        return valueString.contains('\n') ? "<pre>${valueString}</pre>" : "<p>${valueString}</p>"
    }

    private String setDefaultValueIfBlank(final Object value) {
        return value?.toString()?.trim() ?: '-'
    }
}
