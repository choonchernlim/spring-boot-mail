package com.github.choonchernlim.springbootmail.core

import spock.lang.Specification

class TextOutputServiceSpec extends Specification {

    def service = new TextOutputService()

    def flatMap = [
            'key1': 'value1',
            'key2': 'value2',
            'key3': '-',
            'key4': 'value4\nvalue4'
    ]

    def nestedMap = [
            'key1': 'value1',
            'key2': [:],
            'key3': [
                    'subkey3': 'value3',
                    'subkey4': 'value4'
            ],
            'key4': '',
            'key5': 'value5\nvalue5'
    ]

    def "getText - given null data map, should throw exception"() {
        when:
        service.getText(null, false)

        then:
        thrown AssertionError
    }

    def "getText - given flat data map as plain text, should plain text"() {
        when:
        def text = service.getText(flatMap, false)

        then:
        text == '''
--------------------------------------------------
key1
--------------------------------------------------
value1

--------------------------------------------------
key2
--------------------------------------------------
value2

--------------------------------------------------
key3
--------------------------------------------------
-

--------------------------------------------------
key4
--------------------------------------------------
value4
value4
'''
    }

    def "getText - given flat data map as HTML text, should HTML text"() {
        when:
        def text = service.getText(flatMap, true)

        then:
        text == '''
<h2>key1</h2>
<p>value1</p>

<h2>key2</h2>
<p>value2</p>

<h2>key3</h2>
<p>-</p>

<h2>key4</h2>
<pre>value4
value4</pre>
'''
    }

    def "getText - given nested data map as plain text, should plain text"() {
        when:
        def text = service.getText(nestedMap, false)

        then:
        text == '''
--------------------------------------------------
key1
--------------------------------------------------
value1

--------------------------------------------------
key2
--------------------------------------------------
-

--------------------------------------------------
key3
--------------------------------------------------
subkey3                   : value3
subkey4                   : value4

--------------------------------------------------
key4
--------------------------------------------------
-

--------------------------------------------------
key5
--------------------------------------------------
value5
value5
'''
    }

    def "getText - given nested data map as HTML text, should HTML text"() {
        when:
        def text = service.getText(nestedMap, true)

        then:
        text == '''
<h2>key1</h2>
<p>value1</p>

<h2>key2</h2>
<p>-</p>

<h2>key3</h2>
<table>
<tr><td width='250px'>subkey3</td><td>value3</td></tr>
<tr><td width='250px'>subkey4</td><td>value4</td></tr>
</table>

<h2>key4</h2>
<p>-</p>

<h2>key5</h2>
<pre>value5
value5</pre>
'''
    }

}
