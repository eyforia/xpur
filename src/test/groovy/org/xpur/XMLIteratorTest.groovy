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

}
