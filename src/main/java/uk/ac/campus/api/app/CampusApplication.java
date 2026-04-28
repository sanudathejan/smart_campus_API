package uk.ac.campus.api.app;

import jakarta.ws.rs.core.Application;

/**
 * Lightweight JAX-RS bootstrap class.
 *
 * The public API base path is configured in web.xml so the Tomcat servlet
 * mapping and the Jersey resource model stay in sync on Tomcat 10.
 */
public class CampusApplication extends Application {
}
