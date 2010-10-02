package org.lazydog.history.table;

import org.junit.Test;
import org.lazydog.comic.model.Comic;


/**
 * History table test.
 *
 * @author  Ron Rickard
 */
public class HistoryTableTest {


    @Test
    public void testGetCreateTableSQL() throws Exception {

        // Declare.
        HistoryTable historyTable;

        historyTable = new HistoryTable();

System.out.println(historyTable.getCreateTableSQL(Comic.class));
    }
}
