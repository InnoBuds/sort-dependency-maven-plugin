package com.github.awesome.maven.plugin.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;

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
    void testExecute_DependenciesIncludeDependency() throws MojoExecutionException, NoSuchFieldException, IllegalAccessException {
        SortDependenciesMojo mojo = new SortDependenciesMojo();
        MavenProject project = mock(MavenProject.class);
        when(project.getFile()).thenReturn(new File("src/test/resources/test-pom-dependencies-include-dependency.xml"));
        when(project.getArtifactId()).thenReturn("test-pom-dependencies-include-dependency");
        Field projectField = SortDependenciesMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);
        mojo.execute();
        verify(project).getFile();
        verify(project).getArtifactId();
    }

}
