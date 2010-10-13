package org.lazydog.persistence.history.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.lazydog.persistence.history.Action;
import org.lazydog.persistence.history.HistoryTable;
import org.lazydog.persistence.history.HistoryTableException;


/**
 * History table.
 *
 * @author  Ron Rickard
 */
public class HistoryTableImpl implements HistoryTable {

    private static final Logger LOGGER = Logger.getLogger(HistoryTableImpl.class.getName());
    private static final Level DEFAULT_LOG_LEVEL = Level.WARNING;
    
    private static enum COLUMN_META_DATA {
        COLUMN_NAME,
        COLUMN_SIZE,
        DATA_TYPE,
        DECIMAL_DIGITS,
        TYPE_NAME,
        UNSIGNED
    };

    private static enum TABLE_META_DATA {
        TABLE_NAME,
    };

    private List<Map<COLUMN_META_DATA,Object>> columnDefinitions;
    private Class entityClass;
    private DataSource historyTableDataSource;
    private String historyTableIdColumnName;
    private String historyTableName;
    private DataSource tableDataSource;
    private String tableIdColumnName;
    private String tableName;

    /**
     * Connect to the database.
     *
     * @param  dataSource  the data source.
     *
     * @return  the database connection.
     *
     * @throws  SQLException  if unable to connect to the database.
     */
    private static Connection connect(DataSource dataSource) 
            throws SQLException {

        // Declare.
        Connection connection;

        // Initialize.
        connection = null;

        // Get the connection.
        connection = dataSource.getConnection();

        return connection;
    }

