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
import java.util.ArrayList;
import java.util.List;

/**
 * A Mojo that sorts the dependencies in the POM file of a Maven project.
 * This Mojo sorts both the `<dependencies>` and `<dependencyManagement>` elements by the groupId and artifactId of each dependency.
 * The sorting is done during the `compile` phase of the Maven build lifecycle.
 */
@Mojo(name = "sort-dependencies", defaultPhase = LifecyclePhase.COMPILE)
public class SortDependenciesMojo extends AbstractMojo {

    /**
     * The Maven project for which the dependencies should be sorted.
     * This parameter is injected by Maven.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Executes the Mojo to sort dependencies in the project's POM file.
     * This method parses the POM file, sorts the `<dependencies>` and `<dependencyManagement>` sections,
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
        sortDependencyElement(pomXmlDocument, "dependencies", projectArtifactId);
        sortDependencyElement(pomXmlDocument, "dependencyManagement", projectArtifactId);
        XmlHelper.write(pomFile, pomXmlDocument);
    }

    /**
     * Sorts the dependencies within a specific parent element (either `<dependencies>` or `<dependencyManagement>`)
     * in the POM file. The sorting is done alphabetically by groupId, and then by artifactId.
     *
     * @param pomXmlDocument    The parsed POM document.
     * @param parentElementName The name of the parent element that contains the dependencies, either "dependencies" or "dependencyManagement".
     * @param projectArtifactId The artifactId of the project, used for logging purposes.
     */
    private void sortDependencyElement(Document pomXmlDocument, final String parentElementName, final String projectArtifactId) {
        getLog().info(String.format("Sorting <%s> element for module %s", parentElementName, projectArtifactId));
        NodeList dependenciesNode = pomXmlDocument.getElementsByTagName(parentElementName);
        if (dependenciesNode.getLength() == 0) {
            getLog().info(String.format("No <%s> element found in module %s", parentElementName, projectArtifactId));
            return;
        }

        Element dependenciesElement = (Element) dependenciesNode.item(0);
        NodeList dependencyNodeList = dependenciesElement.getElementsByTagName("dependency");
        if (dependencyNodeList.getLength() == 0) {
            getLog().info("No <dependency> element found in module " + projectArtifactId);
            return;
        }

        // Collect all dependency elements
        List<Element> dependencyElementList = new ArrayList<>();
        for (int i = 0, length = dependencyNodeList.getLength(); i < length; i++) {
            Node node = dependencyNodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                dependencyElementList.add(element);
            }
        }

        // Sort the dependencies based on groupId and artifactId
        dependencyElementList.sort((element1, element2) -> {
            final String groupId1 = element1.getElementsByTagName("groupId").item(0).getTextContent();
            final String groupId2 = element2.getElementsByTagName("groupId").item(0).getTextContent();
            final String artifactId1 = element1.getElementsByTagName("artifactId").item(0).getTextContent();
            final String artifactId2 = element2.getElementsByTagName("artifactId").item(0).getTextContent();
            return groupId1.equals(groupId2) ? artifactId1.compareTo(artifactId2) : groupId1.compareTo(groupId2);
        });

        // Clear all existing dependencies and append the sorted ones
        while (dependenciesElement.hasChildNodes()) {
            dependenciesElement.removeChild(dependenciesElement.getFirstChild());
        }
        dependencyElementList.forEach(dependenciesElement::appendChild);

        getLog().info(String.format("Sorted %d <dependency> element for module %s and parent element <%s>",
            dependencyElementList.size(), projectArtifactId, parentElementName)
        );
    }

}
