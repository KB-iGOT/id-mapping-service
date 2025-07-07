package com.igot.cb.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Field;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class PropertiesCacheTest {

    @Test
    void testGetInstanceSingleton() {
        PropertiesCache instance1 = PropertiesCache.getInstance();
        PropertiesCache instance2 = PropertiesCache.getInstance();
        assertNotNull(instance1, "Instance should not be null");
        assertSame(instance1, instance2, "Instances should be the same singleton");
    }

    @Test
    void testGetPropertyFromActualEnvironmentVariable() {
        // Choose a standard environment variable that's almost always present
        String key = "PATH";
        String expected = System.getenv(key);
        assertNotNull(expected, "The environment variable '" + key + "' should be set on this system");

        String actual = PropertiesCache.getInstance().getProperty(key);
        assertEquals(expected, actual, "PropertiesCache should return the same value as System.getenv for key: " + key);
    }

    @Test
    void testGetPropertyFromFileFallback() throws Exception {
        String key = "file.key";
        String expected = "fileValue";

        // Inject into configProp via reflection
        PropertiesCache cache = PropertiesCache.getInstance();
        Field configField = PropertiesCache.class.getDeclaredField("configProp");
        configField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Properties props = (Properties) configField.get(cache);
        props.setProperty(key, expected);

        String actual = cache.getProperty(key);
        assertEquals(expected, actual, "Should fallback to file property");
    }

    @Test
    void testUnknownKeyReturnsNull() {
        String value = PropertiesCache.getInstance().getProperty("non.existent.key");
        assertNull(value, "Unknown keys should return null");
    }
}