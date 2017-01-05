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

    def "iterate stream with CDATA - happy path"() {
        def resource = getClass().getResourceAsStream("cars-cdata.xml")

        when:
        def result = new XmlIterator(resource, "car").collect { it }

        then: "properly terminated CData lines are parsed correctly"
        result[0] == [id: '1', make: 'Ford', model: 'Mustang GT', year: '2016']

        and: "don't fix and pass as-is if CData lines are not terminated / initiated properly"
        result[1].make == "\n            Honda\n        "
        result[1].model == "Accord\n        "
        result[1].year == "\n            2015"

    }

    def "iterate xml with simple tags"() {
        def resource = getClass().getResourceAsStream("ids.xml")

        when: "iterating xml with simple tags"
        def result = new XmlIterator(resource, "id").collect { it }

        then: "they must come as iterator of strings"
        result[0] == '1234'
        result[1] == '5678'
    }

    def "iterate root element"() {
        def resource = getClass().getResourceAsStream("car.xml")

        when: "iterating root element of the xml"
        def result = new XmlIterator(resource, "car").collect { it }

        then: "result must come as iterator with single element "
        result.size() == 1
        result[0] == [
                id   : '1',
                make : 'Ford',
                model: 'Mustang GT',
                year : '2016'
        ]
    }


    def "iterate deeply nested elements"() {
        def resource = getClass().getResourceAsStream("cars-nested.xml")

        when: "iterating deeply nested elements within xml, let's say dealer element within cars"
        def result = new XmlIterator(resource, "dealer").collect { it }

        then: "it should work exactly as with 1st level elements"
        result.size() == 2
        result[0] == [
                id   : '123',
                name : 'Cheap Autos',
                phone: '123-456-7890',
        ]
        result[1] == [
                id  : '456',
                name: 'Joe\'s Cars',

        ]
    }

    def "iterate deeply nested simple tags"() {
        def resource = getClass().getResourceAsStream("cars.xml")

        when: "iterating deeply nested simple tags within xml, let's say year within cars"
        def result = new XmlIterator(resource, "year").collect { it }

        then: "they must come as strings as with any other simple tags"
        result == ['2016', '2015']
    }

}
