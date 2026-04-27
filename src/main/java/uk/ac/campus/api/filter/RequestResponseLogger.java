package uk.ac.campus.api.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Cross-cutting observability filter that logs every HTTP request and response.
 *
 * Implements both ContainerRequestFilter (pre-processing) and
 * ContainerResponseFilter (post-processing) in a single class, keeping
 * logging logic centralised.
 *
 * Why use a JAX-RS filter instead of Logger.info() in every resource method?
 * (answered in report)
 * Inserting logging statements into each resource method violates the
 * Single Responsibility Principle and the DRY principle. With 15+ endpoints,
 * a developer could easily forget to add logging to a new route. A filter
 * runs automatically for every request without any code in the resource
 * classes — logging is guaranteed on all current and future endpoints from
 * a single place. It also makes it trivial to swap logging strategies
 * (format changes, log level changes, correlation IDs) without touching
 * any business logic.
 */
@Provider
public class RequestResponseLogger
        implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG =
            Logger.getLogger(RequestResponseLogger.class.getName());

    /** Runs before the request reaches a resource method. */
    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        LOG.info(String.format(">> %s %s",
                req.getMethod(),
                req.getUriInfo().getRequestUri()));
    }

    /** Runs after the resource method returns a response. */
    @Override
    public void filter(ContainerRequestContext req,
                       ContainerResponseContext resp) throws IOException {
        LOG.info(String.format("<< %d %s  [%s %s]",
                resp.getStatus(),
                labelFor(resp.getStatus()),
                req.getMethod(),
                req.getUriInfo().getPath()));
    }

    private String labelFor(int code) {
        switch (code) {
            case 200: return "OK";
            case 201: return "Created";
            case 204: return "No Content";
            case 400: return "Bad Request";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 409: return "Conflict";
            case 415: return "Unsupported Media Type";
            case 422: return "Unprocessable Entity";
            case 500: return "Internal Server Error";
            default:  return "";
        }
    }
}
