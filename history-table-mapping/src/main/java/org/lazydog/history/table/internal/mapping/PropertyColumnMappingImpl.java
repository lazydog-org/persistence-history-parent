package org.lazydog.history.table.internal.mapping;

import org.lazydog.history.table.spi.mapping.PropertyColumnMapping;


/**
 * Property column mapping implementation.
 *
 * @author  Ron Rickard
 */
public final class PropertyColumnMappingImpl implements PropertyColumnMapping {

    private String entityClassProperty;
    private String entityTableColumn;
    private String historyTableColumn;

    /**
     * Get the entity class property.
     *
     * @return  the entity class property.
     */
    @Override
    public String getEntityClassProperty() {
        return this.entityClassProperty;
    }

    /**
     * Get the entity table column.
     *
     * @return  the entity table column.
     */
    @Override
    public String getEntityTableColumn() {
        return this.entityTableColumn;
    }

    /**
     * Get the history table column.
     *
     * @return  the history table column.
     */
    @Override
    public String getHistoryTableColumn() {
        return this.historyTableColumn;
    }

    /**
     * Set the entity class property.
     *
     * @param  entityClassProperty  the entity class property.
     */
    protected void setEntityClassProperty(String entityClassProperty) {
        this.entityClassProperty = entityClassProperty;
    }

    /**
     * Set the entity table column.
     *
     * @param  entityTableColumn  the entity table column.
     */
    protected void setEntityTableColumn(String entityTableColumn) {
        this.entityTableColumn = entityTableColumn;
    }

    /**
     * Set the history table column.
     *
     * @param  historyTableColumn  the history table column.
     */
    protected void setHistoryTableColumn(String historyTableColumn) {
        this.historyTableColumn = historyTableColumn;
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

        toString.append("PropertyColumnMappingImpl [");
        toString.append("entityClassProperty = ").append(this.getEntityClassProperty());
        toString.append(", entityTableColumn = ").append(this.getEntityTableColumn());
        toString.append(", historyTableColumn = ").append(this.getHistoryTableColumn());
        toString.append("]");

        return toString.toString();
    }
}
