package org.xpur

import spock.lang.Specification

import static java.nio.charset.StandardCharsets.UTF_8

class XmlIteratorTest extends Specification {

    List<Map<String, Object>> carsRefData = [
            [
                    id   : '1',
                    make : 'Ford',
                    model: 'Mustang GT',
                    year : '2016'
            ],
            [
                    id    : '2',
                    make  : 'Honda',
                    model : 'Accord',
                    year  : '2015',
                    dealer: [
                            id   : '345',
                            name : 'MyAutos',
                            phone: '123-456-7890'
                    ]
            ]
    ]

    def "iterate stream - happy path"() {
        def resource = getClass().getResourceAsStream("cars.xml")

        when:
        def result = new XmlIterator(resource, "car").collect { it }

        then:
        result == carsRefData
    }

    def "iterate reader - happy path"() {
        def reader = new InputStreamReader(getClass().getResourceAsStream("cars.xml"), UTF_8)

        when:
        def result = new XmlIterator(reader, "car").collect { it }

        then:
        result == carsRefData
    }


    def "empty valid xml - happy path"() {
        def resource = getClass().getResourceAsStream("cars-empty.xml")

        when:
        def result = new XmlIterator(resource, "car").collect { it }

        then:
        result == []
    }

    def "xml with no matches- happy path"() {
        def resource = getClass().getResourceAsStream("bikes.xml")

        when:
        def result = new XmlIterator(resource, "car").collect { it }

        then:
        result == []
    }

    def "iterate xml with namespaces - happy path"() {
        def resource = getClass().getResourceAsStream("cars-with-namespaces.xml")

        when:
        def result = new XmlIterator(resource, "car").collect { it }

        then:
        result == carsRefData
    }

    def "iterate xml with self-closing tags - happy path"() {
        def resource = getClass().getResourceAsStream("cars-self-closing-tags.xml")

        when:
        def result = new XmlIterator(resource, "car").collect { it }


        then: "self-closing tags are interpreted as empty object - they're not NULL and not characters, so can't be really cast to string"
        result[0].make == [:]

        and: "empty tags are the same - it's an object but has nothing inside - not a string content and no fields, so interpret as empty map"
        result[0].model == [:]

        and: "make sure top-level elements following the same rules"
        result[1] == [:]
        result[2] == [:]
    }

}
