package org.lazydog.persistence.history.internal;

import org.lazydog.persistence.history.HistoryTableManager;
import org.lazydog.persistence.history.HistoryTableManagerFactory;


/**
 * History table manager factory implementation.
 *
 * @author  Ron Rickard
 */
public class HistoryTableManagerFactoryImpl extends HistoryTableManagerFactory {

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
    @Override
    public HistoryTableManager createHistoryTableManager(Class entityClass) {

        // Declare.
        HistoryTableManagerImpl historyTableManager;

        // Create the history table manager.
        historyTableManager = new HistoryTableManagerImpl();
        historyTableManager.setEntityClass(entityClass);

        return historyTableManager;
    }
}
