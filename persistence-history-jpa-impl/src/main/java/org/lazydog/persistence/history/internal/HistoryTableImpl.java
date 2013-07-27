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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.lazydog.persistence.history.HistoryTable;
import org.lazydog.persistence.history.HistoryTableException;
import org.slf4j.LoggerFactory;

/**
 * History table.
 *
 * @author  Ron Rickard
 */
public class HistoryTableImpl implements HistoryTable {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(HistoryTableImpl.class);
    
    private static enum COLUMN_META_DATA {
        COLUMN_NAME,
        COLUMN_SIZE,
        DATA_TYPE,
        DECIMAL_DIGITS,
        TYPE_NAME,
        UNSIGNED
    };

    private static enum TABLE_META_DATA {
        TABLE_NAME
    };

    private List<Map<COLUMN_META_DATA,Object>> columnDefinitions;
    private Object entity;
    private DataSource historyTableDataSource;
    private String historyTableIdColumnName;
    private String historyTableName;
    private DataSource tableDataSource;
    private String tableIdColumnName;
    private String tableName;

    /**
     * Hide the constructor.
     * 
     * @param  entity  the entity.
     * 
     * @throws  IllegalArgumentException  if the entity is invalid.
     */
    private HistoryTableImpl(Object entity) throws IllegalArgumentException {
        
        try {

            // Get the persistence history configuration.
            PersistenceHistoryConfiguration configuration = PersistenceHistoryConfiguration.newInstance();

            // Set the table and history table data sources.
            Context context = new InitialContext();
            this.tableDataSource = (DataSource)context.lookup(configuration.getTableDataSource());
            this.historyTableDataSource = (DataSource)context.lookup(configuration.getHistoryTableDataSource());
            
            // Set the table name, table ID column name, history table name, and history table ID column name.
            this.tableName = configuration.getEntityTableMap().get(entity.getClass().getName());
            this.tableIdColumnName = configuration.getEntityTableIdColumnMap().get(entity.getClass().getName());
            this.historyTableName = configuration.getEntityHistoryTableMap().get(entity.getClass().getName());
            this.historyTableIdColumnName = configuration.getEntityHistoryTableIdColumnMap().get(entity.getClass().getName());

            // Check if the table name, table ID column name, history table name, or history table ID column name do not exist.
            if (this.tableName == null ||
                this.tableName.isEmpty() ||
                this.tableIdColumnName == null ||
                this.tableIdColumnName.isEmpty() ||
                this.historyTableName == null ||
                this.historyTableName.isEmpty() ||
                this.historyTableIdColumnName == null ||
                this.historyTableIdColumnName.isEmpty()) {
                throw new IllegalArgumentException("The entity " + entity.getClass().getSimpleName() + " is invalid.");
            }

            // Set the column definitions.
            this.columnDefinitions = getColumnDefinitions(this.tableDataSource, this.tableName);

            // Set the entity class.
            this.entity = entity;

            logger.info("The history table data source is {}.", this.historyTableDataSource);
            logger.info("The table data source is {}.", this.tableDataSource);
            logger.info("The table ID  column name is {}.", this.tableIdColumnName);
            logger.info("The table name is {}.", this.tableName);
            logger.info("The history table ID column name is {}.", this.historyTableIdColumnName);
            logger.info("The history table name is {}.", this.historyTableName);
        } catch (NamingException e) {
            throw new IllegalArgumentException("Unable to initialize the history table for the entity " + entity.getClass().getSimpleName() + " due to a data source issue.", e);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Unable to initialize the history table for the entity " + entity.getClass().getSimpleName() + " due to a SQL issue.", e);
        }
    }
    
