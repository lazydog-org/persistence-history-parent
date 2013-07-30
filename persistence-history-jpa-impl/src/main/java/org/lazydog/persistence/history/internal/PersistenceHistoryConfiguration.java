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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Persistence history configuration.
 *
 * @author  Ron Rickard
 */
public class PersistenceHistoryConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PersistenceHistoryConfiguration.class);
    private static final String DEFAULT_HISTORY_TABLE_SUFFIX = "_history";
    private static final String DEFAULT_TABLE_ID_COLUMN_SUFFIX = "_id";
    private static final String CONFIGURATION_FILE = "META-INF/persistence-history.xml";
    private static final String SCHEMA_FILE = "META-INF/xsd/persistence-history.xsd";
    private static enum ELEMENT_NAME {
        ENTITY,
        HISTORY_TABLE,
        HISTORY_TABLE_SUFFIX,
        PERSISTENCE_HISTORY,
        SOURCE_DATA_SOURCE,
        TABLE,
        TARGET_DATA_SOURCE;
    };
    private static enum ATTRIBUTE_NAME {
        CLASS,
        ID,
        NAME;
    }

    private Map<String,EntityData> entityDataMap;
    private String historyTableSuffix;
    private String sourceDataSource;
    private String targetDataSource;

    /**
     * Private constructor.
     */
    private PersistenceHistoryConfiguration() {

        try {

            // Validate and parse the persistence history configuration file.
            validate();
            parse();
        } catch (Exception e) {
            logger.error("Unable to parse the {} file.", CONFIGURATION_FILE, e);
        }
    }

    /**
     * Create the default history table name.
     * 
     * @param  tableName           the table name.
     * @param  historyTableSuffix  the history table suffix.
     * 
     * @return  the default history table name.
     */
    private static String createDefaultHistoryTableName(String tableName, String historyTableSuffix) {
        return new StringBuilder()
                .append(tableName)
                .append((historyTableSuffix != null) ? historyTableSuffix : DEFAULT_HISTORY_TABLE_SUFFIX)
                .toString();
    }
    
    /**
     * Create the default table identifier column name.
     *
     * @param  tableName  the table name.
     *
     * @return  the default table identifier column name.
     */
    private static String createDefaultTableIdColumnName(String tableName) {
        return new StringBuilder()
                .append(tableName)
                .append(DEFAULT_TABLE_ID_COLUMN_SUFFIX).toString();
    }

    /**
     * Create the default table name.
     * 
     * @param  entityClassName  the entity class name.
     * 
     * @return  the default table name.
     */
    private static String createDefaultTableName(String entityClassName) {
        return toUnderscore(getSimpleName(entityClassName));
    }

    /**
     * Get the configuration input stream.
     * 
     * @return  the configuration input stream.
     */
    private static InputStream getConfigurationInputStream() {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIGURATION_FILE);
    }

    /**
     * Get the configuration source.
     *
     * @return  the configuration source.
     */
    private static Source getConfigurationSource() {
        return new StreamSource(getConfigurationInputStream());
    }

    /**
     * Get the attribute data.
     *
     * @param  event          the XML event.
     * @param  attributeName  the attribute name.
     *
     * @return  the attribute data.
     */
    private static String getAttributeData(XMLEvent event, ATTRIBUTE_NAME attributeName) {
        return (event.asStartElement().getAttributeByName(new QName(attributeName.toString().toLowerCase())) != null) ?
            event.asStartElement().getAttributeByName(new QName(attributeName.toString().toLowerCase())).getValue() :
            new String();
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
    private static ELEMENT_NAME getElementName(EndElement element) {
        return getElementName(element.getName().getLocalPart());
    }

    /**
     * Get the element name.
     *
     * @param  element  the start element.
     *
     * @return  the element name.
     */
    private static ELEMENT_NAME getElementName(StartElement element) {
        return getElementName(element.getName().getLocalPart());
    }

    /**
     * Get the element name.
     *
     * @param  name  the name.
     *
     * @return  the element name.
     */
    private static ELEMENT_NAME getElementName(String name) {
        return ELEMENT_NAME.valueOf(name.toUpperCase().replaceAll("-", "_"));
    }

    /**
     * Get the target data source.
     * 
     * @return  the target data source.
     */
    public String getTargetDataSource() {
        return this.targetDataSource;
    }

    /**
     * Get the history table identifier column name for the entity class.
     * 
     * @param  entityClass  the entity class.
     * 
     * @return  the history table identifier column name.
     */
    public String getHistoryTableIdColumnName(Class entityClass) {
        return this.entityDataMap.get(entityClass.getName()).getHistoryTableIdColumnName();
    }

    /**
     * Get the history table name for the entity class.
     * 
     * @param  entityClass  the entity class.
     *
     * @return  the history table name.
     */
    public String getHistoryTableName(Class entityClass) {
        return this.entityDataMap.get(entityClass.getName()).getHistoryTableName();
    }

    /**
     * Get the schema source.
     *
     * @return  the schema source.
     */
    private static Source getSchemaSource() {
        return new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream(SCHEMA_FILE));
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
     * Get the source data source.
     *
     * @return  the source data source.
     */
    public String getSourceDataSource() {
        return this.sourceDataSource;
    }

    /**
     * Get the table identifier column name for the entity class.
     * 
     * @param  entityClass  the entity class.
     * 
     * @return  the table identifier column name.
     */
    public String getTableIdColumnName(Class entityClass) {
        return this.entityDataMap.get(entityClass.getName()).getTableIdColumnName();
    }

    /**
     * Get the table name for the entity class.
     * 
     * @param  entityClass  the entity class.
     * 
     * @return  the table name.
     */
    public String getTableName(Class entityClass) {
        return this.entityDataMap.get(entityClass.getName()).getTableName();
    }

    /**
     * Create a new instance of the persistence history configuration class.
     *
     * @return  a new instance of the persistence history configuration class.
     */
    public static PersistenceHistoryConfiguration newInstance() {
       return new PersistenceHistoryConfiguration();
    }

    /**
     * Parse the configuration file.
     *
     * @throws  XMLStreamException  if unable to parse the configuration file.
     */
    private void parse() throws XMLStreamException {

        InputStream inputStream = null;
        XMLEventReader reader = null;

        this.entityDataMap = new HashMap<String,EntityData>();

        try {

            String entityClassName = null;
            String historyTableIdColumnName = null;
            String historyTableName = null;
            String tableIdColumnName = null;
            String tableName = null;

            // Get the configuration file reader.
            XMLInputFactory factory = XMLInputFactory.newInstance();
            inputStream = getConfigurationInputStream();
            reader = factory.createXMLEventReader(inputStream);

            // Loop through the XML events.
            while (reader.hasNext()) {

                // Get the next event.
                XMLEvent event = reader.nextEvent();

                // Check if the event is a start element.
                if (event.isStartElement()) {

                    logger.trace("process start element {}", getElementName(event.asStartElement()));

                    switch(getElementName(event.asStartElement())) {

                        case ENTITY:
                            entityClassName = getAttributeData(event, ATTRIBUTE_NAME.CLASS);
                            historyTableIdColumnName = new String();
                            historyTableName = new String();
                            tableIdColumnName = new String();
                            tableName = new String();
                            logger.trace("entityClassName is {}", entityClassName);
                            break;

                        case HISTORY_TABLE:
                            historyTableIdColumnName = getAttributeData(event, ATTRIBUTE_NAME.ID);
                            historyTableName = getAttributeData(event, ATTRIBUTE_NAME.NAME);
                            logger.trace("historyTableIdColumnName is {}", historyTableIdColumnName);
                            logger.trace("historyTableName is {}", historyTableName);
                            break;

                        case HISTORY_TABLE_SUFFIX:
                            this.historyTableSuffix = getElementData(reader.nextEvent());
                            logger.trace("historyTableSuffix is {}", this.historyTableSuffix);
                            break;
                            
                        case SOURCE_DATA_SOURCE:
                            this.sourceDataSource = getElementData(reader.nextEvent());
                            logger.trace("sourceDataSource is {}", this.sourceDataSource);
                            break;
    
                        case TABLE:
                            tableIdColumnName = getAttributeData(event, ATTRIBUTE_NAME.ID);
                            tableName = getAttributeData(event, ATTRIBUTE_NAME.NAME);
                            logger.trace("tableIdColumnName is {}", tableIdColumnName);
                            logger.trace("tableName is {}", tableName);
                            break;
                            
                        case TARGET_DATA_SOURCE:
                            this.targetDataSource = getElementData(reader.nextEvent());
                            logger.trace("targetDataSource is {}", this.targetDataSource);
                            break;
                    }
                }
                
                // Check if the event is an end element.
                else if (event.isEndElement()) {

                    logger.trace("process end element {}", getElementName(event.asEndElement()));

                    switch(getElementName(event.asEndElement())) {

                        case ENTITY:

                            // Finalize the entity data properties.
                            tableName = (tableName.isEmpty()) ? createDefaultTableName(entityClassName) : tableName;
                            tableIdColumnName = (tableIdColumnName.isEmpty()) ? createDefaultTableIdColumnName(tableName) : tableIdColumnName;
                            historyTableName = (historyTableName.isEmpty()) ? createDefaultHistoryTableName(tableName, this.historyTableSuffix) : historyTableName;
                            historyTableIdColumnName = (historyTableIdColumnName.isEmpty()) ? createDefaultTableIdColumnName(historyTableName) : historyTableIdColumnName;

                            logger.debug("tableName is {} for entity {}", tableName, entityClassName);
                            logger.debug("tableIdColumnName is {} for entity {}", tableIdColumnName, entityClassName);
                            logger.debug("historyTableName is {} for entity {}", historyTableName, entityClassName);
                            logger.debug("historyTableIdColumnName is {} for entity {}", historyTableIdColumnName, entityClassName);
                            
                            // Create the entity data.
                            EntityData entityData = new EntityData();
                            entityData.setHistoryTableIdColumnName(historyTableIdColumnName);
                            entityData.setHistoryTableName(historyTableName);
                            entityData.setTableIdColumnName(tableIdColumnName);
                            entityData.setTableName(tableName);
                            
                            // Put the entity data on the map.
                            this.entityDataMap.put(entityClassName, entityData);
                            break;
                    }
                }
            }
        } finally {

            // Check if the reader exists.
            if (reader != null) {

                // Close the configuration file reader.
                reader.close();
            }

            // Check if the input stream exists.
            if (inputStream != null) {

                try {
                    
                    // Close the configuration file input stream.
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("Unable to close the input stream.", e);
                }
            }
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

        int index = 0;
        StringBuilder returnValue = new StringBuilder();

        // Create a pattern for a word (zero or more uppercase characters followed by non-uppercase characters.)
        Pattern pattern = Pattern.compile("([A-Z]*)([^A-Z]*)");
        Matcher matcher = pattern.matcher(value);

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
     * Validate the configuration file.
     *
     * @throws  IOException   if unable to validate the configuration file.
     * @throws  SAXException  if unable to validate the configuration file.
     */
    private static void validate() throws IOException, SAXException {

        // Validate the configuration file.
        Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(getSchemaSource());
        Validator validator = schema.newValidator();
        validator.validate(getConfigurationSource());
    }
    
    /**
     * Entity data.
     */
    private class EntityData {
        
        private String historyTableIdColumnName;
        private String historyTableName;
        private String tableIdColumnName;
        private String tableName; 
        
        /**
         * Get the history table identifier column name.
         * 
         * @return  the history table identifier column name.
         */
        public String getHistoryTableIdColumnName() {
            return this.historyTableIdColumnName;
        }
        
        /**
         * Get the history table name.
         * 
         * @return  the history table name.
         */
        public String getHistoryTableName() {
            return this.historyTableName;
        }
        
        /**
         * Get the table identifier column name.
         * 
         * @return  the table identifier column name.
         */
        public String getTableIdColumnName() {
            return this.tableIdColumnName;
        }
        
        /**
         * Get the table name.
         * 
         * @return  the table name.
         */
        public String getTableName() {
            return this.tableName;
        }
        
        /**
         * Set the history table identifier column name.
         * 
         * @param  historyTableIdColumnName  the history table identifier column name.
         */
        public void setHistoryTableIdColumnName(String historyTableIdColumnName) {
            this.historyTableIdColumnName = historyTableIdColumnName;
        }
        
        /**
         * Set the history table name.
         * 
         * @param  historyTableName  the history table name.
         */
        public void setHistoryTableName(String historyTableName) {
            this.historyTableName = historyTableName;
        }
        
        /**
         * Set the table identifier column name.
         * 
         * @param  tableIdColumnName  the table identifier column name.
         */
        public void setTableIdColumnName(String tableIdColumnName) {
            this.tableIdColumnName = tableIdColumnName;
        }
        
        /**
         * Set the table name.
         * 
         * @param  tableName  the table name.
         */
        public void setTableName(String tableName) {
            this.tableName = tableName;
        }
    }
}
