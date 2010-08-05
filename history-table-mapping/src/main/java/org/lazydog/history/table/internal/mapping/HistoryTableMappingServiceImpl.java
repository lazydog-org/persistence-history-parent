package org.lazydog.history.table.internal.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import org.lazydog.history.table.spi.mapping.EntityTableMapping;
import org.lazydog.history.table.spi.mapping.HistoryTableMappingService;
import org.lazydog.history.table.spi.mapping.PropertyColumnMapping;
import org.xml.sax.SAXException;


/**
 * History table mapping service implementation.
 *
 * @author  Ron Rickard
 */
public final class HistoryTableMappingServiceImpl implements HistoryTableMappingService {

    private static final String HISTORY_TABLE_MAPPING_SCHEMA = "history-table-mappings.xsd";
    private static final String HISTORY_TABLE_MAPPING_CONFIG = "META-INF/history-table-mappings.xml";
    private static List<EntityTableMapping> entityTableMappings;

    /**
     * Return the default entity table derived from the specified entity class.
     *
     * @param  entityClass  the entity class.
     *
     * @return  the default entity table.
     */
    private static String defaultEntityTable(String entityClass) {
        return toUnderscore(entityClass.substring(entityClass.lastIndexOf(".") + 1));
    }

    /**
     * Return the default history table derived from the specified entity class.
     * 
     * @param  entityClass  the entity class.
     * 
     * @return  the default history table.
     */
    private static String defaultHistoryTable(String entityClass) {
        return new StringBuffer(toUnderscore(entityClass.substring(entityClass.lastIndexOf(".") + 1))).append("_history").toString();
    }

    /**
     * Return the default table column derived from the specified entity class
     * property.
     *
     * @param  entityClassProperty  the entity class property.
     *
     * @return  the default table column.
     */
    private static String defaultTableColumn(String entityClassProperty) {
        return toUnderscore(entityClassProperty.replace(".", "_"));
    }

    /**
     * Return the element data from the specified XML event.
     *
     * @param  event  the XML event.
     *
     * @return  the element data.
     */
    private static String elementData(XMLEvent event) {
        return event.asCharacters().getData();
    }

    /**
     * Return the element name from the specified start element.
     *
     * @param  startElement  the start element.
     *
     * @return  the element name.
     */
    private static ElementName elementName(StartElement startElement) {
        return ElementName.get(startElement.getName().getLocalPart());
    }

    /**
     * Return the element name from the specified end element.
     *
     * @param  endElement  the end element.
     *
     * @return  the element name.
     */
    private static ElementName elementName(EndElement endElement) {
        return ElementName.get(endElement.getName().getLocalPart());
    }

    /**
     * Get the entity table mappings.
     *
     * @return  the entity table mappings.
     */
    @Override
    public List<EntityTableMapping> getEntityTableMappings() {

        // Check if the entity table mappings does not exist.
        if (entityTableMappings == null) {

            // Validate the mapping source against the schema source.
            validate(this.getMappingSource(), this.getSchemaSource());

            // Parse the mapping source.
            entityTableMappings = parse(this.getMappingSource());
        }

        return entityTableMappings;
    }

    /**
     * Get the mapping source.
     *
     * @return  the mapping source.
     */
    private Source getMappingSource() {
        return new StreamSource(getClass().getClassLoader().getResourceAsStream(HISTORY_TABLE_MAPPING_CONFIG));
    }

    /**
     * Get the schema source.
     *
     * @return  the schema source.
     */
    private Source getSchemaSource() {
        return new StreamSource(ClassLoader.getSystemResourceAsStream(HISTORY_TABLE_MAPPING_SCHEMA));
    }

