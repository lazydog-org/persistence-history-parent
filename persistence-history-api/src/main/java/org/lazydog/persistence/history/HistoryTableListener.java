package org.lazydog.persistence.history;

import java.util.Date;
import javax.ejb.EJBContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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

    private String getUsername() {

        // Declare.
        String username;

        // Initialize.
        username = null;

        try {

            // Declare.
            Context context;
            EJBContext ejbContext;

            // Get the caller principal name.
            context = new InitialContext();
            ejbContext = (EJBContext)context.lookup("java:comp/EJBContext");
            username = ejbContext.getCallerPrincipal().getName();
        }
        catch (NamingException e) {
            // Ignore.
        }

        return username;
    }

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

        historyTable = HistoryTableFactory.instance().createHistoryTable(entity.getClass());

        // Check if the history table does not exist.
        if (!historyTable.exists()) {

            // Create the history table.
            historyTable.create();

            // Populate the history table.
            historyTable.populate("000000000000000000000000000000000000", new Date());
        }
        System.err.println("username = " + this.getUsername());
    }

    public void postChange(Object entity, Action action) {

        // Declare.
        HistoryTable historyTable;

        historyTable = HistoryTableFactory.instance().createHistoryTable(entity.getClass());

        historyTable.insert(historyTable.getId(entity), action, "000000000000000000000000000000000000", new Date());

        System.err.println("username = " + this.getUsername());
    }
}
