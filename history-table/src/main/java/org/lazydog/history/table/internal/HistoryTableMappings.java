package org.lazydog.history.table.internal;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.XMLConstants;
import org.xml.sax.SAXException;


/**
 * History table mappings.
 *
 * @author  Ron Rickard
 */
public class HistoryTableMappings {

    private static final Logger LOGGER = Logger.getLogger(HistoryTableMappings.class.getName());
    private static final Level DEFAULT_LOG_LEVEL = Level.WARNING;
    private static final String CONFIGURATION_FILE = "META-INF/history-table-mappings.xml";
    private static final String HISTORY_TABLE_SUFFIX = "_history";
    private static final String SCHEMA_FILE = "history-table-mappings.xsd";

    private enum ElementName {
        ENTITY_CLASS_NAME,
        HISTORY_TABLE_DATA_SOURCE,
        HISTORY_TABLE_NAME,
        MAPPING,
        MAPPINGS,
        TABLE_DATA_SOURCE,
        TABLE_NAME;
    };

    private Map<String,String> entityTableMap;
    private Map<String,String> entityHistoryTableMap;
    private String historyTableDataSource;
    private String tableDataSource;

    /**
     * Private constructor.
     * 
     * @param  logLevel  the log level.
     */
    private HistoryTableMappings(Level logLevel) {
        
        try {

            LOGGER.setLevel(logLevel);
            validate();
            parse();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the configuration source.
     *
     * @return  the configuration source.
     */
    private Source getConfigurationSource() {
        return new StreamSource(getClass().getClassLoader().getResourceAsStream(CONFIGURATION_FILE));
    }

    /**
     * Get the element data.
     *
     * @param  event  the XML event.
     *
     * @return  the element data.
     */
    private static String getElementData(XMLEvent event) {
        return event.asCharacters().getData();
    }

    /**
     * Get the element name.
     *
     * @param  element  the end element.
     *
     * @return  the element name.
     */
    private static ElementName getElementName(EndElement element) {
        return getElementName(element.getName().getLocalPart());
    }

    /**
     * Get the element name.
     *
     * @param  element  the start element.
     *
     * @return  the element name.
     */
    private static ElementName getElementName(StartElement element) {
        return getElementName(element.getName().getLocalPart());
    }

    /**
     * Get the element name.
     *
     * @param  name  the name.
     *
     * @return  the element name.
     */
    private static ElementName getElementName(String name) {
        return ElementName.valueOf(name.toUpperCase().replaceAll("-", "_"));
    }

    /**
     * Get the entity history table map.
     *
     * @return  the entity history table map.
     */
    public Map<String,String> getEntityHistoryTableMap() {
        return this.entityHistoryTableMap;
    }

    /**
     * Get the entity table map.
     * 
     * @return  the entity table map.
     */
    public Map<String,String> getEntityTableMap() {
        return this.entityTableMap;
    }

    /**
     * Get the history table data source.
     * 
     * @return  the history table data source.
     */
    public String getHistoryTableDataSource() {
        return this.historyTableDataSource;
    }

    /**
     * Get the history table name.
     * 
     * @param  entityClassName  the entity class name.
     * 
     * @return  the history table name.
     */
    private static String getHistoryTableName(String entityClassName) {
        return new StringBuffer()
                .append(getTableName(entityClassName))
                .append(HISTORY_TABLE_SUFFIX)
                .toString();
    }
    
    /**
     * Get the schema source.
     *
     * @return  the schema source.
     */
    private Source getSchemaSource() {
        return new StreamSource(ClassLoader.getSystemResourceAsStream(SCHEMA_FILE));
    }

    /**
     * Get the simple name of the fully qualified class name.
     *
     * @param  className  the fully qualified class name.
     *
     * @return  the simple name.
     */
    private static String getSimpleName(String className) {
        return className.substring(className.lastIndexOf(".") + 1);
    }

    /**
     * Get the table data source.
     *
     * @return  the table data source.
     */
    public String getTableDataSource() {
        return this.tableDataSource;
    }

    /**
     * Get the table name.
     * 
     * @param  entityClassName  the entity class name.
     * 
     * @return  the table name.
     */
    private static String getTableName(String entityClassName) {
        return toUnderscore(getSimpleName(entityClassName));
    }

    /**
     * Create a new instance of this class.
     *
     * @param  logLevel  the log level.
     *
     * @return  a new instance of this class.
     */
    public static HistoryTableMappings newInstance(Level logLevel) {
        return new HistoryTableMappings(logLevel);
    }

    /**
     * Create a new instance of this class.
     *
     * @return  a new instance of this class.
     */
    public static HistoryTableMappings newInstance() {
       return new HistoryTableMappings(DEFAULT_LOG_LEVEL);
    }

    /**
     * Parse the configuration file.
     *
     * @throws  XMLStreamException  if unable to parse the configuration file.
     */
    private void parse() throws XMLStreamException {

        // Declare.
        XMLInputFactory factory;
        XMLEventReader reader;

        // Get the configuration file reader.
        factory = XMLInputFactory.newInstance();
        reader = factory.createXMLEventReader(this.getConfigurationSource());

        // Initialize the entity table map and entity history table map.
        entityTableMap = new HashMap<String,String>();
        entityHistoryTableMap = new HashMap<String,String>();

        try {

            // Declare.
            String entityClassName;
            String historyTableName;
            String tableName;

            // Initialize.
            entityClassName = null;
            historyTableName = null;
            tableName = null;

            // Loop through the XML events.
            while (reader.hasNext()) {

                // Declare.
                XMLEvent event;

                // Get the next event.
                event = reader.nextEvent();

                // Check if the event is a start element.
                if (event.isStartElement()) {

                    trace(Level.INFO, "process element %s", getElementName(event.asStartElement()));

                    switch(getElementName(event.asStartElement())) {

                        case ENTITY_CLASS_NAME:
                            entityClassName = getElementData(reader.nextEvent());
                            trace(Level.INFO, "entityClassName is %s", entityClassName);
                            break;

                        case HISTORY_TABLE_DATA_SOURCE:
                            historyTableDataSource = getElementData(reader.nextEvent());
                            trace(Level.INFO, "historyTableDataSource is %s", historyTableDataSource);
                            break;

                        case HISTORY_TABLE_NAME:
                            historyTableName = getElementData(reader.nextEvent());
                            trace(Level.INFO, "historyTableName is %s", historyTableName);
                            break;

                        case TABLE_DATA_SOURCE:
                            tableDataSource = getElementData(reader.nextEvent());
                            trace(Level.INFO, "tableDataSource is %s", tableDataSource);
                            break;

                        case TABLE_NAME:
                            tableName = getElementData(reader.nextEvent());
                            trace(Level.INFO, "tableName is %s", tableName);
                            break;
                    }
                }
                
                // Check if the event is an end element.
                else if (event.isEndElement()) {

                    switch(getElementName(event.asEndElement())) {

                        case MAPPING:

                            // Add the entity table map.
                            entityTableMap.put(
                                    entityClassName,
                                    (tableName == null) ?
                                        getTableName(entityClassName) :
                                        tableName);

                            // Add the entity history table map.
                            entityHistoryTableMap.put(
                                    entityClassName,
                                    (historyTableName == null) ?
                                        getHistoryTableName(entityClassName) :
                                        historyTableName);

                            // Clear the map variables.
                            entityClassName = null;
                            tableName = null;
                            historyTableName = null;
                            
                            break;
                    }
                }
            }
        }
        finally {

            // Close the configuration file reader.
            reader.close();
        }
    }

    /**
     * Convert a camel case string into an underscore string.
     *
     * @param  value  the camel case string.
     *
     * @return  the underscore string.
     */
    private static String toUnderscore(String value) {

        // Declare.
        int index;
        Matcher matcher;
        Pattern pattern;
        StringBuffer returnValue;

        // Initialize.
        index = 0;
        returnValue = new StringBuffer();

        // Create a pattern for a word (zero or more uppercase characters followed by non-uppercase characters.)
        pattern = Pattern.compile("([A-Z]*)([^A-Z]*)");
        matcher = pattern.matcher(value);

        // Check if there is more characters in the string to process.
        while (!matcher.hitEnd()) {

            // Check if the pattern is found in the string.
            if (matcher.find(index)) {

                // Check if the pattern starts with a capital letter.
                if (!matcher.group(1).isEmpty()) {

                    // Check if this is not the first occurrence of the pattern.
                    if (index != 0) {
                        returnValue.append("_");
                    }
                    returnValue.append(matcher.group(1).toLowerCase());
                }
                returnValue.append(matcher.group(2));
            }

            index = matcher.end();
        }

        return returnValue.toString();
    }

    /**
     * Create the trace log at the specified level.
     *
     * @param  level   the trace level.
     * @param  format  the trace format.
     * @param  args    the trace arguments.
     */
    private void trace(Level level, String format, Object... args) {

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

    /**
     * Validate the configuration file.
     *
     * @throws  IOException   if unable to validate the configuration file.
     * @throws  SAXException  if unable to validate the configuration file.
     */
    private void validate() throws IOException, SAXException {

        // Declare.
        SchemaFactory factory;
        Schema schema;
        Validator validator;

        factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schema = factory.newSchema(this.getSchemaSource());

        validator = schema.newValidator();

        validator.validate(this.getConfigurationSource());
    }
}
