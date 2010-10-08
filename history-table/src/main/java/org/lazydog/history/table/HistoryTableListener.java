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

        HistoryTable historyTable;

        historyTable = new HistoryTable(entity.getClass());

        // Check if the history table does not exist.
        if (!historyTable.exists()) {

            // Create the history table.
            historyTable.create();

            // Populate the history table.
            historyTable.populate(Action.INSERT, "000000000000000000000000000000000000", new Date());
        }
    }

    public void postChange(Object entity, Action action) {

        try {
            HistoryTable historyTable;
            Integer id;
            Method method;

            historyTable = new HistoryTable(entity.getClass());

            method = entity.getClass().getMethod("getId", new Class[0]);
            id = (Integer)method.invoke(entity, new Object[0]);
            historyTable.insert(id, action, "000000000000000000000000000000000000", new Date());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
