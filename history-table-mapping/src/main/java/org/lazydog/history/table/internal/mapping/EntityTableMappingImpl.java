package org.lazydog.history.table.internal.mapping;

import java.util.List;
import org.lazydog.history.table.spi.mapping.EntityTableMapping;
import org.lazydog.history.table.spi.mapping.PropertyColumnMapping;


/**
 * Entity table mapping implementation.
 *
 * @author  Ron Rickard
 */
public final class EntityTableMappingImpl implements EntityTableMapping {

    private String entityClass;
    private String entityTable;
    private String historyTable;
    private List<PropertyColumnMapping> propertyColumnMappings;

    /**
     * Get the entity class.
     *
     * @return  the entity class.
     */
    @Override
    public String getEntityClass() {
        return this.entityClass;
    }

    /**
     * Get the entity table.
     *
     * @return  the entity table.
     */
    @Override
    public String getEntityTable() {
        return this.entityTable;
    }

    /**
     * Get the history table.
     *
     * @return  the history table.
     */
    @Override
    public String getHistoryTable() {
        return this.historyTable;
    }

    /**
     * Get the property column mappings.
     *
     * @return  the property column mappings.
     */
    @Override
    public List<PropertyColumnMapping> getPropertyColumnMappings() {
        return this.propertyColumnMappings;
    }

    /**
     * Set the entity class.
     *
     * @param  entityClass  the entity class.
     */
    protected void setEntityClass(String entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Set the entity table.
     *
     * @param  entityTable  the entity table.
     */
    protected void setEntityTable(String entityTable) {
        this.entityTable = entityTable;
    }

    /**
     * Set the history table.
     *
     * @param  historyTable  the history table.
     */
    protected void setHistoryTable(String historyTable) {
        this.historyTable = historyTable;
    }

    /**
     * Set the property column mappings.
     *
     * @param  propertyColumnMappings  the property column mappings.
     */
    protected void setPropertyColumnMappings(List<PropertyColumnMapping> propertyColumnMappings) {
        this.propertyColumnMappings = propertyColumnMappings;
    }

    /**
     * Get this object as a string.
     *
     * @return  this object as a string.
     */
    @Override
    public String toString() {

        // Declare.
        StringBuffer toString;

        // Initialize.
        toString = new StringBuffer();

        toString.append("EntityTableMappingImpl [");
        toString.append("entityClass = ").append(this.getEntityClass());
        toString.append(", entityTable = ").append(this.getEntityTable());
        toString.append(", historyTable = ").append(this.getHistoryTable());
        toString.append(", propertyColumnMappings = ").append(this.getPropertyColumnMappings());
        toString.append("]");

        return toString.toString();
    }
}