    /**
     * Create the history table.
     *
     * @throws  HistoryTableException  if unable to create the history table.
     */
    @Override
    public void create() {

        // Declare.
        Connection connection;
        PreparedStatement preparedStatement;

        // Initialize.
        connection = null;
        preparedStatement = null;

        try {

            // Connect to the database.
            connection = connect(this.historyTableDataSource);

            trace(Level.FINE, "create table SQL is %s", this.getCreateTableSQL());

            // Create the history table.
            preparedStatement = connection.prepareStatement(this.getCreateTableSQL());
            preparedStatement.executeUpdate();
        }
        catch(SQLException e) {
            throw new HistoryTableException(this.entityClass, 
                    "Unable to create the history table.", e);
        }
        finally {

            // Disconnect from the database.
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
    private static void disconnect(Connection connection,
            PreparedStatement preparedStatement, ResultSet resultSet) {

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
            // Ignore.
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
    public boolean exists() {

        // Declare.
        Connection connection;
        boolean exists;
        ResultSet resultSet;

        // Initialize.
        connection = null;
        exists = false;
        resultSet = null;

        try {

            // Connect to the database.
            connection = connect(this.historyTableDataSource);

            // Get the table meta data for the history table.
            resultSet = connection.getMetaData().getTables(
                    null,
                    null,
                    this.historyTableName,
                    null);

            // Check if there is a result set.
            if (resultSet.next()) {

                // Check if the result set is for the history table.
                if (resultSet.getString(TABLE_META_DATA.TABLE_NAME.toString()).equals(this.historyTableName)) {
                    exists = true;
                }
            }
        }
        catch(SQLException e) {
            throw new HistoryTableException(this.entityClass,
                    "Unable to check if the history table exists.", e);
        }
        finally {

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
    private static List<Map<COLUMN_META_DATA,Object>> getColumnDefinitions(
            DataSource dataSource, String tableName)
            throws SQLException {

        // Declare.
        List<Map<COLUMN_META_DATA,Object>> columnDefinitions;
        Connection connection;
        ResultSet resultSet;

        // Initialize.
        columnDefinitions = new ArrayList<Map<COLUMN_META_DATA,Object>>();
        connection = null;
        resultSet = null;

        try {

            // Connect to the database.
            connection = connect(dataSource);

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
        finally {

            // Disconnect from the database.
            disconnect(connection, null, resultSet);
        }

        return columnDefinitions;
    }

    /**
     * Get the create table SQL string.
     *
     * @return  the create table SQL string.
     */
    private String getCreateTableSQL() {

        // Declare.
        StringBuffer sqlStringBuffer;

        // Initialize.
        sqlStringBuffer = new StringBuffer();

        // Loop through the column definitions.
        for (Map<COLUMN_META_DATA,Object> columnDefinition : this.columnDefinitions) {

            // Declare.
            StringBuffer columnSqlStringBuffer;

            // Initialize.
            columnSqlStringBuffer = new StringBuffer();

            columnSqlStringBuffer
                    .append((String)columnDefinition.get(COLUMN_META_DATA.COLUMN_NAME))
                    .append(" ")
                    .append((String)columnDefinition.get(COLUMN_META_DATA.TYPE_NAME));

            // Check if the column data type is not a time or date.
            if ((Integer)columnDefinition.get(COLUMN_META_DATA.DATA_TYPE) != Types.DATE &&
                (Integer)columnDefinition.get(COLUMN_META_DATA.DATA_TYPE) != Types.TIME &&
                (Integer)columnDefinition.get(COLUMN_META_DATA.DATA_TYPE) != Types.TIMESTAMP) {
                columnSqlStringBuffer
                        .append("(")
                        .append((Integer)columnDefinition.get(COLUMN_META_DATA.COLUMN_SIZE));

                // Check if the column decimal digits is not zero.
                if ((Integer)columnDefinition.get(COLUMN_META_DATA.DECIMAL_DIGITS) != 0) {
                    columnSqlStringBuffer
                            .append(",")
                            .append((Integer)columnDefinition.get(COLUMN_META_DATA.DECIMAL_DIGITS));
                }

                columnSqlStringBuffer
                        .append(")")
                        .append(((Boolean)columnDefinition.get(COLUMN_META_DATA.UNSIGNED)) ? " unsigned" : "");
            }

            // Check if this is the first column.
            if (sqlStringBuffer.length() <= 0) {
                sqlStringBuffer
                        .append("create table ")
                        .append(this.historyTableName)
                        .append(" (")
                        .append(this.historyTableIdColumnName)
                        .append(" int(10) unsigned not null auto_increment, ");
            }
            else {
                sqlStringBuffer
                        .append(", ");
            }

            sqlStringBuffer.append(columnSqlStringBuffer);
        }

        // Check if the SQL string buffer has data.
        if (sqlStringBuffer.length() > 0) {
            sqlStringBuffer
                    .append(", action tinyint(1) unsigned, action_by char(36), action_time datetime, primary key (")
                    .append(this.historyTableIdColumnName)
                    .append("))");
        }

        return sqlStringBuffer.toString();
    }

    /**
     * Get the identifier for the specified entity.
     *
     * @param  entity  the entity.
     *
     * @return  the identifier for the specified entity.
     *
     * @throws  IllegalArgumentException  if the entity is invalid.
     */
    @Override
    public Integer getId(Object entity) {

        // Declare.
        Integer id;

        // Initialize.
        id = null;

        try {

            // Declare.
            Method method;
            StringBuffer methodName;

            // Get the method name.
            methodName = new StringBuffer()
                    .append("get")
                    .append(this.tableIdColumnName.substring(0,1).toUpperCase())
                    .append(this.tableIdColumnName.substring(1));
            trace(Level.FINE, "getId method name is %s for %s", methodName, this.tableIdColumnName);

            // Get the method to get the identifier.
            method = entity.getClass().getMethod(methodName.toString(), new Class[0]);

            // Invoke the method to get the identifier.
            id = (Integer)method.invoke(entity, new Object[0]);
            trace(Level.FINE, "id is %d", id);
        }
        catch(Exception e) {
            throw new IllegalArgumentException("The entity is invalid.", e);
        }

        return id;
    }

    /**
     * Get the insert row SQL string.
     *
     * @return  the insert row SQL string.
     */
    private String getInsertRowSQL() {

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
                        .append(" (")
                        .append(this.historyTableIdColumnName)
                        .append(", ");
                columnValuesStringBuffer
                        .append(" values (null, ?");
            }
            else {
                sqlStringBuffer
                        .append(", ");
                columnValuesStringBuffer
                        .append(", ?");
            }

            sqlStringBuffer.append((String)columnDefinition.get(COLUMN_META_DATA.COLUMN_NAME));
        }

        // Check if the SQL string buffer has data.
        if (sqlStringBuffer.length() > 0) {
            sqlStringBuffer
                    .append(", action, action_by, action_time)")
                    .append(columnValuesStringBuffer)
                    .append(", ?, ?, ?)");
        }

        return sqlStringBuffer.toString();
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

        // Declare.
        Map<String,Object> row;
        Connection connection;
        PreparedStatement preparedStatement;
        ResultSet resultSet;

        // Initialize.
        row = new HashMap<String,Object>();
        connection = null;
        preparedStatement = null;
        resultSet = null;

        try {

            // Connect to the database.
            connection = connect(this.tableDataSource);

            trace(Level.FINE, "select row SQL is %s", this.getSelectRowSQL());

            // Get the row.
            preparedStatement = connection.prepareStatement(this.getSelectRowSQL());
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();

            // Check if there is a result set.
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
        finally {

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

        // Declare.
        List<Map<String,Object>> rows;
        Connection connection;
        PreparedStatement preparedStatement;
        ResultSet resultSet;

        // Initialize.
        rows = new ArrayList<Map<String,Object>>();
        connection = null;
        preparedStatement = null;
        resultSet = null;

        try {

            // Connect to the database.
            connection = connect(this.tableDataSource);

            trace(Level.FINE, "select all rows SQL is %s", this.getSelectAllRowsSQL());

            // Get the rows.
            preparedStatement = connection.prepareStatement(this.getSelectAllRowsSQL());
            resultSet = preparedStatement.executeQuery();

            // Loop through the result sets.
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
        finally {

            // Disconnect from the database.
            disconnect(connection, preparedStatement, resultSet);
        }

        return rows;
    }

    /**
     * Get the select all rows SQL string.
     *
     * @return  the select all rows SQL string.
     */
    private String getSelectAllRowsSQL() {
        return new StringBuffer()
                .append("select * from ")
                .append(this.tableName).toString();
    }

    /**
     * Get the select row SQL string.
     *
     * @return  the select row SQL string.
     */
    private String getSelectRowSQL() {
        return new StringBuffer()
                .append("select * from ")
                .append(this.tableName)
                .append(" where ")
                .append(this.tableIdColumnName)
                .append(" = ?").toString();
    }

    /**
     * Insert a row in the history table.
     *
     * @param  id          the row identifier.
     * @param  action      the action.
     * @param  actionBy    the action by.
     * @param  actionTime  the action time.
     *
     * @throws  HistoryTableException  if unable to insert a row in the history table.
     */
    @Override
    public void insert(Integer id, Action action, String actionBy, Date actionTime) {

        // Declare.
        Connection connection;
        PreparedStatement preparedStatement;

        // Initialize.
        connection = null;
        preparedStatement = null;

        try {

            // Declare.
            int parameterIndex;
            Map<String,Object> row;

            // Get the row from the source table.
            row = this.getRow(id);

            // Connect to the database.
            connection = connect(this.historyTableDataSource);

            trace(Level.FINE, "insert row SQL is %s", this.getInsertRowSQL());

            // Initialize the insert statement.
            parameterIndex = 1;
            preparedStatement = connection.prepareStatement(this.getInsertRowSQL());

            // Loop through the column definitions.
            for (Map<COLUMN_META_DATA,Object> columnDefinition: this.columnDefinitions) {

                // Set the parameters to the data from the source table.
                preparedStatement.setObject(parameterIndex++, row.get((String)columnDefinition.get(COLUMN_META_DATA.COLUMN_NAME)));
            }

            // Set the action, action by, and action time parameters.
            preparedStatement.setObject(parameterIndex++, action.ordinal());
            preparedStatement.setObject(parameterIndex++, actionBy);
            preparedStatement.setObject(parameterIndex++, actionTime);

            // Insert the row in the history table.
            preparedStatement.executeUpdate();
        }
        catch(SQLException e) {
            throw new HistoryTableException(this.entityClass,
                    "Unable to insert a row in the history table.", e);
        }
        finally {

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
    public void populate(String actionBy, Date actionTime) {

        // Declare.
        Connection connection;
        PreparedStatement preparedStatement;

        // Initialize.
        connection = null;
        preparedStatement = null;

        try {

            // Declare.
            int parameterIndex;
            List<Map<String,Object>> rows;

            // Get the rows from the source table.
            rows = this.getRows();

            // Connect to the database.
            connection = connect(this.historyTableDataSource);

            trace(Level.FINE, "insert row SQL is %s", this.getInsertRowSQL());

            // Initialize the insert statement batch.
            preparedStatement = connection.prepareStatement(this.getInsertRowSQL());

            // Loop through the rows.
            for (Map<String,Object> row : rows) {

                // Initialize the parameter index.
                parameterIndex = 1;

                // Loop through the column definitions.
                for (Map<COLUMN_META_DATA,Object> columnDefinition: this.columnDefinitions) {

                    // Set the parameters to the data from the source table.
                    preparedStatement.setObject(parameterIndex++, row.get((String)columnDefinition.get(COLUMN_META_DATA.COLUMN_NAME)));
                }

                // Set the action, action by, and action time parameters.
                preparedStatement.setObject(parameterIndex++, Action.INITIAL.ordinal());
                preparedStatement.setObject(parameterIndex++, actionBy);
                preparedStatement.setObject(parameterIndex++, actionTime);
                
                preparedStatement.addBatch();
            }

            // Insert the rows in the history table.
            preparedStatement.executeBatch();
        }
        catch(SQLException e) {
            throw new HistoryTableException(this.entityClass,
                    "Unable to populate the history table.", e);
        }
        finally {

            // Disconnect from the database.
            disconnect(connection, preparedStatement, null);
        }
    }

    /**
     * Set the entity class.
     *
     * @param  entityClass  the entity class.
     *
     * @throws  HistoryTableException     if unable to set the entity class.
     * @throws  IllegalArgumentException  if the entity class is invalid.
     */
    public void setEntityClass(Class entityClass) {

        try {

            // Declare.
            Context context;
            PersistenceHistoryConfiguration configuration;

            // Get the persistence history configuration.
            configuration = PersistenceHistoryConfiguration.newInstance();

            // Set the table and history table data sources.
            context = new InitialContext();
            this.tableDataSource = (DataSource)context.lookup(configuration.getTableDataSource());
            this.historyTableDataSource = (DataSource)context.lookup(configuration.getHistoryTableDataSource());
            
            // Set the table name, table ID column name, history table name,
            // and history table ID column name.
            this.tableName = configuration.getEntityTableMap().get(entityClass.getName());
            this.tableIdColumnName = configuration.getEntityTableIdColumnMap().get(entityClass.getName());
            this.historyTableName = configuration.getEntityHistoryTableMap().get(entityClass.getName());
            this.historyTableIdColumnName = configuration.getEntityHistoryTableIdColumnMap().get(entityClass.getName());

            // Check if the table ID column name, table name, history table ID
            // column name, or history table name do not exist.
            if (this.tableName == null ||
                this.tableName.isEmpty() ||
                this.tableIdColumnName == null ||
                this.tableIdColumnName.isEmpty() ||
                this.historyTableName == null ||
                this.historyTableName.isEmpty() ||
                this.historyTableIdColumnName == null ||
                this.historyTableIdColumnName.isEmpty()) {
                throw new IllegalArgumentException(
                        "The entity class " + entityClass.getName() + " is invalid.");
            }

            // Set the column definitions.
            this.columnDefinitions = getColumnDefinitions(this.tableDataSource, this.tableName);

            // Set the entity class.
            this.entityClass = entityClass;

            LOGGER.setLevel(DEFAULT_LOG_LEVEL);

            trace(Level.INFO, "historyTableDataSource is %s", this.historyTableDataSource);
            trace(Level.INFO, "tableDataSource is %s", this.tableDataSource);
            trace(Level.INFO, "tableIdColumnName is %s", this.tableIdColumnName);
            trace(Level.INFO, "tableName is %s", this.tableName);
            trace(Level.INFO, "historyTableIdColumnName is %s", this.historyTableIdColumnName);
            trace(Level.INFO, "historyTableName is %s", this.historyTableName);
        }
        catch(NamingException e) {
            throw new HistoryTableException(entityClass,
                    "Unable to set the entity class due to a data source issue.", e);
        }
        catch(SQLException e) {
            throw new HistoryTableException(entityClass,
                    "Unable to set the entity class due to a SQL issue.", e);
        }
    }

    /**
     * Create the trace log at the specified level.
     *
     * @param  level   the trace level.
     * @param  format  the trace format.
     * @param  args    the trace arguments.
     */
    private static void trace(Level level, String format, Object... args) {

        // Check if the level is appropriate to log.
        if (level.intValue() >= LOGGER.getLevel().intValue()) {

            // Declare.
            StringBuffer message;

            // Set the trace message.
            message = new StringBuffer();
            message.append(String.format("[%1$tD %1$tT:%1$tL %1$tZ] ", new Date()))
                   .append(String.format("%s ", LOGGER.getName()))
                   .append(String.format(format, args));

            // Create the trace log.
            LOGGER.log(level, message.toString());
        }
    }
}
