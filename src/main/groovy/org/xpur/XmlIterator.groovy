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
 *
 * This iterator returns 2 types of objects:
 *  1. Map - if the element has child elements or is a self-closing tag (e.g. <secure/>)
 *  2. String - if the element is a simple tag, e.g. <name>John</name>
 *
 * @author <a href="mailto:andrei@claz.org">Andrei Karneyenka</a>
 */
@CompileStatic
class XmlIterator implements Iterator {

    private XMLEventReader reader
    private String elementName
    private Object currentElement

    /**
     * Iterates over an XML file.
     *
     * This iterator returns 2 types of objects:
     *  1. Map - if the element has child elements or is a self-closing tag (e.g. <secure/>)
     *  2. String - if the element is a simple tag, e.g. <name>John</name>
     *
     * @param inputStream source
     * @param elementName name of child element to find or null if all elements are to be returned
     */
    XmlIterator(InputStream inputStream, String elementName) {
        this.reader = XMLInputFactory.newInstance().createXMLEventReader(inputStream)
        this.elementName = elementName
    }

    /**
     * Iterates over an XML file.
     *
     * This iterator returns 2 types of objects:
     *  1. Map - if the element has child elements or is a self-closing tag (e.g. <secure/>)
     *  2. String - if the element is a simple tag, e.g. <name>John</name>
     *
     * @param reader source
     * @param elementName name of child element to find or null if all elements are to be returned
     */
    XmlIterator(Reader reader, String elementName) {
        this.reader = XMLInputFactory.newInstance().createXMLEventReader(reader)
        this.elementName = elementName
    }

    @Override
    boolean hasNext() {
        if (currentElement == null && reader.hasNext())
            currentElement = findNext()

        return currentElement != null
    }

    /**
     * This method will return either of 2 types of objects:
     *  1. Map - if the element has child elements or is a self-closing tag (e.g. <secure/>)
     *  2. String - if the element is a simple tag, e.g. <name>John</name>
     *
     * @return
     */
    @Override
    Object next() {
        Object result = currentElement
        currentElement = null
        return result
    }

    /**
     * This method decides whether an element is to be returned by the iterator.
     * The base implementation matches a specified elementName, though subclasses
     * may redefine this element search.
     *
     * @param startElement element
     * @return true iff an element is to be returned by the iterator
     */
    protected boolean isDesired(StartElement startElement) {
        elementName == null || startElement.name?.localPart == elementName
    }

    private Object findNext() {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent()
            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement()
                if (isDesired(startElement)) {
                    return create(startElement)
                }
            }
        }
        return null
    }

    /**
     * add field to an object - should handle primitives and implicit collections
     * @param to
     * @param field
     * @param value
     */
    private void put(Map<String, Object> to, String field, Object value) {
        def existingValue = to[field]

        if (existingValue == null) {
            to.put(field,value)
        } else if (existingValue instanceof List) { //existing implicit collection
            (existingValue as List).add(value)
        } else {  //new implicit collection
            to[field] = [existingValue, value]
        }

    }


    private Object create(StartElement startElement) {
        Map<String, Object> result = new LinkedHashMap<>()
        StringBuilder text = null
        boolean hadChildren = false

        //add attributes first
        Iterator<Attribute> attributes = (Iterator<Attribute>) startElement.attributes
        attributes?.each { Attribute attr -> result.put(attr.name.localPart, attr.value) }

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent()
            if (event.isStartElement()) {
                StartElement child = event.asStartElement()
                def value = create(child)
                put(result, child.name.localPart, value)
                hadChildren = true
            }

            if (event.isCharacters() && !event.asCharacters().isIgnorableWhiteSpace()) {
                if (text == null)
                    text = new StringBuilder()
                text.append(event.asCharacters().getData())
            }

            if (event.isEndElement()) {
                EndElement endElement = event.asEndElement()
                if (endElement.name == startElement.name) {
                    break
                }

                if (text != null) {
                    put(result,endElement.name.localPart,text.toString())
                    text = null
                }
            }
        }

        if (hadChildren)
            text = null

        if (result && text)
            result[null] = text
        return (text?.toString()) ?: result
    }

}
