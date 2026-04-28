package uk.ac.campus.api.resource;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getApiInfo(@Context UriInfo uriInfo) {
        String apiRoot = normaliseApiRoot(uriInfo.getAbsolutePath().toString());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("api", "Smart Campus Sensor and Room Management API");
        payload.put("version", "1.0.0");
        payload.put("description", "RESTful API for managing campus rooms and IoT sensors.");
        payload.put("timestamp", Instant.now().toString());
        payload.put("contact", contactDetails());
        payload.put("resources", resourceLinks(apiRoot));

        return Response.ok(payload).build();
    }

    private Map<String, String> contactDetails() {
        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("name", "Campus Facilities Team");
        contact.put("email", "admin@smartcampus.ac.uk");
        contact.put("department", "Estates and Facilities Management");
        return contact;
    }

    private Map<String, Object> resourceLinks(String apiRoot) {
        Map<String, Object> resources = new LinkedHashMap<>();
        resources.put("rooms", resource(apiRoot + "/rooms", "GET, POST",
                "List all rooms or create a new room"));
        resources.put("room", resource(apiRoot + "/rooms/{roomId}", "GET, DELETE",
                "Retrieve or decommission a specific room"));
        resources.put("sensors", resource(apiRoot + "/sensors", "GET, POST",
                "List sensors (supports ?type= filter) or register a new sensor"));
        resources.put("sensor", resource(apiRoot + "/sensors/{sensorId}", "GET, PUT, DELETE",
                "Retrieve, update, or remove a specific sensor by ID"));
        resources.put("readings", resource(apiRoot + "/sensors/{sensorId}/readings", "GET, POST",
                "Get or append historical readings for a sensor"));
        return resources;
    }

    private Map<String, String> resource(String href, String methods, String description) {
        Map<String, String> resource = new LinkedHashMap<>();
        resource.put("href", href);
        resource.put("methods", methods);
        resource.put("description", description);
        return resource;
    }

    private String normaliseApiRoot(String absolutePath) {
        if (absolutePath.endsWith("/")) {
            return absolutePath.substring(0, absolutePath.length() - 1);
        }
        return absolutePath;
    }
}

