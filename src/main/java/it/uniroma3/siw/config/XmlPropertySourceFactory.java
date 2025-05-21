package it.uniroma3.siw.config;

import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.Properties;

public class XmlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        Properties props = new Properties();
        props.loadFromXML(resource.getInputStream());
        String sourceName = name != null ? name : resource.getResource().getFilename();
        if (sourceName == null || sourceName.isEmpty()) {
            sourceName = "xmlProperties";
        }
        return new PropertiesPropertySource(sourceName, props);
    }
}