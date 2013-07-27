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

import java.util.Date;

/**
 * History table.
 *
 * @author  Ron Rickard
 */
public interface HistoryTable {

    public enum Action {
        INITIAL,
        INSERT,
        UPDATE,
        DELETE;
    };
    
    /**
     * Create the history table.
     *
     * @throws  HistoryTableException  if unable to create the history table.
     */
    public void create() throws HistoryTableException;

    /**
     * Check if the history table exists.
     *
     * @return  true if the history table exists, otherwise false.
     *
     * @throws  HistoryTableException  if unable to check if the history table exists.
     */
    public boolean exists() throws HistoryTableException;

    /**
     * Insert a row in the history table.
     * 
     * @param  action      the action.
     * @param  actionBy    the action by.
     * @param  actionTime  the action time.
     *
     * @throws  HistoryTableException  if unable to insert a row in the history table.
     */
    public void insert(HistoryTable.Action action, String actionBy, Date actionTime) throws HistoryTableException;

    /**
     * Populate the history table.
     *
     * @param  actionBy    the action by.
     * @param  actionTime  the action time.
     *
     * @throws  HistoryTableException  if unable to populate the history table.
     */
    public void populate(String actionBy, Date actionTime) throws HistoryTableException;
}
