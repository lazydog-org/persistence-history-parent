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

import java.util.ServiceLoader;

/**
 * History table factory.
 *
 * @author  Ron Rickard
 */
public abstract class HistoryTableFactory {

    /**
     * Protected constructor.
     */
    protected HistoryTableFactory() {
        // Do nothing.
    }

    /**
     * Get the history table.
     *
     * @param  entity  the entity .
     * 
     * @return  the history table.
     *
     * @throws  IllegalArgumentException  if unable to get the history table.
     */
    public abstract HistoryTable getHistoryTable(Object entity);

    /**
     * Create an instance of the history table factory.
     *
     * @return  the history table factory.
     *
     * @throws  IllegalArgumentException   if not exactly one factory is found.
     * @throws  ServiceConfigurationError  if unable to create the factory due to a provider configuration error.
     */
    public static synchronized HistoryTableFactory newInstance() {

        HistoryTableFactory factory = null;
        ServiceLoader<HistoryTableFactory> factoryLoader = ServiceLoader.load(HistoryTableFactory.class);

        // Loop through the services.
        for (HistoryTableFactory loadedFactory : factoryLoader) {

            // Check if a factory has not been found.
            if (factory == null) {

                // Set the factory.
                factory = loadedFactory;
            } else {
                throw new IllegalArgumentException("More than one history table factory found.");
            }
        }

        // Check if a factory has not been found.
        if (factory == null) {
            throw new IllegalArgumentException("No history table factory found.");
        }

        return factory;
    }
}
