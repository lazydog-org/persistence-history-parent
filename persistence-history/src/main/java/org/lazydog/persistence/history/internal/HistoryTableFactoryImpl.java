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
     * Create the history table.
     *
     * @param  entityClass  the entity class.
     *
     * @return  the history table.
     *
     * @throws  IllegalArgumentException
     */
    @Override
    public HistoryTable createHistoryTable(Class entityClass) {

        // Declare.
        HistoryTableImpl historyTable;

        // Create the Entry account manager.
        historyTable = new HistoryTableImpl();
        historyTable.setEntityClass(entityClass);

        return historyTable;
    }
}
