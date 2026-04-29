package com.smartcampus.application;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Jersey application configuration loaded by Tomcat through WEB-INF/web.xml.
 */
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        packages("com.smartcampus.resource", "com.smartcampus.exception", "com.smartcampus.filter");
        register(JacksonFeature.class);
    }
}
