package com.github.innobuds.maven.plugin.util;

import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing and writing XML files.
 * This class provides static methods to read an XML document from a file and write a modified XML document back to a file.
 * It is not meant to be instantiated.
 *
 * @author <a href="https://github.com/codeboyzhou">codeboyzhou</a>
 * @since 1.0.0
 */
public final class XmlHelper {

    /**
     * Non-public constructor to prevent instantiation of the utility class.
     * This class should not be instantiated.
     * package-private access for testing.
     */
    XmlHelper() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Parses an XML file into a {@link Document} object.
     *
     * @param xmlFile The XML file to be parsed.
     * @return The parsed {@link Document} object representing the XML content.
     * @throws MojoExecutionException If there is an error during parsing the XML file.
     *                                This exception wraps any underlying {@link ParserConfigurationException},
     *                                {@link SAXException}, or {@link IOException}.
     */
    public static Document parse(File xmlFile) throws MojoExecutionException {
        DocumentBuilderFactory xmlDocumentFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder xmlDocumentBuilder = xmlDocumentFactory.newDocumentBuilder();
            return xmlDocumentBuilder.parse(xmlFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new MojoExecutionException("Error parsing pom.xml: " + e.getMessage(), e);
        }
    }

    /**
     * Writes a {@link Document} object to an XML file.
     * This method transforms the {@link Document} and writes it to the specified file,
     * with indentation to improve readability.
     *
     * @param xmlFile     The target file to write the XML content to.
     * @param xmlDocument The {@link Document} object containing the XML content to be written.
     * @throws MojoExecutionException If there is an error during the writing process.
     *                                This exception wraps any underlying {@link TransformerException}.
     */
    public static void write(File xmlFile, Document xmlDocument) throws MojoExecutionException {
        try {
            // Generate the XML content using regular transformer
            TransformerFactory xmlTransformerFactory = TransformerFactory.newInstance();
            Transformer xmlTransformer = xmlTransformerFactory.newTransformer();
            xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xmlTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // Write to a string first
            StringWriter stringWriter = new StringWriter();
            xmlTransformer.transform(new DOMSource(xmlDocument), new StreamResult(stringWriter));
            final String xmlContent = stringWriter.toString();

            // Apply custom formatting for the <project> element attributes
            final String formattedXml = formatProjectElementAttributes(xmlContent);

            // Write the formatted XML to the file
            try (FileWriter fileWriter = new FileWriter(xmlFile)) {
                fileWriter.write(formattedXml);
            }
        } catch (TransformerException | IOException e) {
            throw new MojoExecutionException("Error updating pom.xml: " + e.getMessage(), e);
        }
    }

    /**
     * Formats the &lt;project&gt; element attributes to be on separate lines.
     * This method is used to ensure that &lt;project&gt; attributes in the
     * POM file are aligned vertically rather than being on the same line.
     *
     * @param xmlContent The XML content as a string
     * @return The formatted XML content with &lt;project&gt; attributes on separate lines
     */
    private static String formatProjectElementAttributes(String xmlContent) {
        if (xmlContent == null || xmlContent.isEmpty()) {
            return xmlContent;
        }

        // Regular expression to find the <project> element opening tag with attributes
        Pattern projectPattern = Pattern.compile("<project([^>]*)>", Pattern.DOTALL);
        Matcher projectMatcher = projectPattern.matcher(xmlContent);

        if (projectMatcher.find()) {
            final String attributesText = projectMatcher.group(1);

            // Skip if there are no attributes or just a few
            if (attributesText.trim().isEmpty()) {
                return xmlContent;
            }

            // Extract attributes using a regex pattern
            Pattern attrPattern = Pattern.compile("(\\s+[\\w:]+)=\"([^\"]*)\"");
            Matcher attrMatcher = attrPattern.matcher(attributesText);

            List<String> attributesList = new ArrayList<>();
            while (attrMatcher.find()) {
                attributesList.add(attrMatcher.group(1) + "=\"" + attrMatcher.group(2) + "\"");
            }

            // Skip the formatting if there is only 1-2 attributes (not worth reformatting)
            if (attributesList.size() <= 2) {
                return xmlContent;
            }

            // Create indented attributes format
            StringBuilder formattedAttributes = new StringBuilder(attributesList.get(0));
            final String indent = "        "; // 8 spaces for indentation
            // Skip the first attribute
            for (int i = 1, size = attributesList.size(); i < size; i++) {
                final String attribute = attributesList.get(i);
                formattedAttributes.append("\n").append(indent).append(attribute);
            }

            // Replace the original project tag with formatted
            final String formattedProjectTag = "<project" + formattedAttributes + ">";
            return xmlContent.replace(projectMatcher.group(0), formattedProjectTag);
        }

        return xmlContent;
    }

}