    /**
     * Parse the mapping source.
     *
     * @param  mappingSource  the mapping source.
     * 
     * @return  the entity table mappings.
     */
    private static List<EntityTableMapping> parse(Source mappingSource) {

        // Declare.
        List<EntityTableMapping> entityTableMappings;
        XMLInputFactory factory;
        XMLEventReader reader;

        // Initialize.
        entityTableMappings = new ArrayList<EntityTableMapping>();
        factory = XMLInputFactory.newInstance();
        reader = null;

        try {

            // Declare.
            String entityClass;
            String entityClassProperty;
            String entityTable;
            String entityTableColumn;
            String historyTable;
            String historyTableColumn;
            List<PropertyColumnMapping> propertyColumnMappings;

            // Initialize.
            entityClass = null;
            entityClassProperty = null;
            entityTable = null;
            entityTableColumn = null;
            historyTable = null;
            historyTableColumn = null;
            propertyColumnMappings = new ArrayList<PropertyColumnMapping>();

            // Initialize the XML event reader.
            reader = factory.createXMLEventReader(mappingSource);

            // Loop through the XML events.
            while (reader.hasNext()) {

                // Declare.
                XMLEvent event;

                // Get the next event.
                event = reader.nextEvent();

                // Check if the event is a start element.
                if (event.isStartElement()) {

                    switch(elementName(event.asStartElement())) {

                        case ENTITY_CLASS:
System.out.println("start of element entity-class");
                            entityClass = elementData(reader.nextEvent());
                            break;

                        case ENTITY_CLASS_PROPERTY:
System.out.println("start of element entity-class-property");
                            entityClassProperty = elementData(reader.nextEvent());
                            break;

                        case ENTITY_TABLE:
System.out.println("start of element entity-table");
                            entityTable = elementData(reader.nextEvent());
                            break;

                        case ENTITY_TABLE_COLUMN:
System.out.println("start of element entity-table-column");
                            entityTableColumn = elementData(reader.nextEvent());
                            break;

                        case HISTORY_TABLE:
System.out.println("start of element history-table");
                            historyTable = elementData(reader.nextEvent());
                            break;

                        case HISTORY_TABLE_COLUMN:
System.out.println("start of element history-table-column");
                            historyTableColumn = elementData(reader.nextEvent());
                            break;
                    }
                }

                // Check if the event is an end element.
                else if (event.isEndElement()) {

                    switch(elementName(event.asEndElement())) {

                        case PROPERTY_COLUMN_MAPPING:
System.out.println("end of element property-column-mapping");
                            // Declare.
                            PropertyColumnMappingImpl propertyColumnMapping;

                            if (entityTableColumn == null) {
                                entityTableColumn = defaultTableColumn(entityClassProperty);
                            }

                            if (historyTableColumn == null) {
                                historyTableColumn = defaultTableColumn(entityClassProperty);
                            }

                            // Set the property column mapping.
                            propertyColumnMapping = new PropertyColumnMappingImpl();
                            propertyColumnMapping.setEntityClassProperty(entityClassProperty);
                            propertyColumnMapping.setEntityTableColumn(entityTableColumn);
                            propertyColumnMapping.setHistoryTableColumn(historyTableColumn);

                            // Add the property column mappings to the list.
                            propertyColumnMappings.add(propertyColumnMapping);

                            // Clear the values.
                            entityClassProperty = null;
                            entityTableColumn = null;
                            historyTableColumn = null;
                            break;

                        case ENTITY_TABLE_MAPPING:
System.out.println("end of element entity-table-mapping");
                            // Declare.
                            EntityTableMappingImpl entityTableMapping;

                            if (entityTable == null) {
                                entityTable = defaultEntityTable(entityClass);
                            }

                            if (historyTable == null) {
                                historyTable = defaultHistoryTable(entityClass);
                            }

                            // Set the entity table mapping.
                            entityTableMapping = new EntityTableMappingImpl();
                            entityTableMapping.setEntityClass(entityClass);
                            entityTableMapping.setEntityTable(entityTable);
                            entityTableMapping.setHistoryTable(historyTable);
                            entityTableMapping.setPropertyColumnMappings(propertyColumnMappings);

                            // Add the entity table mapping to the list.
                            entityTableMappings.add(entityTableMapping);
                            
                            // Clear the values.
                            entityClass = null;
                            entityTable = null;
                            historyTable = null;
                            propertyColumnMappings = new ArrayList<PropertyColumnMapping>();
                            break;
                    }
                }
            }
        }
        catch(XMLStreamException e) {
            e.printStackTrace();
        }
        finally {

            try {
                // Check if the XML event reader exists.
                if (reader != null) {

                    // Close the XML event reader.
                    reader.close();
                }
            }
            catch(XMLStreamException e) {
                // Ignore.
            }
        }

        return entityTableMappings;
    }

    /**
     * Convert a string to underscore style.
     * For example, the camel case string "columnKey" will be converted
     * to "column_key".
     *
     * @param  string  the string.
     *
     * @return  the string in underscore style.
     */
    private static String toUnderscore(String string) {

        // Declare.
        CharacterType lastCharacterType;
        StringBuffer stringBuffer;

        // Initialize.
        lastCharacterType = null;
        stringBuffer = new StringBuffer();

        // Loop through the characters in the string.
        for (char character : string.toCharArray()) {

            // Declare.
            CharacterType characterType;

            // Get the character type.
            characterType = CharacterType.get(character);

            // Check if the last character type is lowercase and the character
            // type is not a uppercase.
            if (lastCharacterType == CharacterType.LOWERCASE &&
                characterType == CharacterType.UPPERCASE) {

                // Append the underscore to the string buffer.
                stringBuffer.append("_");
            }

            // Append the lowercase character to the string buffer.
            stringBuffer.append(Character.toLowerCase(character));

            // Set the last character type to the character type.
            lastCharacterType = characterType;
        }

        return stringBuffer.toString();
    }

    /**
     * Validate the mapping source against the schema source.
     *
     * @param  mappingSource  the mapping source.
     * @param  schemaSource   the schema source.
     */
    private static void validate(Source mappingSource, Source schemaSource) {

        try {
            // Declare.
            SchemaFactory factory;
            Schema schema;
            Validator validator;

            // Initialize.
            factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = factory.newSchema(schemaSource);
            validator = schema.newValidator();

            // Validate the mapping source against the schema source.
            validator.validate(mappingSource);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        catch(SAXException e) {
            e.printStackTrace();
        }
    }
}