    /**
     * Connect to the database.
     *
     * @param  dataSource  the data source.
     *
     * @return  the database connection.
     *
     * @throws  SQLException  if unable to connect to the database.
     */
    private static Connection connect(DataSource dataSource) throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Create the history table.
     *
     * @throws  HistoryTableException  if unable to create the history table.
     */
    @Override
    public void create() throws HistoryTableException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {

            // Connect to the database.
            connection = connect(this.historyTableDataSource);

            // Create the history table.
            String createTableSQL = this.createCreateTableSQL();
            logger.trace("Create the history table {} with SQL: {}", this.historyTableName, createTableSQL);
            preparedStatement = connection.prepareStatement(createTableSQL);
            preparedStatement.executeUpdate();
        }  catch(SQLException e) {
            throw new HistoryTableException(this.entity, "Unable to create the history table " + this.historyTableName + ".", e);
        }
        finally {

            // Disconnect from the database.
            disconnect(connection, preparedStatement, null);
        }
    }

    /**
     * Create the create table SQL string.
     *
     * @return  the create table SQL string.
     */
    private String createCreateTableSQL() {

        StringBuilder sqlStringBuilder = new StringBuilder();

        // Loop through the column definitions.
        for (Map<COLUMN_META_DATA,Object> columnDefinition : this.columnDefinitions) {

            StringBuilder columnSqlStringBuilder = new StringBuilder();

            columnSqlStringBuilder
                    .append((String)columnDefinition.get(COLUMN_META_DATA.COLUMN_NAME))
                    .append(" ")
                    .append((String)columnDefinition.get(COLUMN_META_DATA.TYPE_NAME));

            // Check if the column data type is not a time or date.
            if ((Integer)columnDefinition.get(COLUMN_META_DATA.DATA_TYPE) != Types.DATE &&
                (Integer)columnDefinition.get(COLUMN_META_DATA.DATA_TYPE) != Types.TIME &&
                (Integer)columnDefinition.get(COLUMN_META_DATA.DATA_TYPE) != Types.TIMESTAMP) {
                columnSqlStringBuilder
                        .append("(")
                        .append((Integer)columnDefinition.get(COLUMN_META_DATA.COLUMN_SIZE));

                // Check if the column decimal digits is not zero.
                if ((Integer)columnDefinition.get(COLUMN_META_DATA.DECIMAL_DIGITS) != 0) {
                    columnSqlStringBuilder
                            .append(",")
                            .append((Integer)columnDefinition.get(COLUMN_META_DATA.DECIMAL_DIGITS));
                }

                columnSqlStringBuilder
                        .append(")")
                        .append(((Boolean)columnDefinition.get(COLUMN_META_DATA.UNSIGNED)) ? " unsigned" : "");
            }

            // Check if this is the first column.
            if (sqlStringBuilder.length() <= 0) {
                sqlStringBuilder
                        .append("create table ")
                        .append(this.historyTableName)
                        .append(" (")
                        .append(this.historyTableIdColumnName)
                        .append(" int(10) unsigned not null auto_increment, ");
            } else {
                sqlStringBuilder.append(", ");
            }

            sqlStringBuilder.append(columnSqlStringBuilder);
        }

        // Check if the SQL string builder has data.
        if (sqlStringBuilder.length() > 0) {
            sqlStringBuilder
                    .append(", action varchar(255) not null, action_by varchar(255) not null, action_time datetime not null, primary key (")
                    .append(this.historyTableIdColumnName)
                    .append("))");
        }

        return sqlStringBuilder.toString();
    }

    /**
     * Create the insert row SQL string.
     *
     * @return  the insert row SQL string.
     */
    private String createInsertRowSQL() {

        StringBuilder columnValuesStringBuilder = new StringBuilder();
        StringBuilder sqlStringBuilder = new StringBuilder();

        // Loop through the column definitions.
        for (Map<COLUMN_META_DATA,Object> columnDefinition : this.columnDefinitions) {

            // Check if this is the first column.
            if (sqlStringBuilder.length() <= 0) {
                sqlStringBuilder
                        .append("insert into ")
                        .append(this.historyTableName)
                        .append(" (");
                columnValuesStringBuilder
                        .append(" values (?");
            } else {
                sqlStringBuilder
                        .append(", ");
                columnValuesStringBuilder
                        .append(", ?");
            }

            sqlStringBuilder.append((String)columnDefinition.get(COLUMN_META_DATA.COLUMN_NAME));
        }

        // Check if the SQL string builder has data.
        if (sqlStringBuilder.length() > 0) {
            sqlStringBuilder
                    .append(", action, action_by, action_time)")
                    .append(columnValuesStringBuilder)
                    .append(", ?, ?, ?)");
        }

        return sqlStringBuilder.toString();
    }

    /**
     * Create the select all rows SQL string.
     *
     * @return  the select all rows SQL string.
     */
    private String createSelectRowsSQL() {
        return new StringBuilder()
                .append("select * from ")
                .append(this.tableName)
                .toString();
    }

    /**
     * Create the select row SQL string.
     *
     * @return  the select row SQL string.
     */
    private String createSelectRowSQL() {
        return new StringBuilder()
                .append("select * from ")
                .append(this.tableName)
                .append(" where ")
                .append(this.tableIdColumnName)
                .append(" = ?")
                .toString();
    }

    /**
     * Disconnect from the database.
     *
     * @param  connection         the database connection.
     * @param  preparedStatement  the prepared statement.
     * @param  resultSet          the result set.
     */
    private static void disconnect(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet) {

        try {

            // Check if the result set exists.
            if (resultSet != null) {

                // Close the result set.
                resultSet.close();
            }

            // Check if the prepared statement exists.
            if (preparedStatement != null) {

                // Close the prepared statement.
                preparedStatement.close();
            }

            // Check if the connection exists.
            if (connection != null) {

                // Close the connection.
                connection.close();
            }
        } catch (Exception e) {
            logger.error("Unable to disconnect from the database.");
        }
    }

    /**
     * Check if the history table exists.
     *
     * @return  true if the history table exists, otherwise false.
     *
     * @throws  HistoryTableException  if unable to check if the history table exists.
     */
    @Override
    public boolean exists() throws HistoryTableException {

        Connection connection = null;
        boolean exists = false;
        ResultSet resultSet = null;

        try {

            // Connect to the database.
            connection = connect(this.historyTableDataSource);

            // Get the table meta data for the history table.
            resultSet = connection.getMetaData().getTables(null, null, this.historyTableName, null);

            // Check if there is a result set.
            if (resultSet.next()) {

                // Check if the result set is for the history table.
                if (resultSet.getString(TABLE_META_DATA.TABLE_NAME.toString()).equals(this.historyTableName)) {
                    exists = true;
                }
            }
        } catch (SQLException e) {
            throw new HistoryTableException(this.entity, "Unable to check if the history table " + this.historyTableName + " exists.", e);
        } finally {

            // Disconnect from the database.
            disconnect(connection, null, resultSet);
        }

        return exists;
    }

    /**
     * Get the column definitions.
     *
     * @param  dataSource  the data source.
     * @param  tableName   the table name.
     * 
     * @return  the column definitions.
     *
     * @throws  SQLException  if unable to get the column definitions.
     */
    private static List<Map<COLUMN_META_DATA,Object>> getColumnDefinitions(DataSource dataSource, String tableName) throws SQLException {

        List<Map<COLUMN_META_DATA,Object>> columnDefinitions = new ArrayList<Map<COLUMN_META_DATA,Object>>();
        Connection connection = null;
        ResultSet resultSet = null;

        try {

            // Connect to the database.
            connection = connect(dataSource);

            // Get the column meta data for the table.
            resultSet = connection.getMetaData().getColumns(null, null, tableName, null);

            // Check if there is column meta data.
            while (resultSet.next()) {

                // Get the column meta data.
                String columnName = resultSet.getString(COLUMN_META_DATA.COLUMN_NAME.toString());
                Integer columnSize = resultSet.getInt(COLUMN_META_DATA.COLUMN_SIZE.toString());
                Integer dataType = resultSet.getInt(COLUMN_META_DATA.DATA_TYPE.toString());
                Integer decimalDigits = resultSet.getInt(COLUMN_META_DATA.DECIMAL_DIGITS.toString());
                String typeName = resultSet.getString(COLUMN_META_DATA.TYPE_NAME.toString());
                Boolean unsigned = (typeName.indexOf(" " + COLUMN_META_DATA.UNSIGNED.toString()) != -1);
                typeName = typeName.replace(" " + COLUMN_META_DATA.UNSIGNED.toString(), "");

                // Put the column meta data in the definition.
                Map<COLUMN_META_DATA,Object> columnDefinition = new EnumMap<COLUMN_META_DATA,Object>(COLUMN_META_DATA.class);
                columnDefinition.put(COLUMN_META_DATA.COLUMN_NAME, columnName);
                columnDefinition.put(COLUMN_META_DATA.COLUMN_SIZE, columnSize);
                columnDefinition.put(COLUMN_META_DATA.DATA_TYPE, dataType);
                columnDefinition.put(COLUMN_META_DATA.DECIMAL_DIGITS, decimalDigits);
                columnDefinition.put(COLUMN_META_DATA.TYPE_NAME, typeName);
                columnDefinition.put(COLUMN_META_DATA.UNSIGNED, unsigned);

                // Add the column definition to the list.
                columnDefinitions.add(columnDefinition);
            }
        } finally {

            // Disconnect from the database.
            disconnect(connection, null, resultSet);
        }

        return columnDefinitions;
    }

    /**
     * Get the identifier for the entity.
     *
     * @return  the identifier for the entity.
     *
     * @throws  IllegalArgumentException  if the entity is invalid.
     */
    private Integer getId() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        // Get the method name.
        String methodName = new StringBuilder()
                .append("get")
                .append(this.tableIdColumnName.substring(0,1).toUpperCase())
                .append(this.tableIdColumnName.substring(1))
                .toString();
        logger.trace("The getId method name is {} for the entity {}.", methodName, this.entity.getClass().getSimpleName());

        // Get the method to get the identifier.
        Method method = this.entity.getClass().getMethod(methodName.toString(), new Class[0]);

        // Invoke the method to get the identifier.
        return (Integer)method.invoke(this.entity, new Object[0]);
    }

    /**
     * Get the row from the source table.
     *
     * @param  id  the row identifier.
     *
     * @return  the row.
     *
     * @throws  SQLException  if unable to get the row.
     */
    private Map<String,Object> getRow(Integer id) throws SQLException {

        Map<String,Object> row = new HashMap<String,Object>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {

            // Connect to the database.
            connection = connect(this.tableDataSource);

            // Get the row.
            String selectRowSQL = this.createSelectRowSQL();
            logger.trace("Get the row with SQL: {}", selectRowSQL);
            preparedStatement = connection.prepareStatement(selectRowSQL);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();

            // Check if there is a result set.
            if (resultSet.next()) {

                // Loop through the column definitions.
                for (Map<COLUMN_META_DATA,Object> columnDefinition : this.columnDefinitions) {

                    // Get the column name and data.
                    String columnName = (String)columnDefinition.get(COLUMN_META_DATA.COLUMN_NAME);
                    Object data = resultSet.getObject(columnName);

                    // Add the column name and data to the row.
                    row.put(columnName, data);
                }
            }
        } finally {

            // Disconnect from the database.
            disconnect(connection, preparedStatement, resultSet);
        }

        return row;
    }

    /**
     * Get the rows from the source table.
     *
     * @return  the rows.
     *
     * @throws  SQLException  if unable to get the rows.
     */
    private List<Map<String,Object>> getRows() throws SQLException {

        List<Map<String,Object>> rows = new ArrayList<Map<String,Object>>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {

            // Connect to the database.
            connection = connect(this.tableDataSource);

            // Get the rows.
            String selectRowsSQL = this.createSelectRowsSQL();
            logger.trace("Get the rows with SQL: {}", selectRowsSQL);
            preparedStatement = connection.prepareStatement(selectRowsSQL);
            resultSet = preparedStatement.executeQuery();

            // Loop through the result sets.
            while (resultSet.next()) {

                // Initialize.
                Map<String,Object> row = new HashMap<String,Object>();

                // Loop through the column definitions.
                for (Map<COLUMN_META_DATA,Object> columnDefinition : this.columnDefinitions) {

                    // Get the column name and data.
                    String columnName = (String)columnDefinition.get(COLUMN_META_DATA.COLUMN_NAME);
                    Object data = resultSet.getObject(columnName);

                    // Add the column name and data to the row.
                    row.put(columnName, data);
                }

                // Add the row to the list.
                rows.add(row);
            }
        } finally {

            // Disconnect from the database.
            disconnect(connection, preparedStatement, resultSet);
        }

        return rows;
    }

    /**
     * Insert a row in the history table.
     * 
     * @param  action      the action.
     * @param  actionBy    the action by.
     * @param  actionTime  the action time.
     *
     * @throws  HistoryTableException  if unable to insert a row in the history table.
     */
    @Override
    public void insert(Action action, String actionBy, Date actionTime) throws HistoryTableException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {

            // Get the row from the source table.
            Map<String,Object> row = this.getRow(this.getId());

            // Connect to the database.
            connection = connect(this.historyTableDataSource);

            // Initialize the insert statement.
            int parameterIndex = 1;
            String insertRowSQL = this.createInsertRowSQL();
            logger.trace("Insert with SQL: {}", insertRowSQL);
            preparedStatement = connection.prepareStatement(insertRowSQL);

            // Loop through the column definitions.
            for (Map<COLUMN_META_DATA,Object> columnDefinition: this.columnDefinitions) {

                // Set the parameters to the data from the source table.
                preparedStatement.setObject(parameterIndex++, row.get((String)columnDefinition.get(COLUMN_META_DATA.COLUMN_NAME)));
            }

            // Set the action, action by, and action time parameters.
            preparedStatement.setObject(parameterIndex++, action.toString());
            preparedStatement.setObject(parameterIndex++, actionBy);
            preparedStatement.setObject(parameterIndex++, actionTime);

            // Insert the row in the history table.
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new HistoryTableException(this.entity, "Unable to insert a row in the history table " + this.historyTableName + ".", e);
        } finally {

            // Disconnect from the database.
            disconnect(connection, preparedStatement, null);
        }
    }

    /**
     * Populate the history table.
     *
     * @param  actionBy    the action by.
     * @param  actionTime  the action time.
     *
     * @throws  HistoryTableException  if unable to populate the history table.
     */
    @Override
    public void populate(String actionBy, Date actionTime) throws HistoryTableException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {

            // Connect to the database.
            connection = connect(this.historyTableDataSource);

            // Initialize the insert statement batch.
            String insertRowSQL = this.createInsertRowSQL();
            logger.trace("Insert with SQL: {}", insertRowSQL);
            preparedStatement = connection.prepareStatement(insertRowSQL);

            // Loop through the rows from the source table.
            for (Map<String,Object> row : this.getRows()) {

                // Initialize the parameter index.
                int parameterIndex = 1;

                // Loop through the column definitions.
                for (Map<COLUMN_META_DATA,Object> columnDefinition: this.columnDefinitions) {

                    // Set the parameters to the data from the source table.
                    preparedStatement.setObject(parameterIndex++, row.get((String)columnDefinition.get(COLUMN_META_DATA.COLUMN_NAME)));
                }

                // Set the action, action by, and action time parameters.
                preparedStatement.setObject(parameterIndex++, Action.INITIAL.toString());
                preparedStatement.setObject(parameterIndex++, actionBy);
                preparedStatement.setObject(parameterIndex++, actionTime);
                
                preparedStatement.addBatch();
            }

            // Insert the rows in the history table.
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            throw new HistoryTableException(this.entity, "Unable to populate the history table " + this.historyTableName + ".", e);
        } finally {

            // Disconnect from the database.
            disconnect(connection, preparedStatement, null);
        }
    }

    /**
     * Set the entity.
     *
     * @param  entity  the entity.
     *
     * @throws  IllegalArgumentException  if the entity is invalid.
     */
    protected static HistoryTable newInstance(Object entity) throws IllegalArgumentException {
        return new HistoryTableImpl(entity);
    }
}
