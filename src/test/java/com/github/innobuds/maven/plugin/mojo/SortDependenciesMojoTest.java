package com.github.innobuds.maven.plugin.mojo;

import com.github.innobuds.maven.plugin.util.DomHelper;
import com.github.innobuds.maven.plugin.util.XmlHelper;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class SortDependenciesMojoTest {

    @Test
    void testExecute_NoAnyDependencyTag() throws MojoExecutionException, NoSuchFieldException, IllegalAccessException {
        SortDependenciesMojo mojo = new SortDependenciesMojo();
        MavenProject project = mock(MavenProject.class);
        when(project.getFile()).thenReturn(new File("src/test/resources/test-pom-no-any-dependency-tag.xml"));
        when(project.getArtifactId()).thenReturn("test-pom-no-any-dependency-tag");
        Field projectField = SortDependenciesMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);
        mojo.execute();
        verify(project).getFile();
        verify(project).getArtifactId();
    }

    @Test
    void testExecute_EmptyDependenciesTag() throws MojoExecutionException, NoSuchFieldException, IllegalAccessException {
        SortDependenciesMojo mojo = new SortDependenciesMojo();
        MavenProject project = mock(MavenProject.class);
        when(project.getFile()).thenReturn(new File("src/test/resources/test-pom-empty-dependencies-tag.xml"));
        when(project.getArtifactId()).thenReturn("test-pom-empty-dependencies-tag");
        Field projectField = SortDependenciesMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);
        mojo.execute();
        verify(project).getFile();
        verify(project).getArtifactId();
    }

    @Test
    void testExecute_DependenciesIncludeDependencyTags() throws MojoExecutionException, NoSuchFieldException, IllegalAccessException {
        SortDependenciesMojo mojo = new SortDependenciesMojo();
        MavenProject project = mock(MavenProject.class);
        File pomFile = new File("src/test/resources/test-pom-dependencies-include-dependency-tags.xml");
        when(project.getFile()).thenReturn(pomFile);
        when(project.getArtifactId()).thenReturn("test-pom-dependencies-include-dependency-tags");
        Field projectField = SortDependenciesMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);
        mojo.execute();

        Document pomXmlDocument = XmlHelper.parse(pomFile);
        Node dependenciesNode = pomXmlDocument.getElementsByTagName("dependencies").item(0);
        NodeList dependenciesChildNodes = dependenciesNode.getChildNodes();
        List<String> commentList = new ArrayList<>();
        List<String> elementUniqueKeyList = new ArrayList<>();
        for (int i = 0; i < dependenciesChildNodes.getLength(); i++) {
            Node childNode = dependenciesChildNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element dependencyElement = (Element) childNode;
                elementUniqueKeyList.add(DomHelper.getElementUniqueKey(dependencyElement));
                // Check for comment node before the <dependency> element
                Node commentNode = DomHelper.findCommentNodeOf(dependencyElement);
                commentList.add(commentNode == null ? null : commentNode.getTextContent().trim());
            }
        }

        assertNull(commentList.get(1));
        assertNull(commentList.get(3));
        assertEquals("https://mvnrepository.com/artifact/com.alibaba/fastjson", commentList.get(0));
        assertEquals("https://mvnrepository.com/artifact/org.apache.commons/commons-collections4", commentList.get(2));
        assertEquals("com.alibaba:fastjson", elementUniqueKeyList.get(0));
        assertEquals("com.google:guava", elementUniqueKeyList.get(1));
        assertEquals("org.apache.commons:commons-collections4", elementUniqueKeyList.get(2));
        assertEquals("org.apache.commons:commons-lang3", elementUniqueKeyList.get(3));

        verify(project).getFile();
        verify(project).getArtifactId();
    }

}
