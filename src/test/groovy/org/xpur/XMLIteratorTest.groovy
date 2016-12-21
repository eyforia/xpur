package org.xpur

import spock.lang.Specification

import static java.nio.charset.StandardCharsets.UTF_8

class XMLIteratorTest extends Specification {

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
        def result = new XMLIterator(resource, "car").collect { it }

        then:
        result == carsRefData
    }

    def "iterate reader - happy path"() {
        def reader = new InputStreamReader(getClass().getResourceAsStream("cars.xml"), UTF_8)

        when:
        def result = new XMLIterator(reader, "car").collect { it }

        then:
        result == carsRefData
    }


    def "empty valid xml - happy path"() {
        def resource = getClass().getResourceAsStream("cars-empty.xml")

        when:
        def result = new XMLIterator(resource, "car").collect { it }

        then:
        result == []
    }

    def "xml with no matches- happy path"() {
        def resource = getClass().getResourceAsStream("bikes.xml")

        when:
        def result = new XMLIterator(resource, "car").collect { it }

        then:
        result == []
    }

    def "iterate xml with namespaces - happy path"() {
        def resource = getClass().getResourceAsStream("cars-with-namespaces.xml")

        when:
        def result = new XMLIterator(resource, "car").collect { it }

        then:
        result == carsRefData
    }

}
