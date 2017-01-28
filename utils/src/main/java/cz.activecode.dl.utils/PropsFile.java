package cz.activecode.dl.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class PropsFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropsFile.class);

    private File propertyFile;
    private Properties properties;

    public PropsFile(File propertyFile) throws IOException {
        this.propertyFile = propertyFile;
        init();
    }

    private void init() throws IOException {
        if(propertyFile == null) {
            throw new IllegalArgumentException("PropsFile - property file is null");
        }

        properties = new Properties();

        if(propertyFile.exists()) {
            try(InputStream is = new BufferedInputStream(new FileInputStream(propertyFile))) {
                properties.load(is);
            }
        }
    }

    private void store() {
        try(OutputStream os = new BufferedOutputStream(new FileOutputStream(propertyFile))) {
            properties.store(os, null);
        } catch (IOException e) {
            LOGGER.error("Cannot store {}", propertyFile);
        }
    }

    private void updateAndStore(String propName, Object value) {
        properties.setProperty(propName, String.valueOf(value));
        store();
    }

    public int getIntProperty(String propName, int defaultValue) {
        String value = properties.getProperty(propName);
        if(value == null) {
            updateAndStore(propName, defaultValue);
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("Property {} is not a number", propName);
            updateAndStore(propName, String.valueOf(defaultValue));
            return defaultValue;
        }
    }

    public long getLongProperty(String propName, long defaultValue) {
        String value = properties.getProperty(propName);
        if(value == null) {
            updateAndStore(propName, defaultValue);
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("Property {} is not a number", propName);
            updateAndStore(propName, defaultValue);
            return defaultValue;
        }
    }

    public boolean getBooleanProperty(String propName, boolean defaultValue) {
        String value = properties.getProperty(propName);
        if (value == null) {
            updateAndStore(propName, defaultValue);
            return defaultValue;
        }
        if (value.equalsIgnoreCase(Boolean.TRUE.toString())) {
            return true;
        }
        if (value.equalsIgnoreCase(Boolean.FALSE.toString())) {
            return false;
        }
        updateAndStore(propName, defaultValue);
        return defaultValue;
    }

    public String getStringProperty(String propName, boolean allowEmpty, String defaultValue) {
        String value = properties.getProperty(propName);
        if(value == null || (!allowEmpty && value.trim().isEmpty())) {
            updateAndStore(propName, defaultValue);
            return defaultValue;
        }
        return value;
    }

    public double getDoubleProperty(String propName, double defaultValue) {
        String value = properties.getProperty(propName);
        if(value == null) {
            updateAndStore(propName, defaultValue);
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("Property {} is not a number", propName);
            updateAndStore(propName, defaultValue);
            return defaultValue;
        }
    }


    public void setProperty(String propName, Object value) {
        updateAndStore(propName, value);
    }
}
