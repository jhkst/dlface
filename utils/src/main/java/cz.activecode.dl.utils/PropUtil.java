package cz.activecode.dl.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Utilities for properties reading.
 */
public class PropUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropUtil.class);

    /**
     * Returns {@code int} property with given name if present. If not present or
     * value is not integer number then default value is returned.
     * @param properties properties where to search for property
     * @param propName name of the property
     * @param defaultValue default value for the property
     * @return property value or {@code defaultValue} if not present or not an integer.
     */
    public static int getIntProperty(Properties properties, String propName, int defaultValue) {
        String value = properties.getProperty(propName);
        if(value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("Property {} is not a number", propName);
            return defaultValue;
        }
    }

    /**
     * Returns {@code long} property with given name if present. If not present or
     * value is not long number then default value is returned.
     * @param properties properties where to search for property
     * @param propName name of the property
     * @param defaultValue default value for the property
     * @return property value or {@code defaultValue} if not present or not an integer.
     */
    public static long getLongProperty(Properties properties, String propName, long defaultValue) {
        String value = properties.getProperty(propName);
        if(value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("Property {} is not a number", propName);
            return defaultValue;
        }
    }

    /**
     * Returns {@code boolean} property with given name if present. If not present or
     * value is not boolean then default value is returned.
     * @param properties properties where to search for property
     * @param propName name of the property
     * @param defaultValue default value for the property
     * @return property value or {@code defaultValue} if not present or not an integer.
     */
    public static boolean getBooleanProperty(Properties properties, String propName, boolean defaultValue) {
        String value = properties.getProperty(propName);
        if (value == null) {
            return defaultValue;
        }
        return value.equalsIgnoreCase(Boolean.TRUE.toString()) ||
                (!value.equalsIgnoreCase(Boolean.FALSE.toString()) && defaultValue);
    }

    /**
     * Returns {@link String} property with given name if present. If not present
     * or {@code allowEmpty} is {@code false} and propety is empty
     * then default value is returned.
     *
     * @param properties properties where to search for property
     * @param propName name of the property
     * @param allowEmpty {@code true} if empty string is allowed
     * @param defaultValue default value for the property
     * @return property value or {@code defaultValue} if above conditions are met.
     */
    public static String getStringProperty(Properties properties, String propName, boolean allowEmpty, String defaultValue) {
        String value = properties.getProperty(propName);
        if(value == null || (!allowEmpty && value.trim().isEmpty())) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Returns {@code double} property with given name if present. If not present or
     * value is not double then default value is returned.
     * @param properties properties where to search for property
     * @param propName name of the property
     * @param defaultValue default value for the property
     * @return property value or {@code defaultValue} if not present or not an integer.
     */
    public static double getDoubleProperty(Properties properties, String propName, double defaultValue) {
        String value = properties.getProperty(propName);
        if(value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("Property {} is not a number", propName);
            return defaultValue;
        }
    }
}
