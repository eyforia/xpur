package org.xpur

import groovy.transform.CompileStatic

import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.EndElement
import javax.xml.stream.events.StartElement
import javax.xml.stream.events.XMLEvent

@CompileStatic
class XMLIterator implements Iterable<Map<String, Object>>, Iterator<Map<String, Object>> {

    private XMLEventReader reader
    private String elementName
    private Map<String, Object> currentElement

    XMLIterator(InputStream inputStream, String elementName) {
        XMLInputFactory factory = XMLInputFactory.newInstance()
        this.reader = factory.createXMLEventReader(inputStream)
        this.elementName = elementName
    }

    XMLIterator(Reader reader, String elementName) {
        XMLInputFactory factory = XMLInputFactory.newInstance()
        this.reader = factory.createXMLEventReader(reader)
        this.elementName = elementName
    }

    @Override
    Iterator<Map> iterator() {
        return this
    }

    @Override
    boolean hasNext() {
        if (currentElement == null && reader.hasNext())
            currentElement = findNext(elementName)

        return currentElement != null
    }

    @Override
    Map<String, Object> next() {
        Map<String, Object> result = currentElement
        currentElement = null
        return result
    }

    private Map<String, Object> findNext(String elementName) {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent()
            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement()
                if (startElement.name?.localPart == elementName) {
                    return create(startElement)
                }
            }
        }
        return null
    }

    private Map<String, Object> create(StartElement startElement) {
        Map<String, Object> result = new LinkedHashMap<>()
        //add attributes first
        Iterator<Attribute> attributes = (Iterator<Attribute>) startElement.attributes
        attributes?.each { Attribute attr -> result.put(attr.name.localPart, attr.value) }

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent()
            if (event.isStartElement()) {
                StartElement element = event.asStartElement()
                XMLEvent next = reader.peek()
                if (next.characters && !next.asCharacters().whiteSpace) {   //no nested tags
                    result.put(element.name.localPart, reader.getElementText())
                } else { //has nested tags
                    result.put(element.name.localPart, create(element))
                }
            }

            if (event.isEndElement()) {
                EndElement endElement = event.asEndElement()
                if (endElement.name == startElement.name) {
                    break
                }

            }
        }
        return result
    }

}
