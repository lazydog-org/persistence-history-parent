package org.lazydog.history.table.internal.mapping;

import java.util.List;
import org.junit.Test;
import org.lazydog.history.table.spi.mapping.EntityTableMapping;
import org.lazydog.history.table.spi.mapping.HistoryTableMappingService;
//import org.lazydog.utilities.service.factory.ServiceFactory;


/**
 * History table mapping service test.
 * 
 * @author  Ron Rickard
 */
public class HistoryTableMappingServiceTest {

    @Test
    public void testGetEntityTableMapping() throws Exception {

        // Declare.
        //HistoryTableMappingService service;
        HistoryTableMappingServiceImpl service;
        List<EntityTableMapping> entityTableMappings;

        //service = ServiceFactory.create(HistoryTableMappingService.class);
        service = new HistoryTableMappingServiceImpl();

        entityTableMappings = service.getEntityTableMappings();

        System.out.println(entityTableMappings);
    }
}
