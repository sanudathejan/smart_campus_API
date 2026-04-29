package com.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Part 1.2 - Discovery endpoint
 * GET /api/v1 returns API metadata with HATEOAS-style resource links.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover(@Context UriInfo uriInfo) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        response.put("api", "Smart Campus Sensor and Room Management API");
        response.put("version", "1.0.0");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");
        response.put("timestamp", Instant.now().toString());

        Map<String, String> contact = new LinkedHashMap<String, String>();
        contact.put("name", "Campus Facilities Team");
        contact.put("email", "admin@smartcampus.ac.uk");
        contact.put("department", "Estates and Facilities Management");
        response.put("contact", contact);

        Map<String, Object> resources = new LinkedHashMap<String, Object>();
        resources.put("rooms", buildLink(uriInfo, "rooms", "GET, POST",
                "List all rooms or create a new room"));
        resources.put("room", buildLink(uriInfo, "rooms/{roomId}", "GET, DELETE",
                "Retrieve or decommission a specific room"));
        resources.put("sensors", buildLink(uriInfo, "sensors", "GET, POST",
                "List sensors (supports ?type= filter) or register a new sensor"));
        resources.put("sensor", buildLink(uriInfo, "sensors/{sensorId}", "GET",
                "Retrieve a specific sensor by ID"));
        resources.put("readings", buildLink(uriInfo, "sensors/{sensorId}/readings", "GET, POST",
                "Get or append historical readings for a sensor"));
        response.put("resources", resources);

        return Response.ok(response).build();
    }

    private Map<String, Object> buildLink(UriInfo uriInfo, String relativePath,
                                          String methods, String description) {
        Map<String, Object> link = new LinkedHashMap<String, Object>();
        link.put("href", buildHref(uriInfo, relativePath));
        link.put("methods", methods);
        link.put("description", description);
        return link;
    }

    private String buildHref(UriInfo uriInfo, String relativePath) {
        String baseUri = uriInfo.getBaseUri().toString();
        if (!baseUri.endsWith("/")) {
            baseUri = baseUri + "/";
        }
        return baseUri + relativePath;
    }
}
