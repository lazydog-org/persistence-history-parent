package org.lazydog.history.table.spi.mapping;


/**
 * Property column mapping.
 *
 * @author  Ron Rickard
 */
public interface PropertyColumnMapping {

    /**
     * Get the entity class property.
     *
     * @return  the entity class property.
     */
    public String getEntityClassProperty();

    /**
     * Get the entity table column.
     *
     * @return  the entity table column.
     */
    public String getEntityTableColumn();

    /**
     * Get the history table column.
     *
     * @return  the history table column.
     */
    public String getHistoryTableColumn();
}
