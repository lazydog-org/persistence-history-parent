/**
 * Copyright 2010-2013 lazydog.org.
 *
 * This file is part of persistence history.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.lazydog.persistence.history.listener;

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
import org.lazydog.persistence.history.HistoryTable;
import org.lazydog.persistence.history.HistoryTableFactory;

/**
 * History table listener.
 *
 * @author  Ron Rickard
 */
public class HistoryTableListener {

    /**
     * Get the username
     *
     * @return  the username.
     */
    private String getUsername() {

        String username = null;

        try {

            // Get the username.
            Context context = new InitialContext();
            EJBContext ejbContext = (EJBContext)context.lookup("java:comp/EJBContext");
            username = ejbContext.getCallerPrincipal().getName();
        } catch (NamingException e) {
            // Ignore.
        }

        return username;
    }

    /**
     * Process post persist.
     *
     * @param  entity  the entity.
     */
    @PostPersist
    public void postPersist(Object entity) {
        insertHistoryTable(entity, HistoryTable.Action.INSERT);
    }

    /**
     * Process post remove.
     *
     * @param  entity  the entity.
     */
    @PostRemove
    public void postRemove(Object entity) {
        insertHistoryTable(entity, HistoryTable.Action.DELETE);
    }

    /**
     * Process post update.
     *
     * @param  entity  the entity.
     */
    @PostUpdate
    public void postUpdate(Object entity) {
        insertHistoryTable(entity, HistoryTable.Action.UPDATE);
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

        // Get the history table.
        HistoryTable historyTable = HistoryTableFactory.newInstance().getHistoryTable(entity.getClass());

        // Check if the history table does not exist.
        if (!historyTable.exists()) {

            // Create the history table.
            historyTable.create();

            // Populate the history table.
            historyTable.populate("initial_creation", new Date());
        }
    }

    /**
     * Insert a row into the history table.
     *
     * @param  entity  the entity.
     * @param  action  the action.
     */
    private void insertHistoryTable(Object entity, HistoryTable.Action action) {

        // Get the history table.
        HistoryTable historyTable = HistoryTableFactory.newInstance().getHistoryTable(entity);

        // Insert a row into the history table.
        historyTable.insert(action, this.getUsername(), new Date());
    }
}
