package com.github.awesome.maven.plugin.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XmlHelperTest {

    @Test
    void testNewInstance() {
        UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, XmlHelper::new);
        assertEquals("Utility class should not be instantiated", e.getMessage());
    }

}
