package org.lazydog.persistence.history.internal;

import java.util.logging.Level;
import org.junit.Test;


/**
 * History table configuration test.
 *
 * @author  Ron Rickard
 */
public class HistoryTableConfigurationTest {

    @Test
    public void testValidate() {
        PersistenceHistoryConfiguration.newInstance(Level.FINEST);
    }
}
