package org.xpur

import spock.lang.Specification

class XMLIteratorTest extends Specification {

    def "iterate stream - happy path"() {
        def resource = getClass().getResourceAsStream("cars.xml")

        when:
        def iterator = new XMLIterator("car")
        def result = iterator.iterate(resource)?.collect { it }

        then:
        result == [
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
                                name : 'MyAutos',
                                phone: '123-456-7890'
                        ]
                ]
        ]

    }

    def "iterate stream and print"() {
        def stream = new FileInputStream("/home/rolz/Downloads/listings.xml")
        int count = 0

        when:
        new XMLIterator("listing")
                .iterate(stream)
                .each { println "${it.url} ${it.title}" }
        println count

        then:
        notThrown(Exception)

    }

}
