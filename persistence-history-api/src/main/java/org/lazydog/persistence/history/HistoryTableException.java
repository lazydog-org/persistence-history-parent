/**
 * Copyright 2010-2013 lazydog.org.
 *
 * This file is part of persistence history.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.lazydog.persistence.history;

import java.io.Serializable;

/**
 * History table exception.
 *
 * @author  Ron Rickard
 */
public class HistoryTableException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 1L;
    private Object entity;

    /**
     * Constructs a new exception with no message.
     *
     * @param  entityClass  the entity class.
     *
     */
    public HistoryTableException(Object entity) {
        super();
        this.entity = entity;
    }

    /**
     * Constructs a new exception with the specified message.
     *
     * @param  entity   the entity .
     * @param  message  the message.
     */
    public HistoryTableException(Object entity, String message) {
        super(message);
        this.entity = entity;
    }

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param  entity   the entity.
     * @param  message  the message.
     * @param  cause    the cause.
     */
    public HistoryTableException(Object entity, String message, Throwable cause) {
        super(message, cause);
        this.entity = entity;
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param  entity  the entity.
     * @param  cause   the cause.
     */
    public HistoryTableException(Object entity, Throwable cause) {
        super(cause);
        this.entity = entity;
    }

    /**
     * Get the entity class.
     *
     * @return  the entity class.
     */
    public Object getEntity() {
        return this.entity;
    }
}
