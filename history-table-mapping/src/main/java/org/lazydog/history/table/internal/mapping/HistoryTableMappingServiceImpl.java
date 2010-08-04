package org.lazydog.history.table.internal.mapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.lazydog.history.table.spi.mapping.HistoryTableMappingService;
import org.xml.sax.SAXException;


/**
 * History table mapping service implementation.
 *
 * @author  Ron Rickard
 */
public class HistoryTableMappingServiceImpl implements HistoryTableMappingService {

    private static final String HISTORY_TABLE_MAPPING_SCHEMA = "history-table-mappings.xsd";
    private static final String HISTORY_TABLE_MAPPING_CONFIG = "META-INF/history-table-mappings.xml";
    private static Map<String,String> tableMap;
    private static Map<String,String> columnMap;

    private Source getMappingSource() {
        return new StreamSource(getClass().getClassLoader().getResourceAsStream(HISTORY_TABLE_MAPPING_CONFIG));
    }

    private Source getSchemaSource() {
        return new StreamSource(ClassLoader.getSystemResourceAsStream(HISTORY_TABLE_MAPPING_SCHEMA));
    }

    public void parse() {

        try {

            // Declare.
            XMLEventReader reader;
            XMLInputFactory factory;

            factory = XMLInputFactory.newInstance();
            reader = factory.createXMLEventReader(this.getMappingSource());

            // Loop through the XML events.
            while (reader.hasNext()) {

                // Declare.
                String entityClassName;
                String entityClassProperty;
                String entityClassTable;
                String historyTable;
                String historyTableColumn;
                XMLEvent event;

                // Initialize.
                entityClassName = null;
                entityClassProperty = null;
                entityClassTable = null;
                historyTable = null;
                historyTableColumn = null;

                // Get the next event.
                event = reader.nextEvent();

                tableMap = new HashMap<String,String>();
                columnMap = new HashMap<String,String>();

                // Check if the event is an  start element.
                if (event.isStartElement()) {

                    switch(elementName(event)) {

                        case ENTITY_CLASS_NAME:
                            entityClassName = elementData(reader.nextEvent());
                            break;

                        case ENTITY_CLASS_PROPERTY:
                            entityClassProperty = elementData(reader.nextEvent());
                            break;

                        case ENTITY_CLASS_TABLE:
                            entityClassTable = elementData(reader.nextEvent());
                            break;

                        case FIELD:
                            if (entityClassProperty != null) {

                                if (historyTableColumn == null) {
                                    historyTableColumn = defaultHistoryTableColumn(entityClassProperty);
                                }

                                entityClassProperty = null;
                            }
                            break;

                        case HISTORY_TABLE:
                            historyTable = elementData(reader.nextEvent());
                            break;

                        case HISTORY_TABLE_COLUMN:
                            historyTableColumn = elementData(reader.nextEvent());
                            break;

                        case MAPPING:
                            if (entityClassName != null) {

                            }
                            break;
                    }
                }
                else if (event.isEndDocument()) {

                }
            }

            reader.close();

        }
        catch(XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private static String addUnderscores(String name) {

        Pattern pattern;

        pattern = Pattern.compile("(.*)([A-Z]*)(.*)");

        return null;
    }

    private static String defaultHistoryTableColumn(String entityClassProperty) {

        return entityClassProperty.replaceAll("[A-Z]", entityClassProperty).replaceAll(".", "_").toLowerCase();
    }

    private static String elementData(XMLEvent event) {
        return event.asCharacters().getData();
    }

    private static ElementName elementName(XMLEvent event) {
        return elementName(event.asStartElement().getName().getLocalPart());
    }

    private static ElementName elementName(String name) {
        return ElementName.valueOf(name.toUpperCase().replaceAll("-", "_"));
    }

    public void validate() {

        try {
            // Declare.
            SchemaFactory factory;
            Schema schema;
            Validator validator;

            factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = factory.newSchema(this.getSchemaSource());

            validator = schema.newValidator();

            validator.validate(this.getMappingSource());
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        catch(SAXException e) {
            e.printStackTrace();
        }
    }

    
}
