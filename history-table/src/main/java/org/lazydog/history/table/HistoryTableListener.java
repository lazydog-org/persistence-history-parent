package org.lazydog.history.table;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;


/**
 * History table listener.
 *
 * @author  Ron Rickard
 */
public class HistoryTableListener {

    @PostPersist
    public void postPersist(Object entity) {
        doit(entity, Action.INSERT);
    }

    @PostRemove
    public void postRemove(Object entity) {
        doit(entity, Action.DELETE);
    }

    @PostUpdate
    public void postUpdate(Object entity) {
        doit(entity, Action.UPDATE);
    }

    public void doit(Object entity, Action action) {

        HistoryTable historyTable;

        historyTable = new HistoryTable();

System.err.println("createTableSQL = " + historyTable.getCreateTableSQL(entity.getClass()));
    }
}
