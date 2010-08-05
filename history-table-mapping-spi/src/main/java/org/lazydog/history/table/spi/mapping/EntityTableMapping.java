package org.lazydog.history.table.spi.mapping;

import java.util.List;


/**
 * Entity table mapping.
 *
 * @author  Ron Rickard
 */
public interface EntityTableMapping {

    /**
     * Get the entity class.
     *
     * @return  the entity class.
     */
    public String getEntityClass();

    /**
     * Get the entity table.
     *
     * @return  the entity table.
     */
    public String getEntityTable();

    /**
     * Get the history table.
     *
     * @return  the history table.
     */
    public String getHistoryTable();

    /**
     * Get the property column mappings.
     *
     * @return  the property column mappings.
     */
    public List<PropertyColumnMapping> getPropertyColumnMappings();
}
