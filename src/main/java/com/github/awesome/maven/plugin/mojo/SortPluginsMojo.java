package com.github.awesome.maven.plugin.mojo;

import com.github.awesome.maven.plugin.util.DomHelper;
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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A Mojo that sorts the maven plugins in the POM file of a Maven project.
 * This Mojo sorts both the &lt;plugins&gt; elements by the groupId and artifactId of each plugin.
 * By default, the sorting is done during the `compile` phase of the Maven build lifecycle.
 *
 * @author <a href="https://github.com/codeboyzhou">codeboyzhou</a>
 * @since 1.0.0
 */
@Mojo(name = "sort-plugins", defaultPhase = LifecyclePhase.COMPILE)
public class SortPluginsMojo extends AbstractMojo {

    /**
     * The Maven project for which the elements should be sorted.
     * This parameter is injected by Maven.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Executes the Mojo to sort maven plugins in the project's POM file.
     * This method parses the POM file, sorts the &lt;plugins&gt; sections,
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
        sortPluginElement(pomXmlDocument, projectArtifactId);
        XmlHelper.write(pomFile, pomXmlDocument);
    }

    /**
     * Sorts the &lt;plugin&gt; elements in the POM file.
     * The sorting is done alphabetically by groupId, and then by artifactId.
     *
     * @param pomXmlDocument    The parsed POM document.
     * @param projectArtifactId The artifactId of the project, used for logging purposes.
     */
    private void sortPluginElement(Document pomXmlDocument, final String projectArtifactId) {
        getLog().info(String.format("Sorting <plugins> element for module %s", projectArtifactId));
        NodeList pluginsNode = pomXmlDocument.getElementsByTagName("plugins");
        if (pluginsNode.getLength() == 0) {
            getLog().info(String.format("No <plugins> element found in module %s", projectArtifactId));
            return;
        }

        Element pluginsElement = (Element) pluginsNode.item(0);
        if (pluginsElement.getElementsByTagName("plugin").getLength() == 0) {
            getLog().info("No <plugin> element found in module " + projectArtifactId);
            return;
        }

        // Collect all plugin elements
        NodeList pluginsElementChildNodes = pluginsElement.getChildNodes();
        TreeMap<String, Element> pluginElementMap = new TreeMap<>();
        Map<String, Node> commentsMap = new HashMap<>();
        for (int i = 0, length = pluginsElementChildNodes.getLength(); i < length; i++) {
            Node node = pluginsElementChildNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                final String elementUniqueKey = DomHelper.getElementUniqueKey(element);
                pluginElementMap.put(elementUniqueKey, element);
                // Check for comment nodes before the <plugin> element
                Node commentNode = DomHelper.findCommentNodeOf(element);
                commentsMap.put(elementUniqueKey, commentNode);
            }
        }

        // Clear all existing plugins and append the sorted ones
        while (pluginsElement.hasChildNodes()) {
            pluginsElement.removeChild(pluginsElement.getFirstChild());
        }
        pluginElementMap.forEach((elementUniqueKey, element) -> {
            Node commentNode = commentsMap.get(elementUniqueKey);
            if (commentNode != null) {
                pluginsElement.appendChild(commentNode);
            }
            pluginsElement.appendChild(element);
        });

        getLog().info(String.format("Sorted %d <plugin> element for module %s", pluginElementMap.size(), projectArtifactId));
    }

}
