package org.lazydog.history.table.internal.mapping;

/**
 * Pair.
 *
 * @author  Ron Rickard
 */
public class Pair<T,U> implements Comparable<Pair<T,U>> {

    private final T key;
    private final U value;

    /**
     * Construct a new pair with the specified key and value.
     *
     * @param  key    the key.
     * @param  value  the value.
     *
     * @throws  NullPointerException  if the key or value is null.
     */
    public Pair(T key, U value) {

        if (key == null || value == null) {
            throw new NullPointerException("The ");
        }
        this.key = key;
        this.value = value;
    }

    /**
     * Compare this object to the specified object.
     *
     * @param  object  the object to compare this object against.
     *
     * @return  the value 0 if this object is equal to the object;
     *          a value less than 0 if this object is less than the object;
     *          and a value greater than 0 if this object is greater than the
     *          object.
     */
    @Override
    public int compareTo(Pair<T,U> object) {

        int compareTo;

        compareTo = 0;

        if (object.hashCode() > this.hashCode()) {
            compareTo = 1;
        }
        else if (object.hashCode() < this.hashCode()) {
            compareTo = -1;
        }

        return compareTo;
    }

    /**
     * Compare this object to the specified object.
     *
     * @param  object  the object to compare this object against.
     *
     * @return  true if the objects are equal; false otherwise.
     */
    @Override
    public boolean equals(Object object) {

        // Declare.
        boolean equals;

        // Initialize.
        equals = false;

        // Check if the object is an instance of this class
        // and is equal to this object.
        if (object instanceof Pair &&
            this.compareTo((Pair)object) == 0) {
            equals = true;
        }

        return equals;
    }

    /**
     * Get the key.
     *
     * @return  the key.
     */
    public T getKey() {
        return this.key;
    }

    /**
     * Get the value.
     *
     * @return  the value.
     */
    public U getValue() {
        return this.value;
    }

    /**
     * Returns a hash code for this object.
     *
     * @return  a hash code for this object.
     */
    @Override
    public int hashCode() {
        return this.getValue().hashCode()*31 + this.getKey().hashCode();
    }

    /**
     * Get this object as a String.
     *
     * @return  this object as a String.
     */
    @Override
    public String toString() {

        // Declare.
        StringBuffer toString;

        // Initialize.
        toString = new StringBuffer();

        toString.append("Pair [");
        toString.append("key = ").append(this.getKey());
        toString.append(", value = ").append(this.getValue());
        toString.append("]");

        return toString.toString();
    }
}
