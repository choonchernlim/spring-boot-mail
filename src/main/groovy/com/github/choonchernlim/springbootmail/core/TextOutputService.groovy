package com.github.choonchernlim.springbootmail.core

import groovy.transform.PackageScope
import org.springframework.stereotype.Service

@Service
@PackageScope
@SuppressWarnings("GrMethodMayBeStatic")
class TextOutputService {

    static final int MAX_KEY_WIDTH = 50
    static final String PLAIN_TEXT_SEPARATOR = '-' * MAX_KEY_WIDTH + '\n'

    /**
     * Returns text constructed based on the given data map.
     *
     * @param dataMap Data map
     * @param isHtmlText `true` to generate HTML text, otherwise `false` to generate plain text
     * @return Constructed text
     */
    String getText(final Map<String, Object> dataMap, final boolean isHtmlText) {
        assert dataMap

        return isHtmlText ?
                '\n' + toHtmlText(dataMap) + '\n' :
                '\n' + toPlainText(dataMap) + '\n'
    }

    /**
     * Returns plain text.
     *
     * @param dataMap Data map
     * @return Plain text
     */
    private String toPlainText(final Map<String, Object> dataMap) {
        return dataMap.
                collect { PLAIN_TEXT_SEPARATOR + it.key + '\n' + PLAIN_TEXT_SEPARATOR + getTextValue(it.value) }.
                join('\n\n')
    }

    /**
     * Returns formatted plain text.
     *
     * @param value Value
     * @return Formatted plain text
     */
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

    /**
     * Returns HTML text.
     *
     * @param dataMap Data map
     * @return HTML text
     */
    private String toHtmlText(final Map<String, Object> dataMap) {
        return dataMap.collect { "<h2>${it.key}</h2>\n" + getHtmlValue(it.value) }.join('\n\n')
    }

    /**
     * Returns formatted HTML text.
     *
     * @param value Value
     * @return Formatted HTML text
     */
    private String getHtmlValue(final Object value) {
        if (value instanceof Map) {
            final Map<String, Object> valueMap = value as TreeMap

            return valueMap.isEmpty() ?
                    "<p>-</p>" :
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

    /**
     * Returns a default value if the value is blank.
     *
     * @param value Value
     * @return Existing string value if not blank, otherwise `-` if blank
     */
    private String setDefaultValueIfBlank(final Object value) {
        return value?.toString()?.trim() ?: '-'
    }
}
