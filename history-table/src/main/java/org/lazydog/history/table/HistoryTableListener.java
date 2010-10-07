package org.lazydog.history.table;

import java.lang.reflect.Method;
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
System.err.println("createTableSQL = " + historyTable.getCreateTableSQL());
            // Create the history table.
            historyTable.create();

            // Populate the history table.
        }
    }

    public void postChange(Object entity, Action action) {

        try {
            HistoryTable historyTable;

            historyTable = new HistoryTable(entity.getClass());

System.err.println("insertRowSQL = " + historyTable.getInsertRowSQL());
            Method method = entity.getClass().getMethod("getId", new Class[0]);
            Integer id = (Integer)method.invoke(entity, new Object[0]);
System.err.println("row = " + historyTable.getRow(id));

for (int x = 0; x < historyTable.getRows().size(); x++) {
    java.util.Map<String,Object> row = (java.util.Map<String,Object>)historyTable.getRows().get(x);
    System.err.println("rows row = " + row);
}
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
