package com.github.awesome.maven.plugin.mojo;

import com.github.awesome.maven.plugin.util.DomHelper;
import com.github.awesome.maven.plugin.util.XmlHelper;
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

class SortPluginsMojoTest {

    @Test
    void testExecute_NoAnyPluginTag() throws MojoExecutionException, NoSuchFieldException, IllegalAccessException {
        SortPluginsMojo mojo = new SortPluginsMojo();
        MavenProject project = mock(MavenProject.class);
        when(project.getFile()).thenReturn(new File("src/test/resources/test-pom-no-any-dependency-tag.xml"));
        when(project.getArtifactId()).thenReturn("test-pom-no-any-dependency-tag");
        Field projectField = SortPluginsMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);
        mojo.execute();
        verify(project).getFile();
        verify(project).getArtifactId();
    }

    @Test
    void testExecute_EmptyPluginsTag() throws MojoExecutionException, NoSuchFieldException, IllegalAccessException {
        SortPluginsMojo mojo = new SortPluginsMojo();
        MavenProject project = mock(MavenProject.class);
        when(project.getFile()).thenReturn(new File("src/test/resources/test-pom-empty-dependencies-tag.xml"));
        when(project.getArtifactId()).thenReturn("test-pom-empty-dependencies-tag");
        Field projectField = SortPluginsMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);
        mojo.execute();
        verify(project).getFile();
        verify(project).getArtifactId();
    }

    @Test
    void testExecute_PluginsIncludePluginTags() throws MojoExecutionException, NoSuchFieldException, IllegalAccessException {
        SortPluginsMojo mojo = new SortPluginsMojo();
        MavenProject project = mock(MavenProject.class);
        File pomFile = new File("src/test/resources/test-pom-dependencies-include-dependency-tags.xml");
        when(project.getFile()).thenReturn(pomFile);
        when(project.getArtifactId()).thenReturn("test-pom-dependencies-include-dependency-tags");
        Field projectField = SortPluginsMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);
        mojo.execute();

        Document pomXmlDocument = XmlHelper.parse(pomFile);
        Node pluginsNode = pomXmlDocument.getElementsByTagName("plugins").item(0);
        NodeList pluginsChildNodes = pluginsNode.getChildNodes();
        List<String> commentList = new ArrayList<>();
        List<String> elementUniqueKeyList = new ArrayList<>();
        for (int i = 0; i < pluginsChildNodes.getLength(); i++) {
            Node childNode = pluginsChildNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element pluginElement = (Element) childNode;
                elementUniqueKeyList.add(DomHelper.getElementUniqueKey(pluginElement));
                // Check for comment node before the <plugin> element
                Node commentNode = DomHelper.findCommentNodeOf(pluginElement);
                commentList.add(commentNode == null ? null : commentNode.getTextContent().trim());
            }
        }

        assertNull(commentList.get(1));
        assertEquals("https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-gpg-plugin", commentList.get(0));
        assertEquals("https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-source-plugin", commentList.get(2));
        assertEquals("org.apache.maven.plugins:maven-gpg-plugin", elementUniqueKeyList.get(0));
        assertEquals("org.apache.maven.plugins:maven-javadoc-plugin", elementUniqueKeyList.get(1));
        assertEquals("org.apache.maven.plugins:maven-source-plugin", elementUniqueKeyList.get(2));

        verify(project).getFile();
        verify(project).getArtifactId();
    }

}
