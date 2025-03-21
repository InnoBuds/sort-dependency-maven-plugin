package com.github.innobuds.maven.plugin.mojo;

import com.github.innobuds.maven.plugin.util.DomHelper;
import com.github.innobuds.maven.plugin.util.XmlHelper;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A Mojo that sorts the properties in the POM file of a Maven project.
 * This Mojo sorts the child elements of &lt;properties&gt; element by the tag name of each child element.
 * By default, the sorting is done during the `compile` phase of the Maven build lifecycle.
 *
 * @author <a href="https://github.com/codeboyzhou">codeboyzhou</a>
 * @since 1.1.0
 */
@Mojo(name = "sort-properties", defaultPhase = LifecyclePhase.COMPILE)
public class SortPropertiesMojo extends AbstractMojo {

    /**
     * Default comment for maven plugin versions.
     */
    private static final String MAVEN_PLUGIN_VERSION_COMMENT = "==================== maven plugin versions ====================";

    /**
     * Default comment for dependency versions.
     */
    private static final String DEPENDENCY_VERSION_COMMENT = "==================== dependency versions ======================";

    /**
     * The Maven project for which the elements should be sorted.
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

        // Collect and group all the properties elements
        ChildNodeGroup childNodeGroup = groupingPropertiesChildNodes(projectArtifactId, childNodes);

        // Clear all existing properties and re-arrange them
        DomHelper.removeAllChildNodesOf(propertiesElement);

        childNodeGroup.skippedElementCommentMap.forEach((elementTagName, commentNode) -> {
            if (commentNode != null) {
                propertiesElement.appendChild(commentNode);
            }
            propertiesElement.appendChild(childNodeGroup.skippedElementMap.get(elementTagName));
        });

        if (!childNodeGroup.mavenPluginVersionElementCommentMap.isEmpty()) {
            propertiesElement.appendChild(pomXmlDocument.createComment(MAVEN_PLUGIN_VERSION_COMMENT));
        }
        childNodeGroup.mavenPluginVersionElementCommentMap.forEach((elementTagName, commentNode) -> {
            if (commentNode != null && !commentNode.getTextContent().trim().equals(MAVEN_PLUGIN_VERSION_COMMENT)) {
                propertiesElement.appendChild(commentNode);
            }
            propertiesElement.appendChild(childNodeGroup.mavenPluginVersionElementMap.get(elementTagName));
        });

        if (!childNodeGroup.dependencyVersionElementCommentMap.isEmpty()) {
            propertiesElement.appendChild(pomXmlDocument.createComment(DEPENDENCY_VERSION_COMMENT));
        }
        childNodeGroup.dependencyVersionElementCommentMap.forEach((elementTagName, commentNode) -> {
            if (commentNode != null && !commentNode.getTextContent().trim().equals(DEPENDENCY_VERSION_COMMENT)) {
                propertiesElement.appendChild(commentNode);
            }
            propertiesElement.appendChild(childNodeGroup.dependencyVersionElementMap.get(elementTagName));
        });

        final int sortedSize = childNodeGroup.mavenPluginVersionElementCommentMap.size() + childNodeGroup.dependencyVersionElementCommentMap.size();
        getLog().info(String.format("Sorted %d <properties> element for module %s", sortedSize, projectArtifactId));
    }

    /**
     * The child node group for properties element.
     */
    private static class ChildNodeGroup {
        /**
         * The map to store skipped element comments.
         */
        Map<String, Node> skippedElementCommentMap = new LinkedHashMap<>();

        /**
         * The map to store maven plugin version element comments.
         */
        Map<String, Node> dependencyVersionElementCommentMap = new TreeMap<>();

        /**
         * The map to store dependency version element comments.
         */
        Map<String, Node> mavenPluginVersionElementCommentMap = new TreeMap<>();

        /**
         * The map to store skipped elements.
         */
        Map<String, Element> skippedElementMap = new LinkedHashMap<>();

        /**
         * The map to store maven plugin version elements.
         */
        Map<String, Element> dependencyVersionElementMap = new TreeMap<>();

        /**
         * The map to store dependency version elements.
         */
        Map<String, Element> mavenPluginVersionElementMap = new TreeMap<>();
    }

    /**
     * Grouping properties child nodes by their prefix or suffix.
     * For reducing the Cognitive Complexity of the method {@link #sortPropertiesElement(Document, String)}.
     *
     * @param projectArtifactId The artifactId of the project, used for logging purposes.
     * @param childNodes        The child nodes of the properties element.
     * @return The child node group object that stores the grouped child nodes.
     */
    private ChildNodeGroup groupingPropertiesChildNodes(String projectArtifactId, NodeList childNodes) {
        ChildNodeGroup childNodeGroup = new ChildNodeGroup();
        for (int i = 0, length = childNodes.getLength(); i < length; i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                final String elementTagName = element.getTagName();
                // Skip elements that are not version properties or we don't want to sort
                if (!elementTagName.endsWith(".version") || elementTagName.equals("java.version") || elementTagName.equals("kotlin.version")) {
                    getLog().info(String.format("Skipping element %s in <properties> element for module %s", elementTagName, projectArtifactId));
                    Node commentNode = DomHelper.findCommentNodeOf(element);
                    childNodeGroup.skippedElementCommentMap.put(elementTagName, commentNode);
                    childNodeGroup.skippedElementMap.put(elementTagName, element);
                    continue;
                }
                // Group elements by their prefix or suffix
                Node commentNode = DomHelper.findCommentNodeOf(element);
                if (elementTagName.startsWith("maven-") || elementTagName.endsWith("-maven-plugin.version")) {
                    childNodeGroup.mavenPluginVersionElementCommentMap.put(elementTagName, commentNode);
                    childNodeGroup.mavenPluginVersionElementMap.put(elementTagName, element);
                } else {
                    childNodeGroup.dependencyVersionElementCommentMap.put(elementTagName, commentNode);
                    childNodeGroup.dependencyVersionElementMap.put(elementTagName, element);
                }
            }
        }
        return childNodeGroup;
    }

}
