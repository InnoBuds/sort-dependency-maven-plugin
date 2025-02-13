package com.github.awesome.maven.plugin.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

class SortPropertiesMojoTest {

    @Test
    void testExecute_NoAnyPropertiesTag() throws NoSuchFieldException, IllegalAccessException, MojoExecutionException {
        SortPropertiesMojo mojo = new SortPropertiesMojo();
        MavenProject project = mock(MavenProject.class);
        when(project.getFile()).thenReturn(new File("src/test/resources/test-pom-no-any-properties-tag.xml"));
        when(project.getArtifactId()).thenReturn("test-pom-no-any-properties-tag");
        Field projectField = SortPropertiesMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);
        mojo.execute();
        verify(project).getFile();
        verify(project).getArtifactId();
    }

    @Test
    void testExecute_EmptyPropertiesTag() throws NoSuchFieldException, IllegalAccessException, MojoExecutionException {
        SortPropertiesMojo mojo = new SortPropertiesMojo();
        MavenProject project = mock(MavenProject.class);
        when(project.getFile()).thenReturn(new File("src/test/resources/test-pom-empty-properties-tag.xml"));
        when(project.getArtifactId()).thenReturn("test-pom-empty-properties-tag");
        Field projectField = SortPropertiesMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);
        mojo.execute();
        verify(project).getFile();
        verify(project).getArtifactId();
    }

    @ParameterizedTest
    @CsvSource({
        "src/test/resources/test-pom-properties-include-child-nodes.xml",
        "src/test/resources/test-pom-properties-include-child-nodes-but-no-maven-plugin-versions.xml",
        "src/test/resources/test-pom-properties-include-child-nodes-but-no-dependency-versions.xml"
    })
    void testExecute_PropertiesIncludeChildNodes(String pomFilePath) throws NoSuchFieldException, IllegalAccessException, MojoExecutionException {
        SortPropertiesMojo mojo = new SortPropertiesMojo();
        MavenProject project = mock(MavenProject.class);
        File pomFile = new File(pomFilePath);
        when(project.getFile()).thenReturn(pomFile);
        when(project.getArtifactId()).thenReturn("test-pom-properties-include-child-nodes");
        Field projectField = SortPropertiesMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);
        mojo.execute();
        verify(project).getFile();
        verify(project).getArtifactId();
    }

}
