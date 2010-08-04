package org.lazydog.history.table;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;


/**
 * History table generator.
 *
 * @author  Ron Rickard
 */
public class HistoryTableGenerator {

    public static final String DB_CONNECTION_URL = "database.connection.url";
    public static final String DB_DATA_SOURCE = "database.data.source";
    public static final String DB_DRIVER_CLASS = "database.driver.class";
    public static final String DB_PASSWORD = "database.password";
    public static final String DB_SCHEMA = "database.schema";
    public static final String DB_USER_NAME = "database.user.name";
    
    private String connectionUrl;
    private DataSource dataSource;
    private String password;
    private String schema;
    private String userName;
    
    public HistoryTableGenerator(Properties environment) {

        try {

            // Check if a data source is specified.
            if (environment.getProperty(DB_DATA_SOURCE) != null) {

                Context context = new InitialContext();
                this.dataSource = (DataSource)context.lookup(environment.getProperty(DB_DATA_SOURCE));
            }

            // Check if a driver class is specified.
            else if (environment.getProperty(DB_DRIVER_CLASS) != null) {

                // Check if a connection URL is not specified.
                 if ((this.connectionUrl = environment.getProperty(DB_CONNECTION_URL)) == null) {
                     throw new Exception("No connection URL was specified.");
                 }

                 // Check if a user name is not specified.
                 if ((this.userName = environment.getProperty(DB_USER_NAME)) == null) {
                     throw new Exception("No user name was specified.");
                 }

                 // Check if a password is not specified.
                 if ((this.password = environment.getProperty(DB_PASSWORD)) == null) {
                     throw new Exception("No password was specified.");
                 }

                 // Load the database driver class.
                 Class.forName(environment.getProperty(DB_DRIVER_CLASS)).newInstance();
            }
            else {
                throw new Exception("No data source or driver class was specified.");
            }

            if ((this.schema = environment.getProperty(DB_SCHEMA)) == null) {
                throw new Exception("No schema was specified.");
            }
        }
        catch (Exception e) {
System.out.println(e);
        }
    }

    public void disconnect(Connection connection, ResultSet resultSet) {

        try {

            // Check if the connection exists.
            if (connection != null) {

                // Close the connection.
                connection.close();
            }
        }
        catch (Exception e) {
System.out.println(e);
        }
    }

    public Connection connect() {

        // Declare.
        Connection connection;

        // Initialize.
        connection = null;

        try {

            // Check if the data source exists.
            if (dataSource != null) {

                // Get the connection using the data source.
                connection = dataSource.getConnection();
            }
            else {

                // Get the connection using the driver manager.
                connection = DriverManager.getConnection(this.connectionUrl, this.userName, this.password);
            }
        }
        catch (Exception e) {
System.out.println(e);
        }

        return connection;
    }

    public boolean exists(String table, String suffix) {

        // Declare.
        Connection connection;
        boolean exists;
        ResultSet resultSet;

        // Initialize.
        connection = connect();
        exists = false;
        resultSet = null;

        return exists;
    }

    public void createHistoryTable(String table, String suffix) {

        // Declare.
        Connection connection;
        ResultSet resultSet;
        StringBuffer sqlString;

        // Initialize.
        connection = connect();
        resultSet = null;
        sqlString = new StringBuffer();

        try {

            // Declare.
            int columnCount;
            DatabaseMetaData metaData;

            // Get the database meta data.
            metaData = connection.getMetaData();

            // Get the column meta data for the table.
            resultSet = metaData.getColumns(this.schema, this.schema, table, null);

            // Initialize the column count.
            columnCount = 0;

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
                if (++columnCount == 1) {
                    sqlString
                            .append("create table ")
                            .append(this.schema)
                            .append(".")
                            .append(table)
                            .append(suffix)
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

            // Check if the table does not exist.
            if (columnCount == 0) {
                throw new Exception("Unable to create audit table for missing table " + table + ".");
            }
            else {
                sqlString.append(")");
            }
        }
        catch (Exception e) {
System.out.println(e);
            disconnect(connection, resultSet);
        }

        System.out.println(sqlString);
    }

    public static void main(String[] args) {

        // Declare.
        Properties environment;
        HistoryTableGenerator generator;

        // Set the environment.
        environment = new Properties();
        environment.setProperty(DB_CONNECTION_URL, "jdbc:mysql://localhost:3306/comic_collection");
        environment.setProperty(DB_DRIVER_CLASS, "com.mysql.jdbc.Driver");
        environment.setProperty(DB_PASSWORD, "c0m1c@dm1n");
        environment.setProperty(DB_SCHEMA, "comic_collection");
        environment.setProperty(DB_USER_NAME, "comicadmin");

        generator = new HistoryTableGenerator(environment);

        generator.createHistoryTable("comic", "_history");
    }
}
