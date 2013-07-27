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
package org.lazydog.persistence.history.internal;

import org.lazydog.persistence.history.HistoryTable;
import org.lazydog.persistence.history.HistoryTableFactory;

/**
 * History table factory implementation.
 *
 * @author  Ron Rickard
 */
public class HistoryTableFactoryImpl extends HistoryTableFactory {

    /**
     * Get the history table.
     *
     * @param  entity  the entity.
     *
     * @return  the history table.
     *
     * @throws  IllegalArgumentException  if unable to get the history table.
     */
    @Override
    public HistoryTable getHistoryTable(Object entity) {
        return HistoryTableImpl.newInstance(entity);
    }
}
