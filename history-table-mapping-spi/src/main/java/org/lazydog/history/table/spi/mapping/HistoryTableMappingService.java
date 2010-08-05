package org.lazydog.history.table.spi.mapping;

import java.util.List;


/**
 * History table mapping service.
 *
 * @author  Ron Rickard
 */
public interface HistoryTableMappingService {

    /**
     * Get the entity table mappings.
     *
     * @return  the entity table mappings.
     */
    public List<EntityTableMapping> getEntityTableMappings();
}
