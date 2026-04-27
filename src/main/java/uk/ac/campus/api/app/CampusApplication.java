package uk.ac.campus.api.app;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS application bootstrap class.
 *
 * The @ApplicationPath annotation declares the versioned base URI for every
 * endpoint in this API. All resource classes discovered by Jersey's package
 * scanner will be reachable under /api/v1/...
 *
 * JAX-RS Lifecycle note (answered in report):
 * By default, JAX-RS creates a brand-new instance of each resource class for
 * every incoming HTTP request (per-request scope). This means instance fields
 * on resource classes are NOT shared between requests and cannot be used to
 * hold persistent state. To share data safely across requests, this project
 * uses a static Singleton registry (InMemoryRegistry) backed by
 * ConcurrentHashMap, which lives for the entire lifetime of the server.
 */
@ApplicationPath("/api/v1")
public class CampusApplication extends Application {
    // No explicit class registration needed — web.xml package scanning handles discovery
}
