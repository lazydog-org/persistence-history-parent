/**
 * Copyright 2010-2013 lazydog.org.
 *
 * This file is part of persistence history.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.lazydog.persistence.history.internal;

import java.sql.Connection;
import java.sql.SQLNonTransientConnectionException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lazydog.addressbook.model.Address;
import org.lazydog.persistence.history.HistoryTable;

/**
 *
 * @author rjrjr
 */
public class HistoryTableImplTest {

    private static final String TEST_FILE = "dataset.xml";
    private static DataSource sourceDataSource;
    private static DataSource targetDataSource;
    
    @BeforeClass
    public static void beforeClass() throws Exception {

        // Ensure the derby.log file is in the target directory.
        System.setProperty("derby.system.home", "./target");

        // Create and start the source database.
        sourceDataSource = new EmbeddedDataSource();
        ((EmbeddedDataSource)sourceDataSource).setUser("addressbookuser");
        ((EmbeddedDataSource)sourceDataSource).setPassword("addressbookuser");
        ((EmbeddedDataSource)sourceDataSource).setDatabaseName("memory:./target/addressbook");
        ((EmbeddedDataSource)sourceDataSource).setCreateDatabase("create");
        sourceDataSource.getConnection();
        
        // Create the source tables.
        createSourceTables();
        
        // Create and start the target database.
        targetDataSource = new EmbeddedDataSource();
        ((EmbeddedDataSource)targetDataSource).setUser("addressbookuser");
        ((EmbeddedDataSource)targetDataSource).setPassword("addressbookuser");
        ((EmbeddedDataSource)targetDataSource).setDatabaseName("memory:./target/addressbook_history");
        ((EmbeddedDataSource)targetDataSource).setCreateDatabase("create");
        targetDataSource.getConnection();
        
        System.out.println("sourceDataSource: " + sourceDataSource);
        System.out.println("targetDataSource: " + targetDataSource);
    }
    
    @AfterClass
    public static void afterClass() throws Exception {
        
        // Shutdown the source database.
        try {
            ((EmbeddedDataSource)sourceDataSource).setShutdownDatabase("shutdown");
            sourceDataSource.getConnection();
        } catch (SQLNonTransientConnectionException e) {
            // Ignore.
        }
        
        // Shutdown the target database.
        try {
            ((EmbeddedDataSource)targetDataSource).setShutdownDatabase("shutdown");
            targetDataSource.getConnection();
        } catch (SQLNonTransientConnectionException e) {
            // Ignore.
        }
    }
    @Before
    public void beforeTest() throws Exception {

        // Get the database connection.
        IDatabaseConnection databaseConnection = this.getDatabaseConnection();
        
        // Refresh the dataset in the database.
        DatabaseOperation.CLEAN_INSERT.execute(databaseConnection, getDataSet());
        
        // Close the database connection.
        databaseConnection.close();
    }
    
    @Test
    public void testCreate() {
        Address address = new Address();
        address.setCity("Flagstaff");
        address.setState("AZ");
        HistoryTable historyTable = HistoryTableImpl.newInstance(address, sourceDataSource, targetDataSource);
        //historyTable.create();
    }
    
    private static void createSourceTables() throws Exception {
        
        Connection connection = null;
        Statement statement = null;
        
        try {
            
            // Get the connection.
            connection = sourceDataSource.getConnection();  
            
            // Create the database.
            statement = connection.createStatement();
            statement.execute("create table address(id int primary key, city varchar(255), state varchar(255), street_address varchar(255), zipcode varchar(255))");
        } finally {
            
            // Close the connection.
            statement.close();
            connection.close();
        }
    }
    
    private IDatabaseConnection getDatabaseConnection() throws Exception {
        return new DatabaseConnection(sourceDataSource.getConnection());
    }
    
    private static IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(Thread.currentThread().getContextClassLoader().getResourceAsStream(TEST_FILE));
    }
}
