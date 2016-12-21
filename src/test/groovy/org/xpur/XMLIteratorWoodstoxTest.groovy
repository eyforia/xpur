package org.xpur

import org.codehaus.stax2.XMLInputFactory2
import spock.lang.Specification

import javax.xml.stream.XMLInputFactory

import static java.nio.charset.StandardCharsets.UTF_8

class XMLIteratorWoodstoxTest extends Specification {

    def "iterating stream with WoodStox produces same result as ref impl"() {
        def stream1 = getClass().getResourceAsStream("cars.xml")
        def stream2 = getClass().getResourceAsStream("cars.xml")

        def refStaxImpl = XMLInputFactory.newInstance()
        def woodstoxStaxImpl = XMLInputFactory2.newInstance()

        when:
        def refResult = new XMLIterator(stream1, "car", refStaxImpl).collect { it }
        def woodstoxResult = new XMLIterator(stream2, "car", woodstoxStaxImpl).collect { it }

        then:
        refResult && woodstoxResult
        refResult == woodstoxResult
    }

    def "iterating reader with WoodStox produces same result as ref impl"() {
        def reader1 = new InputStreamReader(getClass().getResourceAsStream("cars.xml"), UTF_8)
        def reader2 = new InputStreamReader(getClass().getResourceAsStream("cars.xml"), UTF_8)

        def refStaxImpl = XMLInputFactory.newInstance()
        def woodstoxStaxImpl = XMLInputFactory2.newInstance()

        when:
        def refResult = new XMLIterator(reader1, "car", refStaxImpl).collect { it }
        def woodstoxResult = new XMLIterator(reader2, "car", woodstoxStaxImpl).collect { it }

        then:
        refResult && woodstoxResult
        refResult == woodstoxResult
    }


}
