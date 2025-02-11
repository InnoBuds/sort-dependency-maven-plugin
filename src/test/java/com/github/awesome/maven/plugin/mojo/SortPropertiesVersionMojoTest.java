package com.github.awesome.maven.plugin.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

class SortPropertiesVersionMojoTest {

    @Test
    void testExecute_NoAnyPropertiesTag() throws NoSuchFieldException, IllegalAccessException, MojoExecutionException {
        SortPropertiesVersionMojo mojo = new SortPropertiesVersionMojo();
        MavenProject project = mock(MavenProject.class);
        when(project.getFile()).thenReturn(new File("src/test/resources/test-pom-no-any-properties-tag.xml"));
        when(project.getArtifactId()).thenReturn("test-pom-no-any-properties-tag");
        Field projectField = SortPropertiesVersionMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);
        mojo.execute();
        verify(project).getFile();
        verify(project).getArtifactId();
    }

    @Test
    void testExecute_EmptyPropertiesTag() throws NoSuchFieldException, IllegalAccessException, MojoExecutionException {
        SortPropertiesVersionMojo mojo = new SortPropertiesVersionMojo();
        MavenProject project = mock(MavenProject.class);
        when(project.getFile()).thenReturn(new File("src/test/resources/test-pom-empty-properties-tag.xml"));
        when(project.getArtifactId()).thenReturn("test-pom-empty-properties-tag");
        Field projectField = SortPropertiesVersionMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);
        mojo.execute();
        verify(project).getFile();
        verify(project).getArtifactId();
    }

    @Test
    void testExecute_PropertiesIncludeChildNodes() throws NoSuchFieldException, IllegalAccessException, MojoExecutionException {
        SortPropertiesVersionMojo mojo = new SortPropertiesVersionMojo();
        MavenProject project = mock(MavenProject.class);
        File pomFile = new File("src/test/resources/test-pom-properties-include-child-nodes.xml");
        when(project.getFile()).thenReturn(pomFile);
        when(project.getArtifactId()).thenReturn("test-pom-properties-include-child-nodes");
        Field projectField = SortPropertiesVersionMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);
        mojo.execute();
        verify(project).getFile();
        verify(project).getArtifactId();
    }

}
