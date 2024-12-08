package com.github.awesome.maven.plugin.mojo;

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
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "sort-dependencies", defaultPhase = LifecyclePhase.COMPILE)
public class SortDependenciesMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        File pomFile = project.getFile();
        final String projectArtifactId = project.getArtifactId();

        try {
            DocumentBuilderFactory xmlDocumentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder xmlDocumentBuilder = xmlDocumentFactory.newDocumentBuilder();
            Document pomXmlDocument = xmlDocumentBuilder.parse(pomFile);

            getLog().info("Sorting <dependencies> for " + projectArtifactId);
            NodeList dependenciesNode = pomXmlDocument.getElementsByTagName("dependencies");
            if (dependenciesNode.getLength() == 0) {
                getLog().info("No <dependencies> found in " + projectArtifactId);
                return;
            }

            Element dependenciesElement = (Element) dependenciesNode.item(0);
            NodeList dependencyNodeList = dependenciesElement.getElementsByTagName("dependency");
            if (dependencyNodeList.getLength() == 0) {
                getLog().info("No <dependency> found in " + projectArtifactId);
                return;
            }

            List<Element> dependencyElementList = new ArrayList<>();
            for (int i = 0; i < dependencyNodeList.getLength(); i++) {
                Node node = dependencyNodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    dependencyElementList.add(element);
                }
            }

            dependencyElementList.sort((element1, element2) -> {
                final String groupId1 = element1.getElementsByTagName("groupId").item(0).getTextContent();
                final String groupId2 = element2.getElementsByTagName("groupId").item(0).getTextContent();
                final String artifactId1 = element1.getElementsByTagName("artifactId").item(0).getTextContent();
                final String artifactId2 = element2.getElementsByTagName("artifactId").item(0).getTextContent();
                return groupId1.equals(groupId2) ? artifactId1.compareTo(artifactId2) : groupId1.compareTo(groupId2);
            });

            while (dependenciesElement.hasChildNodes()) {
                dependenciesElement.removeChild(dependenciesElement.getFirstChild());
            }
            dependencyElementList.forEach(dependenciesElement::appendChild);

            TransformerFactory xmlTransformerFactory = TransformerFactory.newInstance();
            Transformer xmlTransformer = xmlTransformerFactory.newTransformer();
            xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource xmlSource = new DOMSource(pomXmlDocument);
            StreamResult xmlResult = new StreamResult(pomFile);
            xmlTransformer.transform(xmlSource, xmlResult);
            getLog().info("Sorted " + dependencyElementList.size() + " <dependency> for " + projectArtifactId);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new MojoExecutionException("Error parsing pom.xml: " + e.getMessage(), e);
        } catch (TransformerException e) {
            throw new MojoExecutionException("Error updating pom.xml: " + e.getMessage(), e);
        }
    }

}
