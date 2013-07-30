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

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.lazydog.addressbook.model.Address;
import org.lazydog.addressbook.model.Company;
import org.lazydog.addressbook.model.Department;
import org.lazydog.addressbook.model.Employee;
import org.lazydog.addressbook.model.Phone;

/**
 * Persistence history configuration test.
 *
 * @author  Ron Rickard
 */
public class PersistenceHistoryConfigurationTest {

    @Test
    public void testNewInstance() {
        PersistenceHistoryConfiguration.newInstance();
    }
   
    @Test
    public void testGetHistoryTableIdColumnName() {
        PersistenceHistoryConfiguration configuration = PersistenceHistoryConfiguration.newInstance();
        assertEquals(configuration.getHistoryTableIdColumnName(Address.class), "address_audit_id");
        assertEquals(configuration.getHistoryTableIdColumnName(Company.class), "company_audit_id");
        assertEquals(configuration.getHistoryTableIdColumnName(Department.class), "department_test_audit_id");
        assertEquals(configuration.getHistoryTableIdColumnName(Employee.class), "history_id");
        assertEquals(configuration.getHistoryTableIdColumnName(Phone.class), "phone_test_history_id");
    }
      
    @Test
    public void testGetHistoryTableName() {
        PersistenceHistoryConfiguration configuration = PersistenceHistoryConfiguration.newInstance();
        assertEquals(configuration.getHistoryTableName(Address.class), "address_audit");
        assertEquals(configuration.getHistoryTableName(Company.class), "company_audit");
        assertEquals(configuration.getHistoryTableName(Department.class), "department_test_audit");
        assertEquals(configuration.getHistoryTableName(Employee.class), "employee_test_history");
        assertEquals(configuration.getHistoryTableName(Phone.class), "phone_test_history");
    }
           
    @Test
    public void testGetSourceDataSource() {
        PersistenceHistoryConfiguration configuration = PersistenceHistoryConfiguration.newInstance();
        assertEquals(configuration.getSourceDataSource(), "jdbc/AddressbookPool");
    }
          
    @Test
    public void testGetTableIdColumnName() {
        PersistenceHistoryConfiguration configuration = PersistenceHistoryConfiguration.newInstance();
        assertEquals(configuration.getTableIdColumnName(Address.class), "address_id");
        assertEquals(configuration.getTableIdColumnName(Company.class), "id");
        assertEquals(configuration.getTableIdColumnName(Department.class), "department_test_id");
        assertEquals(configuration.getTableIdColumnName(Employee.class), "id");
        assertEquals(configuration.getTableIdColumnName(Phone.class), "phone_test_id");
    }
      
    @Test
    public void testGetTableName() {
        PersistenceHistoryConfiguration configuration = PersistenceHistoryConfiguration.newInstance();
        assertEquals(configuration.getTableName(Address.class), "address");
        assertEquals(configuration.getTableName(Company.class), "company");
        assertEquals(configuration.getTableName(Department.class), "department_test");
        assertEquals(configuration.getTableName(Employee.class), "employee_test");
        assertEquals(configuration.getTableName(Phone.class), "phone_test");
    }
   
    @Test
    public void testGetTargetDataSource() {
        PersistenceHistoryConfiguration configuration = PersistenceHistoryConfiguration.newInstance();
        assertEquals(configuration.getTargetDataSource(), "jdbc/AddressbookHistoryPool");
    }
}
