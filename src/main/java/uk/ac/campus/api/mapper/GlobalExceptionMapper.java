package uk.ac.campus.api.mapper;

import uk.ac.campus.api.model.ErrorResponse;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global catch-all exception mapper.
 *
 * Intercepts any Throwable not handled by a more specific mapper, ensuring the
 * API never leaks raw Java stack traces to external consumers.
 *
 * Cybersecurity rationale (answered in report):
 * A raw stack trace reveals internal class names, package structures, framework
 * versions (e.g. Jersey 2.41, Tomcat 9), and the exact line where a failure
 * occurred. Attackers use this information to search for known CVEs targeting
 * those specific library versions, to map the internal architecture for
 * targeted exploitation, and to craft payloads that trigger the exposed logic
 * paths. Hiding stack traces from responses (while logging them server-side)
 * is a fundamental defensive measure.
 *
 * JAX-RS built-in exceptions (WebApplicationException and subclasses such as
 * NotFoundException, NotAllowedException) already carry a meaningful status
 * code. This mapper preserves those codes rather than incorrectly forcing a
 * 500 on a 404 or 405.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {

        // Preserve intentional JAX-RS HTTP status codes (404, 405, 415, etc.)
        if (ex instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) ex;
            Response original = wae.getResponse();
            ErrorResponse body = new ErrorResponse(
                    original.getStatus(),
                    original.getStatusInfo().getReasonPhrase(),
                    ex.getMessage() != null ? ex.getMessage() : "HTTP " + original.getStatus());
            return Response.status(original.getStatus())
                    .entity(body)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Log the full trace server-side for debugging — never expose it to the client
        LOG.log(Level.SEVERE, "Unhandled server error: " + ex.getMessage(), ex);

        ErrorResponse body = new ErrorResponse(
                500,
                "Internal Server Error",
                "An unexpected error occurred. Please contact the system administrator.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

