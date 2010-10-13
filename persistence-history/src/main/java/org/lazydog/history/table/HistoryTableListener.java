package org.lazydog.history.table;

import java.lang.reflect.Method;
import java.util.Date;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;


/**
 * History table listener.
 *
 * @author  Ron Rickard
 */
public class HistoryTableListener {

    @PostPersist
    public void postPersist(Object entity) {
        postChange(entity, Action.INSERT);
    }

    @PostRemove
    public void postRemove(Object entity) {
        postChange(entity, Action.DELETE);
    }

    @PostUpdate
    public void postUpdate(Object entity) {
        postChange(entity, Action.UPDATE);
    }

    @PrePersist
    @PreRemove
    @PreUpdate
    public void setupHistoryTable(Object entity) {

        // Declare.
        HistoryTable historyTable;

        historyTable = HistoryTable.newInstance(entity.getClass());

        // Check if the history table does not exist.
        if (!historyTable.exists()) {

            // Create the history table.
            historyTable.create();

            // Populate the history table.
            historyTable.populate("000000000000000000000000000000000000", new Date());
        }
    }

    public void postChange(Object entity, Action action) {

        // Declare.
        HistoryTable historyTable;

        historyTable = HistoryTable.newInstance(entity.getClass());

        historyTable.insert(historyTable.getId(entity), action, "000000000000000000000000000000000000", new Date());
    }
}
