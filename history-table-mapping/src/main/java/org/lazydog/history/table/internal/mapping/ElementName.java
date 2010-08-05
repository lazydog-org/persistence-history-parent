package org.lazydog.history.table.internal.mapping;


/**
 * Element name.
 *
 * @author  Ron Rickard
 */
public enum ElementName {
    ENTITY_CLASS,
    ENTITY_CLASS_PROPERTY,
    ENTITY_TABLE,
    ENTITY_TABLE_COLUMN,
    ENTITY_TABLE_MAPPING,
    HISTORY_TABLE,
    HISTORY_TABLE_COLUMN,
    MAPPINGS,
    PROPERTY_COLUMN_MAPPING;

    public static ElementName get(String name) {
        return ElementName.valueOf(name.toUpperCase().replaceAll("-", "_"));
    }
};
