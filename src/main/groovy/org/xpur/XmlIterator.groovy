/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.xpur

import groovy.transform.CompileStatic

import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.EndElement
import javax.xml.stream.events.StartElement
import javax.xml.stream.events.XMLEvent

/**
 * Implementation of Iterator interface for huge XML files
 * Optimized for memory usage and read performance - should process GBs of XML with a few Mb of heap memory
 *
 * 1. scans XML for matching elements using StAX, without reading entire document into memory
 * 2. exports child nodes as POJO over Iterator interface - you can iterate, search, collect etc.
 * @author <a href="mailto:andrei@claz.org">Andrei Karneyenka</a>
 */
@CompileStatic
class XmlIterator implements Iterator<Map<String, Object>> {

    private XMLEventReader reader
    private String elementName
    private Map<String, Object> currentElement

    /**
     * Iterates over an XML file.
     * @param inputStream source
     * @param elementName name of child element to find
     * @param staxImpl custom StAX implementation
     */
    XmlIterator(InputStream inputStream, String elementName, XMLInputFactory staxImpl) {
        this.reader = staxImpl.createXMLEventReader(inputStream)
        this.elementName = elementName
    }

    /**
     * Iterates over an XML file.
     * Uses System default StAX implementation
     * @param inputStream source
     * @param elementName name of child element to find
     */
    XmlIterator(InputStream inputStream, String elementName) {
        this(inputStream, elementName, XMLInputFactory.newInstance()) //use default StAX impl
    }

    /**
     * Iterates over an XML file.
     * @param reader source
     * @param elementName name of child element to find
     * @param staxImpl custom StAX implementation
     */
    XmlIterator(Reader reader, String elementName, XMLInputFactory staxImpl) {
        this.reader = staxImpl.createXMLEventReader(reader)
        this.elementName = elementName
    }

    /**
     * Iterates over an XML file.
     * Uses System default StAX implementation
     * @param reader source
     * @param elementName name of child element to find
     */
    XmlIterator(Reader reader, String elementName) {
        this(reader, elementName, XMLInputFactory.newInstance()) //use default StAX impl
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
