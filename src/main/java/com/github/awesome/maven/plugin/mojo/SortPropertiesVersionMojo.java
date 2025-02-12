package com.github.awesome.maven.plugin.mojo;

import com.github.awesome.maven.plugin.util.XmlHelper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.*;

/**
 * A Mojo that sorts the properties in the POM file of a Maven project.
 * This Mojo sorts the child elements of &lt;properties&gt; element by the tag name of each child element.
 * The sorting is done during the `compile` phase of the Maven build lifecycle.
 *
 * @author <a href="https://github.com/codeboyzhou">codeboyzhou</a>
 * @since 1.1.0
 */
@Mojo(name = "sort-properties-version", defaultPhase = LifecyclePhase.COMPILE)
public class SortPropertiesVersionMojo extends AbstractMojo {

    /**
     * The Maven project for which the dependencies should be sorted.
     * This parameter is injected by Maven.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Executes the Mojo to sort properties in the project's POM file.
     * This method parses the POM file, sorts the &lt;properties&gt; sections,
     * and then writes the modified POM file back to disk.
     *
     * @throws MojoExecutionException If there is an error during the execution of the Mojo,
     *                                such as an issue reading, parsing, or writing the POM file.
     */
    @Override
    public void execute() throws MojoExecutionException {
        File pomFile = project.getFile();
        Document pomXmlDocument = XmlHelper.parse(pomFile);
        final String projectArtifactId = project.getArtifactId();
        sortPropertiesElement(pomXmlDocument, projectArtifactId);
        XmlHelper.write(pomFile, pomXmlDocument);
    }

    /**
     * Sorts the &lt;properties&gt; elements in the POM file.
     * The sorting is done alphabetically by element's tag name.
     *
     * @param pomXmlDocument    The parsed POM document.
     * @param projectArtifactId The artifactId of the project, used for logging purposes.
     */
    private void sortPropertiesElement(Document pomXmlDocument, final String projectArtifactId) {
        getLog().info(String.format("Sorting <properties> element for module %s", projectArtifactId));
        NodeList propertiesNode = pomXmlDocument.getElementsByTagName("properties");
        if (propertiesNode.getLength() == 0) {
            getLog().info(String.format("No <properties> element found in module %s", projectArtifactId));
            return;
        }

        Element propertiesElement = (Element) propertiesNode.item(0);
        NodeList childNodes = propertiesElement.getChildNodes();
        if (childNodes.getLength() == 0) {
            getLog().info(String.format("No child elements found in <properties> element for module %s", projectArtifactId));
            return;
        }

        // Collect all properties elements
        List<Element> skippedChildNodes = new ArrayList<>();
        List<Element> sortedChildNodes = new ArrayList<>();
        Map<String, Node> commentsMap = new HashMap<>();
        for (int i = 0, length = childNodes.getLength(); i < length; i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                final String elementTagName = element.getTagName();
                // Skip elements that are not version properties or we don't want to sort
                if (!elementTagName.endsWith(".version") || elementTagName.equals("java.version") || elementTagName.equals("kotlin.version")) {
                    getLog().info(String.format("Skipping element %s in <properties> element for module %s", elementTagName, projectArtifactId));
                    skippedChildNodes.add(element);
                    continue;
                }
                sortedChildNodes.add(element);
                // Check for comment nodes before the element
                Node previousSibling = element.getPreviousSibling();
                while (previousSibling != null && previousSibling.getNodeType() != Node.COMMENT_NODE) {
                    previousSibling = previousSibling.getPreviousSibling();
                }
                // Find the closest element node after the comment node
                if (previousSibling != null) {
                    Node nextSibling = previousSibling.getNextSibling();
                    while (nextSibling != null && nextSibling.getNodeType() != Node.ELEMENT_NODE) {
                        nextSibling = nextSibling.getNextSibling();
                    }
                    if (nextSibling != null) {
                        Element closetElementToCommentNode = (Element) nextSibling;
                        commentsMap.put(closetElementToCommentNode.getTagName(), previousSibling);
                    }
                }
            }
        }

        // Clear all existing properties and re-arrange them
        while (propertiesElement.hasChildNodes()) {
            propertiesElement.removeChild(propertiesElement.getFirstChild());
        }
        skippedChildNodes.forEach(propertiesElement::appendChild);
        sortedChildNodes.stream().sorted(Comparator.comparing(Element::getTagName)).forEach(element -> {
            Node commentNode = commentsMap.get(element.getTagName());
            if (commentNode != null) {
                propertiesElement.appendChild(commentNode);
            }
            propertiesElement.appendChild(element);
        });

        getLog().info(String.format("Sorted %d <properties> element for module %s", sortedChildNodes.size(), projectArtifactId));
    }

}
