package com.github.awesome.maven.plugin.util;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class DomHelperTest {

    @Test
    void testNewInstance() {
        UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, DomHelper::new);
        assertEquals("Utility class should not be instantiated", e.getMessage());
    }

    @Test
    void testFindCommentNodeOfElement() throws MojoExecutionException {
        File xmlFile = new File("src/test/resources/test-pom-properties-include-child-nodes.xml");
        Document xmlDocument = XmlHelper.parse(xmlFile);
        Element propertiesElement = (Element) xmlDocument.getElementsByTagName("properties").item(0);

        Element javaVersionElement = (Element) propertiesElement.getElementsByTagName("java.version").item(0);
        Node javaVersionCommentNode = DomHelper.findCommentNodeOf(javaVersionElement);
        assertNull(javaVersionCommentNode);

        Element kotlinVersionElement = (Element) propertiesElement.getElementsByTagName("kotlin.version").item(0);
        Node kotlinVersionCommentNode = DomHelper.findCommentNodeOf(kotlinVersionElement);
        assertNotNull(kotlinVersionCommentNode);
        assertEquals("This is the kotlin version", kotlinVersionCommentNode.getTextContent().trim());
    }

}
