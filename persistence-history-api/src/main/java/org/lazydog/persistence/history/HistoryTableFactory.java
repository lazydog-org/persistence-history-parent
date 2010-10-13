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
     * Create the history table.
     *
     * @param  entityClass  the entity class.
     * 
     * @return  the history table.
     *
     * @throws  IllegalArgumentException  if the entity class is invalid.
     */
    public abstract HistoryTable createHistoryTable(Class entityClass);

    /**
     * Get an instance of the history table factory.
     *
     * @return  the history table factory.
     *
     * @throws  IllegalArgumentException   if not exactly one factory is found.
     * @throws  ServiceConfigurationError  if unable to create the factory due
     *                                     to a provider configuration error.
     */
    public static synchronized HistoryTableFactory instance() {

        // Declare.
        HistoryTableFactory factory;
        ServiceLoader<HistoryTableFactory> factoryLoader;

        // Initialize.
        factory = null;
        factoryLoader = ServiceLoader.load(HistoryTableFactory.class);

        // Loop through the services.
        for (HistoryTableFactory loadedFactory : factoryLoader) {

            // Check if a factory has not been found.
            if (factory == null) {

                // Set the factory.
                factory = loadedFactory;
            }
            else {
                throw new IllegalArgumentException(
                    "More than one history table factory found.");
            }
        }

        // Check if a factory has not been found.
        if (factory == null) {
            throw new IllegalArgumentException(
                "No history table factory found.");
        }

        return factory;
    }
}
