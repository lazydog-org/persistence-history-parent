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
import org.lazydog.entry.EntryService;


/**
 * History table listener.
 *
 * @author  Ron Rickard
 */
public class HistoryTableListener {

    private static final String NIL_UUID = "000000000000000000000000000000000000";

    /**
     * Get the universally unique identifier (UUID).
     *
     * @return  the universally unique identifier (UUID).
     */
    private String getUuid() {

        // Declare.
        String uuid;

        // Initialize.
        uuid = NIL_UUID;

        try {

            // Declare.
            Context context;
            EJBContext ejbContext;
            String username;

            // Get the username.
            context = new InitialContext();
            ejbContext = (EJBContext)context.lookup("java:comp/EJBContext");
            username = ejbContext.getCallerPrincipal().getName();

            // Check if the username exists.
            if (username != null) {

                // Declare.
                EntryService entryService;

                // Get the entry service.
                entryService = (EntryService)context.lookup("ejb/EntryService");

                // Get the UUID.
                uuid = entryService.getUserProfile(username).getUuid();
            }
        }
        catch (NamingException e) {
            // Ignore.
        }

        return uuid;
    }

    /**
     * Process post persist.
     *
     * @param  entity  the entity.
     */
    @PostPersist
    public void postPersist(Object entity) {
        insertHistoryTable(entity, Action.INSERT);
    }

    /**
     * Process post remove.
     *
     * @param  entity  the entity.
     */
    @PostRemove
    public void postRemove(Object entity) {
        insertHistoryTable(entity, Action.DELETE);
    }

    /**
     * Process post update.
     *
     * @param  entity  the entity.
     */
    @PostUpdate
    public void postUpdate(Object entity) {
        insertHistoryTable(entity, Action.UPDATE);
    }

    /**
     * Create the history table for the entity if it does not exist.
     *
     * @param  entity  the entity.
     */
    @PrePersist
    @PreRemove
    @PreUpdate
    public void createHistoryTable(Object entity) {

        // Declare.
        HistoryTableManager manager;

        // Get the history table manager.
        manager = HistoryTableManagerFactory.instance().createHistoryTableManager(entity.getClass());

        // Check if the history table does not exist.
        if (!manager.exists()) {

            // Create the history table.
            manager.create();

            // Populate the history table.
            manager.populate(NIL_UUID, new Date());
        }
    }

    /**
     * Insert a row into the history table.
     *
     * @param  entity  the entity.
     * @param  action  the action.
     */
    public void insertHistoryTable(Object entity, Action action) {

        // Declare.
        HistoryTableManager manager;

        // Get the history table manager.
        manager = HistoryTableManagerFactory.instance().createHistoryTableManager(entity.getClass());

        // Insert a row into the history table.
        manager.insert(manager.getId(entity), action, this.getUuid(), new Date());

        
    }
}
