package org.lazydog.history.table.internal.mapping;

import org.junit.Test;
import org.lazydog.history.table.spi.mapping.HistoryTableMappingService;
import org.lazydog.utilities.service.factory.ServiceFactory;


/**
 *
 * @author R4R
 */
public class HistoryTableMappingServiceTest {

    @Test
    public void testValidate() throws Exception {

        // Declare.
        HistoryTableMappingService service;

        service = ServiceFactory.create(HistoryTableMappingService.class);

        // Declare.
        HistoryTableMappingServiceImpl serviceImpl;

        serviceImpl = new HistoryTableMappingServiceImpl();

        serviceImpl.validate();
        serviceImpl.parse();
    }
}
