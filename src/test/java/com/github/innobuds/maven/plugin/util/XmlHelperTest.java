package com.github.innobuds.maven.plugin.util;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class XmlHelperTest {

    @Test
    void testNewInstance() {
        UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, XmlHelper::new);
        assertEquals("Utility class should not be instantiated", e.getMessage());
    }

    @Test
    void testParse() {
        File pomFile = new File("src/test/resources/no-such-file.xml");
        MojoExecutionException e = assertThrows(MojoExecutionException.class, () -> XmlHelper.parse(pomFile));
        assertTrue(e.getMessage().contains("Error parsing pom.xml"));
    }

}
