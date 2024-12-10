package com.github.awesome.maven.plugin.util;

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
import java.io.IOException;

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
     * Private constructor to prevent instantiation of the utility class.
     * This class should not be instantiated.
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
        TransformerFactory xmlTransformerFactory = TransformerFactory.newInstance();
        try {
            Transformer xmlTransformer = xmlTransformerFactory.newTransformer();
            xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xmlTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource xmlSource = new DOMSource(xmlDocument);
            StreamResult xmlResult = new StreamResult(xmlFile);
            xmlTransformer.transform(xmlSource, xmlResult);
        } catch (TransformerException e) {
            throw new MojoExecutionException("Error updating pom.xml: " + e.getMessage(), e);
        }
    }

}
