package org.lazydog.history.table;

import java.util.Date;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.lazydog.history.table.internal.HistoryTableConfiguration;


/**
 * History table.
 *
 * @author  Ron Rickard
 */
public class HistoryTable {

    private HistoryTableConfiguration configuration;
    private DataSource historyTableDataSource;
    private DataSource tableDataSource;
    
    public HistoryTable() {

        try {

            // Declare.
            Context context;

            this.configuration = HistoryTableConfiguration.newInstance();

            context = new InitialContext();
            this.historyTableDataSource = (DataSource)context.lookup(this.configuration.getHistoryTableDataSource());
            this.tableDataSource = (DataSource)context.lookup(this.configuration.getTableDataSource());
        }
        catch(NamingException e) {
            e.printStackTrace();
        }
    }

    private static void disconnect(Connection connection, ResultSet resultSet) {

        try {

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
    /*
    public boolean exists(String table, String suffix) {

        // Declare.
        Connection connection;
        boolean exists;
        ResultSet resultSet;

        // Initialize.
        connection = connect(this.dataSource);
        exists = false;
        resultSet = null;

        return exists;
    }
    */
    public <T> void create(Class<T> entityClass) {

    }

    public String getCreateTableSQL(Class entityClass) {

        // Declare.
        Connection connection;
        ResultSet resultSet;
        StringBuffer sqlString;

        // Initialize.
        connection = connect(this.tableDataSource);
        resultSet = null;
        sqlString = new StringBuffer();

        try {

            // Declare.
            DatabaseMetaData metaData;

            // Get the database meta data.
            metaData = connection.getMetaData();

            // Get the column meta data for the table.
            resultSet = metaData.getColumns(
                    null,
                    null,
                    this.configuration.getEntityTableMap().get(entityClass.getName()),
                    null);

            // Loop through the column meta data.
            while (resultSet.next()) {

                // Declare.
                StringBuffer columnDefinition;
                String columnName;
                int columnSize;
                int dataType;
                int decimalDigits;
                String typeName;
                String unsigned;

                // Initialize.
                columnDefinition = new StringBuffer();

                // Get the column meta data.
                columnName = resultSet.getString("COLUMN_NAME");
                columnSize = resultSet.getInt("COLUMN_SIZE");
                dataType = resultSet.getInt("DATA_TYPE");
                decimalDigits = resultSet.getInt("DECIMAL_DIGITS");
                typeName = resultSet.getString("TYPE_NAME");
                unsigned = (typeName.indexOf(" UNSIGNED") != -1) ? " UNSIGNED" : "";
                typeName = typeName.replace(" UNSIGNED", "");

                // Check if this is the first column.
                if (sqlString.length() <= 0) {
                    sqlString
                            .append("create table ")
                            .append(this.configuration.getEntityHistoryTableMap().get(entityClass.getName()))
                            .append(" (");
                }
                else {
                    sqlString.append(", ");
                }

                columnDefinition.append(typeName);

                if (dataType != Types.DATE &&
                    dataType != Types.TIME &&
                    dataType != Types.TIMESTAMP) {

                    columnDefinition
                            .append("(")
                            .append(columnSize);

                    if (decimalDigits != 0) {

                        columnDefinition
                                .append(",")
                                .append(decimalDigits);
                    }

                    columnDefinition
                            .append(")")
                            .append(unsigned);
                }

                sqlString
                        .append(columnName)
                        .append(" ")
                        .append(columnDefinition);
            }

            if (sqlString.length() > 0) {
                sqlString.append(")");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            disconnect(connection, resultSet);
        }

        return sqlString.toString();
    }

    public void insert(Object entity, Action action, String actionBy, Date actionTime) {

    }
}
