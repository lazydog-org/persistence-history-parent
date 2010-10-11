package org.lazydog.history.table.internal;

import java.util.logging.Level;
import org.junit.Test;


/**
 * History table configuration test.
 *
 * @author  Ron Rickard
 */
public class HistoryTableConfigurationTest {

    @Test
    public void testValidate() throws Exception {
        PersistenceHistoryConfiguration.newInstance(Level.FINEST);
    }
}
