import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class XMLValidator {

    public static void main(String[] args) {
        List<Source> schemaSources = new ArrayList<>();
        List<String> handledFiles = new ArrayList<>();
        Schema schema;

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("xsd"))) {
            for (Path entry:ds) {
                if (Files.isRegularFile(entry) && entry.getFileName().toString().toLowerCase().endsWith(".xsd")) {
                    addToSources(schemaSources, handledFiles, entry);
                }
            }
            System.out.println("Read " + schemaSources.size() + " schema files.");
            Source[] sourcesArray = schemaSources.toArray(new Source[0]);
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = factory.newSchema(sourcesArray);
            System.out.println("Created schema.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return;
        }

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("xml"))) {
            for (Path entry:ds) {
                if (Files.isRegularFile(entry) && entry.getFileName().toString().toLowerCase().endsWith(".xml")) {
                    validateXML(schema, entry.toFile());
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Go through each schema source file and check for dependencies, recursively calling this function.
    // Search for lines containing "<xs:import ... schemaLocation="dependency.xsd"/>" to find dependencies.
    private static void addToSources(List<Source> sources, List<String> handledFiles, Path entry) {
        try {
            List<String> lines = Files.readAllLines(entry);
            for (String line : lines) {
                if (line.contains("import") && line.contains("schemaLocation=")) {
                    String schemaFilename = line.split("schemaLocation=\"")[1].split("\"")[0];
                    Path dependencyPath = entry.getParent().resolve(schemaFilename);
                    addToSources(sources, handledFiles, dependencyPath);
                }
            }
            Source dependencySource = new StreamSource(entry.toFile());
            // Add the schema to the sources list only when all the dependencies have been added already,
            // so that schema load order will be correct.
            if (!handledFiles.contains(entry.getFileName().toString())) {
                sources.add(dependencySource);
                handledFiles.add(entry.getFileName().toString());
            }
        } catch (Exception e) {
            System.err.println("Error reading file " + entry + ": " + e.getMessage());
        }
    }

    private static void validateXML(Schema schema, File xmlFile){

        System.out.println("Validating: " + xmlFile.getName());
        try {
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xmlFile));
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return;
        }

        System.out.println("XML is valid.");
    }
}