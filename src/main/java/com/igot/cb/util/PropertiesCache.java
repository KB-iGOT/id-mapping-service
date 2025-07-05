package com.igot.cb.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class PropertiesCache {

    private final Properties configProp = new Properties();

    private static PropertiesCache INSTANCE = null;

    private PropertiesCache() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                configProp.load(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties file", e);
        }
    }

    public static PropertiesCache getInstance() {
        if (null == INSTANCE) {
			synchronized (PropertiesCache.class) {
				if (null == INSTANCE) {
					INSTANCE = new PropertiesCache();
				}
			}
		}
        return INSTANCE;
    }

    public String getProperty(String key) {
        String value = System.getenv(key);
        if (StringUtils.isNotBlank(value)) return value;
        return configProp.getProperty(key);
    }
}