package org.lazydog.persistence.history;

import java.util.Date;


/**
 * History table manager.
 *
 * @author  Ron Rickard
 */
public interface HistoryTableManager {

    /**
     * Create the history table.
     *
     * @throws  HistoryTableException  if unable to create the history table.
     */
    public void create();

    /**
     * Check if the history table exists.
     *
     * @return  true if the history table exists, otherwise false.
     *
     * @throws  HistoryTableException  if unable to check if the history table exists.
     */
    public boolean exists();
    
    /**
     * Get the identifier for the specified entity.
     *
     * @param  entity  the entity.
     *
     * @return  the identifier for the specified entity.
     *
     * @throws  IllegalArgumentException  if the entity is invalid.
     */
    public Integer getId(Object entity);

    /**
     * Insert a row in the history table.
     *
     * @param  id          the row identifier.
     * @param  action      the action.
     * @param  actionBy    the action by.
     * @param  actionTime  the action time.
     *
     * @throws  HistoryTableException  if unable to insert a row in the history table.
     */
    public void insert(Integer id, Action action, String actionBy,  Date actionTime);

    /**
     * Populate the history table.
     *
     * @param  actionBy    the action by.
     * @param  actionTime  the action time.
     *
     * @throws  HistoryTableException  if unable to populate the history table.
     */
    public void populate(String actionBy, Date actionTime);
}
