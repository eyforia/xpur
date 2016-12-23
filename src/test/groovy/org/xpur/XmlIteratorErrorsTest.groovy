package org.xpur

import spock.lang.Specification

import javax.xml.stream.XMLStreamException

class XmlIteratorErrorsTest extends Specification {

    def "malformed xml must generate an error"() {
        def resource = getClass().getResourceAsStream("cars-malformed.xml")

        when:
        new XmlIterator(resource, "car").collect { it }

        then:
        thrown(XMLStreamException)
    }
}
