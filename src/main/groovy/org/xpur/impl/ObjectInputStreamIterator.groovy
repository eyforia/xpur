package org.xpur.impl

import groovy.transform.CompileStatic
import org.apache.commons.lang3.NotImplementedException

import static org.apache.commons.io.IOUtils.closeQuietly

@CompileStatic
public class ObjectInputStreamIterator<T> implements Iterable<T>, Iterator<T> {

    private T currentEntry
    private ObjectInputStream inputStream

    @Override
    public Iterator<T> iterator() {
        return this
    }

    public ObjectInputStreamIterator(ObjectInputStream inputStream) {
        this.inputStream = inputStream
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean hasNext() {
        if (inputStream == null)
            return false

        if (currentEntry != null)
            return true

        try {
            currentEntry = (T) inputStream.readObject()
        } catch (EOFException ex) { //reached end of stream
            closeQuietly(inputStream)
            inputStream = null
        } catch (IOException | ClassNotFoundException ex) {
            closeQuietly(inputStream)
            throw new IllegalStateException(ex)
        }

        return currentEntry != null
    }

    public T next() {
        if (!hasNext())
            throw new NoSuchElementException()
        T result = currentEntry
        currentEntry = null
        return result
    }

    public void remove() {
        throw new NotImplementedException("Operation is not supported")
    }

}
