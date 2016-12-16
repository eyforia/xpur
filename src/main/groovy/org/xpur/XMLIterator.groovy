package org.xpur

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.io.xml.StaxDriver
import groovy.transform.CompileStatic
import org.xpur.impl.ObjectInputStreamIterator
import org.xpur.impl.StringMapConverter;

@CompileStatic
public class XMLIterator {
    private String elementName

    XMLIterator(String elementName) {
        this.elementName = elementName
    }

    Iterator<Map> iterate(InputStream is) {
        ObjectInputStream ois = createParser(elementName).createObjectInputStream(is)
        return new ObjectInputStreamIterator<Map>(ois)
    }

    Iterator<Map> iterate(Reader reader) {
        ObjectInputStream ois = createParser(elementName).createObjectInputStream(reader)
        return new ObjectInputStreamIterator<Map>(ois)
    }


    private XStream createParser(String elementName) {
        XStream result = new XStream(new StaxDriver())
        result.aliasType(elementName, Map)
        result.registerConverter(new StringMapConverter())
        return result
    }

}
