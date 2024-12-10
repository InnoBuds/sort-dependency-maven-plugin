package com.github.awesome.maven.plugin.mojo;

import com.github.awesome.maven.plugin.util.XmlHelper;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SortDependenciesMojoTest {

    @Test
    void testExecute_NoAnyDependency() throws MojoExecutionException, NoSuchFieldException, IllegalAccessException {
        SortDependenciesMojo mojo = new SortDependenciesMojo();
        MavenProject project = mock(MavenProject.class);
        when(project.getFile()).thenReturn(new File("src/test/resources/test-pom-no-any-dependency.xml"));
        when(project.getArtifactId()).thenReturn("test-pom-no-any-dependency");
        Field projectField = SortDependenciesMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);
        mojo.execute();
        verify(project).getFile();
        verify(project).getArtifactId();
    }

    @Test
    void testExecute_OnlyDependenciesTag() throws MojoExecutionException, NoSuchFieldException, IllegalAccessException {
        SortDependenciesMojo mojo = new SortDependenciesMojo();
        MavenProject project = mock(MavenProject.class);
        when(project.getFile()).thenReturn(new File("src/test/resources/test-pom-only-dependencies-tag.xml"));
        when(project.getArtifactId()).thenReturn("test-pom-only-dependencies-tag");
        Field projectField = SortDependenciesMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);
        mojo.execute();
        verify(project).getFile();
        verify(project).getArtifactId();
    }

    @Test
    void testExecute_DependenciesIncludeDependency() throws MojoExecutionException, NoSuchFieldException, IllegalAccessException, IOException {
        SortDependenciesMojo mojo = new SortDependenciesMojo();
        MavenProject project = mock(MavenProject.class);
        File pomFile = new File("src/test/resources/test-pom-dependencies-include-dependency.xml");
        when(project.getFile()).thenReturn(pomFile);
        when(project.getArtifactId()).thenReturn("test-pom-dependencies-include-dependency");
        Field projectField = SortDependenciesMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);
        mojo.execute();
        Document pomXmlDocument = XmlHelper.parse(pomFile);
        Node dependencies = pomXmlDocument.getElementsByTagName("dependencies").item(0);
        NodeList dependencyNodeList = dependencies.getChildNodes();
        List<String> dependencyInfo = new ArrayList<>(dependencyNodeList.getLength());
        for (int i = 0; i < dependencyNodeList.getLength(); i++) {
            Node dependencyNode = dependencyNodeList.item(i);
            if (dependencyNode.getNodeType() == Node.ELEMENT_NODE) {
                Element dependencyElement = (Element) dependencyNode;
                final String groupId = dependencyElement.getElementsByTagName("groupId").item(0).getTextContent();
                final String artifactId = dependencyElement.getElementsByTagName("artifactId").item(0).getTextContent();
                final String version = dependencyElement.getElementsByTagName("version").item(0).getTextContent();
                dependencyInfo.add(groupId + ":" + artifactId + ":" + version);
            }
        }
        assertEquals("com.google:guava:33.3.1-jre", dependencyInfo.get(0));
        assertEquals("org.apache.commons:commons-collections4:4.4", dependencyInfo.get(1));
        assertEquals("org.apache.commons:commons-lang3:3.17.0", dependencyInfo.get(2));
        verify(project).getFile();
        verify(project).getArtifactId();
    }

}
