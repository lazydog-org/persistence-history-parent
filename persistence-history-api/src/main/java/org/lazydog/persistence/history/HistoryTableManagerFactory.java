package org.lazydog.persistence.history;

import java.util.ServiceLoader;


/**
 * History table manager factory.
 *
 * @author  Ron Rickard
 */
public abstract class HistoryTableManagerFactory {

    /**
     * Protected constructor.
     */
    protected HistoryTableManagerFactory() {
        // Do nothing.
    }

    /**
     * Create the history table manager.
     *
     * @param  entityClass  the entity class.
     * 
     * @return  the history table manager.
     *
     * @throws  IllegalArgumentException  if unable to create the history table
     *                                    manager.
     */
    public abstract HistoryTableManager createHistoryTableManager(Class entityClass);

    /**
     * Get an instance of the history table manager factory.
     *
     * @return  the history table manager factory.
     *
     * @throws  IllegalArgumentException   if not exactly one factory is found.
     * @throws  ServiceConfigurationError  if unable to create the factory due
     *                                     to a provider configuration error.
     */
    public static synchronized HistoryTableManagerFactory instance() {

        // Declare.
        HistoryTableManagerFactory factory;
        ServiceLoader<HistoryTableManagerFactory> factoryLoader;

        // Initialize.
        factory = null;
        factoryLoader = ServiceLoader.load(HistoryTableManagerFactory.class);

        // Loop through the services.
        for (HistoryTableManagerFactory loadedFactory : factoryLoader) {

            // Check if a factory has not been found.
            if (factory == null) {

                // Set the factory.
                factory = loadedFactory;
            }
            else {
                throw new IllegalArgumentException(
                    "More than one history table manager factory found.");
            }
        }

        // Check if a factory has not been found.
        if (factory == null) {
            throw new IllegalArgumentException(
                "No history table manager factory found.");
        }

        return factory;
    }
}
