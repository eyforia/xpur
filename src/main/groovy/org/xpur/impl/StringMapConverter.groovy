package org.xpur.impl

import com.thoughtworks.xstream.converters.Converter
import com.thoughtworks.xstream.converters.MarshallingContext
import com.thoughtworks.xstream.converters.UnmarshallingContext
import com.thoughtworks.xstream.io.HierarchicalStreamReader
import com.thoughtworks.xstream.io.HierarchicalStreamWriter
import groovy.transform.CompileStatic

@CompileStatic
public class StringMapConverter implements Converter {
    public boolean canConvert(Class clazz) {
        return Map.class.isAssignableFrom(clazz)
    }

    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        Map<String, Object> map = (Map<String, Object>) value
        map?.entrySet()?.each { entry ->
            writer.startNode(entry.key)
            writer.value = entry.value
            writer.endNode()
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Map<String, Object> result = new LinkedHashMap<>()

        if (reader.attributeCount) {
            reader?.attributeNames?.each { attr ->
                String name = (String) attr
                result[name] = reader.getAttribute(name)
            }
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown()
            result[reader.nodeName] = reader.hasMoreChildren() ? unmarshal(reader, context) : reader.value
            reader.moveUp()
        }
        return result
    }

}