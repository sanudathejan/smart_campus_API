package uk.ac.campus.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery endpoint — GET /api/v1
 *
 * Returns API metadata including version, administrator contact, and a
 * hypermedia map of primary resource collections.
 *
 * HATEOAS rationale (answered in report):
 * Hypermedia As The Engine Of Application State (HATEOAS) embeds navigation
 * links directly inside API responses. Instead of relying on external static
 * documentation that may drift out of sync with the implementation, clients
 * discover available operations at runtime by following the links the server
 * provides. This decouples the client from hard-coded URL assumptions —
 * if the server restructures its routing, clients that navigate via links
 * continue to work without updates, because they always follow what the server
 * tells them rather than what a document told them yesterday.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getApiInfo() {

        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms",   "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name",        "Campus Room & Sensor Management API");
        payload.put("version",     "1.0");
        payload.put("status",      "ONLINE");
        payload.put("description", "RESTful API for managing university campus infrastructure");
        payload.put("contact",     "campus-admin@university.ac.uk");
        payload.put("resources",   resources);

        return Response.ok(payload).build();
    }
}
