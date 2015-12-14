package cz.activecode.dl.templater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;

public abstract class TemplatedBean implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplatedBean.class);

    private String template = null;

    public final void setTemplate(String template) {
        this.template = template;
    }

    public final void setResourceFile(String resourceFile) {
        this.template = readResourceTemplate(resourceFile);
    }

    public final String getResourceFile() {
        return null;
    }

    public String getHtml() {
        String localTemplate;
        if(template != null) {
            localTemplate = template;
        } else {
            localTemplate = readResourceTemplate(this.getClass().getSimpleName() + ".html");
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(this.getClass());
            for(PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                Method readMethod = pd.getReadMethod();
                if(readMethod == null || "getHtml".equals(readMethod.getName())) {
                    continue;
                }

                String replace = "${" + pd.getName() + "}";
                String value = "";

                try {
                    value = String.valueOf(readMethod.invoke(this));
                } catch (IllegalAccessException e) {
                    LOGGER.warn("Cannot access " + this.getClass().getName() + "." + readMethod.getName(), e);
                } catch (InvocationTargetException e) {
                    LOGGER.warn("Cannot invoke " + this.getClass().getName() + "." + readMethod.getName(), e);
                }
                localTemplate = localTemplate.replace(replace, value);
            }
            return localTemplate;
        } catch (IntrospectionException e) {
            LOGGER.warn("Cannot find ActionRequestData fields", e);
            return localTemplate;
        }
    }

    private String readResourceTemplate(String resourceFile) {
        InputStream res = this.getClass().getResourceAsStream(resourceFile);
        if(res == null) {
            //LOGGER.warn("Template " + resourceFile + " not found");
            return "<!-- no template -->";
        }
        return new Scanner(res).useDelimiter("\\A").next();
    }

}
