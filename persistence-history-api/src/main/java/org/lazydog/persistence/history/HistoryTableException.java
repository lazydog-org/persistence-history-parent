package org.lazydog.persistence.history;

import java.io.Serializable;


/**
 * History table exception.
 *
 * @author  Ron Rickard
 */
public class HistoryTableException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 1L;
    private Class entityClass;

    /**
     * Constructs a new exception with no message.
     *
     * @param  entityClass  the entity class.
     *
     */
    public HistoryTableException(Class entityClass) {
        super();
        this.entityClass = entityClass;
    }

    /**
     * Constructs a new exception with the specified message.
     *
     * @param  entityClass  the entity class.
     * @param  message      the message.
     */
    public HistoryTableException(Class entityClass, String message) {
        super(message);
        this.entityClass = entityClass;
    }

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param  entityClass  the entity class.
     * @param  message      the message.
     * @param  cause        the cause.
     */
    public HistoryTableException(Class entityClass, String message, Throwable cause) {
        super(message, cause);
        this.entityClass = entityClass;
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param  entityClass  the entity class.
     * @param  cause        the cause.
     */
    public HistoryTableException(Class entityClass, Throwable cause) {
        super(cause);
        this.entityClass = entityClass;
    }

    /**
     * Get the entity class.
     *
     * @return  the entity class.
     */
    public Class getEntityClass() {
        return this.entityClass;
    }
}
