package cz.activecode.dl.to;

import cz.activecode.dl.templater.TemplatedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;

public abstract class AbstractActionRequestData extends TemplatedBean implements ActionRequestData {

    private String id = IdFactory.newId();

    public String getId() {
        return id;
    }

}
