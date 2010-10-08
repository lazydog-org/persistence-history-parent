package org.lazydog.history.table;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.lazydog.history.table.internal.PersistenceHistoryConfiguration;


/**
 * History table.
 *
 * @author  Ron Rickard
 */
public class HistoryTable<T> {

    private enum COLUMN_META_DATA {
        COLUMN_NAME,
        COLUMN_SIZE,
        DATA_TYPE,
        DECIMAL_DIGITS,
        TYPE_NAME,
        UNSIGNED
    };

    private enum TABLE_META_DATA {
        TABLE_NAME,
    };
    
    private Class<T> entityClass;
    private DataSource historyTableDataSource;
    private String historyTableName;
    private DataSource tableDataSource;
    private List<Map<COLUMN_META_DATA,Object>> columnDefinitions;
    private String tableName;
    
    public HistoryTable(Class<T> entityClass) {

        try {

            // Declare.
            Context context;
            PersistenceHistoryConfiguration configuration;

            configuration = PersistenceHistoryConfiguration.newInstance();

            context = new InitialContext();
            this.historyTableDataSource = (DataSource)context.lookup(configuration.getHistoryTableDataSource());
            this.tableDataSource = (DataSource)context.lookup(configuration.getTableDataSource());

            this.entityClass = entityClass;
            this.tableName = configuration.getEntityTableMap().get(entityClass.getName());
            this.historyTableName = configuration.getEntityHistoryTableMap().get(entityClass.getName());
            this.columnDefinitions = getColumnDefinitions(this.tableDataSource, this.tableName);
        }
        catch(NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connect to the database.
     *
     * @param  dataSource  the data source.
     *
     * @return  the database connection.
     */
    private static Connection connect(DataSource dataSource) {

        // Declare.
        Connection connection;

        // Initialize.
        connection = null;

        try {

            // Get the connection.
            connection = dataSource.getConnection();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return connection;
    }

    /**
     * Create the history table.
     */
    public void create() {

        // Declare.
        Connection connection;
        PreparedStatement preparedStatement;

        // Initialize.
        connection = connect(this.historyTableDataSource);
        preparedStatement = null;

        try {

            preparedStatement = connection.prepareStatement(this.getCreateTableSQL());
            preparedStatement.executeUpdate();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            disconnect(connection, preparedStatement, null);
        }
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if the history table exists.
     *
     * @return  true if the history table exists, otherwise false.
     */
    public boolean exists() {

        // Declare.
        Connection connection;
        boolean exists;
        ResultSet resultSet;

        // Initialize.
        connection = connect(this.historyTableDataSource);
        exists = false;
        resultSet = null;

        try {

            // Get the table meta data for the history table.
            resultSet = connection.getMetaData().getTables(
                    null,
                    null,
                    this.historyTableName,
                    null);

            if (resultSet.next()) {

                if (resultSet.getString(TABLE_META_DATA.TABLE_NAME.toString()).equals(this.historyTableName)) {
                    exists = true;
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
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
     */
    private static List<Map<COLUMN_META_DATA,Object>> getColumnDefinitions(DataSource dataSource, String tableName) {

        // Declare.
        List<Map<COLUMN_META_DATA,Object>> columnDefinitions;
        Connection connection;
        ResultSet resultSet;

        // Initialize.
        columnDefinitions = new ArrayList<Map<COLUMN_META_DATA,Object>>();
        connection = connect(dataSource);
        resultSet = null;

        try {

            // Get the column meta data for the table.
            resultSet = connection.getMetaData().getColumns(
                    null,
                    null,
                    tableName,
                    null);

            // Check if there is column meta data.
            while (resultSet.next()) {

                // Declare.
                Map<COLUMN_META_DATA,Object> columnDefinition;
                String columnName;
                Integer columnSize;
                Integer dataType;
                Integer decimalDigits;
                String typeName;
                Boolean unsigned;

                // Get the column meta data.
                columnName = resultSet.getString(COLUMN_META_DATA.COLUMN_NAME.toString());
                columnSize = resultSet.getInt(COLUMN_META_DATA.COLUMN_SIZE.toString());
                dataType = resultSet.getInt(COLUMN_META_DATA.DATA_TYPE.toString());
                decimalDigits = resultSet.getInt(COLUMN_META_DATA.DECIMAL_DIGITS.toString());
                typeName = resultSet.getString(COLUMN_META_DATA.TYPE_NAME.toString());
                unsigned = (typeName.indexOf(" " + COLUMN_META_DATA.UNSIGNED.toString()) != -1);
                typeName = typeName.replace(" " + COLUMN_META_DATA.UNSIGNED.toString(), "");

                // Put the column meta data in the definition.
                columnDefinition = new EnumMap<COLUMN_META_DATA,Object>(COLUMN_META_DATA.class);
                columnDefinition.put(COLUMN_META_DATA.COLUMN_NAME, columnName);
                columnDefinition.put(COLUMN_META_DATA.COLUMN_SIZE, columnSize);
                columnDefinition.put(COLUMN_META_DATA.DATA_TYPE, dataType);
                columnDefinition.put(COLUMN_META_DATA.DECIMAL_DIGITS, decimalDigits);
                columnDefinition.put(COLUMN_META_DATA.TYPE_NAME, typeName);
                columnDefinition.put(COLUMN_META_DATA.UNSIGNED, unsigned);

                // Add the column definition to the list.
                columnDefinitions.add(columnDefinition);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            disconnect(connection, null, resultSet);
        }

        return columnDefinitions;
    }

    public String getCreateTableSQL() {

        // Declare.
        StringBuffer sqlStringBuffer;

        // Initialize.
        sqlStringBuffer = new StringBuffer();

        // Loop through the column definitions.
        for (Map<COLUMN_META_DATA,Object> columnDefinition : this.columnDefinitions) {

            // Declare.
            StringBuffer columnSql;

            // Initialize.
            columnSql = new StringBuffer();

            columnSql
                    .append((String)columnDefinition.get(COLUMN_META_DATA.COLUMN_NAME))
                    .append(" ")
                    .append((String)columnDefinition.get(COLUMN_META_DATA.TYPE_NAME));

            if ((Integer)columnDefinition.get(COLUMN_META_DATA.DATA_TYPE) != Types.DATE &&
                (Integer)columnDefinition.get(COLUMN_META_DATA.DATA_TYPE) != Types.TIME &&
                (Integer)columnDefinition.get(COLUMN_META_DATA.DATA_TYPE) != Types.TIMESTAMP) {

                columnSql
                        .append("(")
                        .append((Integer)columnDefinition.get(COLUMN_META_DATA.COLUMN_SIZE));

                if ((Integer)columnDefinition.get(COLUMN_META_DATA.DECIMAL_DIGITS) != 0) {

                    columnSql
                            .append(",")
                            .append((Integer)columnDefinition.get(COLUMN_META_DATA.DECIMAL_DIGITS));
                }

                columnSql
                        .append(")")
                        .append(((Boolean)columnDefinition.get(COLUMN_META_DATA.UNSIGNED)) ? " UNSIGNED" : "");
            }

            // Check if this is the first column.
            if (sqlStringBuffer.length() <= 0) {
                sqlStringBuffer
                        .append("create table ")
                        .append(this.historyTableName)
                        .append(" (");
            }
            else {
                sqlStringBuffer
                        .append(", ");
            }

            sqlStringBuffer.append(columnSql);
        }

        if (sqlStringBuffer.length() > 0) {

            sqlStringBuffer
                    .append(", action TINYINT(1) UNSIGNED, action_by CHAR(36), action_time DATETIME)");
        }

        return sqlStringBuffer.toString();
    }

    public String getInsertRowSQL() {

        // Declare.
        StringBuffer columnValuesStringBuffer;
        StringBuffer sqlStringBuffer;

        // Initialize.
        columnValuesStringBuffer = new StringBuffer();
        sqlStringBuffer = new StringBuffer();

        // Loop through the column definitions.
        for (Map<COLUMN_META_DATA,Object> columnDefinition : this.columnDefinitions) {

            // Check if this is the first column.
            if (sqlStringBuffer.length() <= 0) {
                sqlStringBuffer
                        .append("insert into ")
                        .append(this.historyTableName)
                        .append(" (");
                columnValuesStringBuffer
                        .append(" values (?");
            }
            else {
                sqlStringBuffer
                        .append(", ");
                columnValuesStringBuffer
                        .append(", ?");
            }

            sqlStringBuffer.append((String)columnDefinition.get(COLUMN_META_DATA.COLUMN_NAME));
        }

        if (sqlStringBuffer.length() > 0) {
            sqlStringBuffer
                    .append(", action, action_by, action_time)")
                    .append(columnValuesStringBuffer)
                    .append(", ?, ?, ?)");
        }

        return sqlStringBuffer.toString();
    }

    public Map<String,Object> getRow(Integer id) {

        // Declare.
        Map<String,Object> row;
        Connection connection;
        PreparedStatement preparedStatement;
        ResultSet resultSet;

        // Initialize.
        row = new HashMap<String,Object>();
        connection = connect(this.tableDataSource);
        preparedStatement = null;
        resultSet = null;

        try {

            preparedStatement = connection.prepareStatement(this.getSelectRowSQL());
            preparedStatement.setInt(1, id);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                // Loop through the column definitions.
                for (Map<COLUMN_META_DATA, Object> columnDefinition : this.columnDefinitions) {

                    // Declare.
                    Object data;
                    String columnName;

                    // Get the column name and data.
                    columnName = (String)columnDefinition.get(COLUMN_META_DATA.COLUMN_NAME);
                    data = resultSet.getObject(columnName);

                    // Add the column name and data to the row.
                    row.put(columnName, data);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            disconnect(connection, preparedStatement, resultSet);
        }

        return row;
    }

    public List<Map<String,Object>> getRows() {

        // Declare.
        List<Map<String,Object>> rows;
        Connection connection;
        PreparedStatement preparedStatement;
        ResultSet resultSet;

        // Initialize.
        rows = new ArrayList<Map<String,Object>>();
        connection = connect(this.tableDataSource);
        preparedStatement = null;
        resultSet = null;

        try {

            preparedStatement = connection.prepareStatement(this.getSelectAllRowsSQL());

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                // Declare.
                Map<String,Object> row;

                // Initialize.
                row = new HashMap<String,Object>();

                // Loop through the column definitions.
                for (Map<COLUMN_META_DATA, Object> columnDefinition : this.columnDefinitions) {

                    // Declare.
                    Object data;
                    String columnName;

                    // Get the column name and data.
                    columnName = (String)columnDefinition.get(COLUMN_META_DATA.COLUMN_NAME);
                    data = resultSet.getObject(columnName);

                    // Add the column name and data to the row.
                    row.put(columnName, data);
                }

                // Add the row to the list.
                rows.add(row);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            disconnect(connection, preparedStatement, resultSet);
        }

        return rows;
    }

    public String getSelectAllRowsSQL() {
        return new StringBuffer()
                .append("select * from ")
                .append(this.tableName).toString();
    }

    public String getSelectRowSQL() {
        return new StringBuffer()
                .append("select * from ")
                .append(this.tableName)
                .append(" where id = ?").toString();
    }

    /**
     * Insert row in the history table.
     *
     * @param  id          the row identifier.
     * @param  action      the action.
     * @param  actionBy    the action by.
     * @param  actionTime  the action time.
     */
    public void insert(Integer id, Action action, String actionBy, Date actionTime) {

        // Declare.
        Connection connection;
        PreparedStatement preparedStatement;

        // Initialize.
        connection = connect(this.historyTableDataSource);
        preparedStatement = null;

        try {

            // Declare.
            int parameterIndex;
            Map<String,Object> row;

            row = this.getRow(id);

            preparedStatement = connection.prepareStatement(this.getInsertRowSQL());

            // Set the parameter index to one.
            parameterIndex = 1;

            for (Map<COLUMN_META_DATA,Object> columnDefinition: columnDefinitions) {
                preparedStatement.setObject(parameterIndex++, row.get((String)columnDefinition.get(COLUMN_META_DATA.COLUMN_NAME)));
            }

            preparedStatement.setObject(parameterIndex++, action.ordinal());
            preparedStatement.setObject(parameterIndex++, actionBy);
            preparedStatement.setObject(parameterIndex++, actionTime);

            preparedStatement.executeUpdate();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            disconnect(connection, preparedStatement, null);
        }
    }

    /**
     * Populate the history table.
     *
     * @param  action      the action.
     * @param  actionBy    the action by.
     * @param  actionTime  the action time.
     */
    public void populate(Action action, String actionBy, Date actionTime) {

        // Declare.
        Connection connection;
        PreparedStatement preparedStatement;

        // Initialize.
        connection = connect(this.historyTableDataSource);
        preparedStatement = null;

        try {

            // Declare.
            int parameterIndex;
            List<Map<String,Object>> rows;

            rows = this.getRows();

            preparedStatement = connection.prepareStatement(this.getInsertRowSQL());

            for (Map<String,Object> row : rows) {

                // Set the parameter index to one.
                parameterIndex = 1;

                for (Map<COLUMN_META_DATA,Object> columnDefinition: columnDefinitions) {
                    preparedStatement.setObject(parameterIndex++, row.get((String)columnDefinition.get(COLUMN_META_DATA.COLUMN_NAME)));
                }

                preparedStatement.setObject(parameterIndex++, action.ordinal());
                preparedStatement.setObject(parameterIndex++, actionBy);
                preparedStatement.setObject(parameterIndex++, actionTime);
                
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            disconnect(connection, preparedStatement, null);
        }
    }
}
