package pro.kensait.jakartaee.issuer;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/*
 * Keycloak と同じ形のパスで、JWKS とトークンを返す
 *   GET  /jakarta-mpjwt-issuer/realms/learn/protocol/openid-connect/certs
 *   POST /jakarta-mpjwt-issuer/realms/learn/protocol/openid-connect/token（grant_type=password）
 */
@Path("/realms/learn/protocol/openid-connect")
@RequestScoped
public class OpenIdConnectResource {
    @Inject
    private TokenIssuer issuer;

    @GET
    @Path("/certs")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject certs() {
        return issuer.jwks();
    }

    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response token(@FormParam("grant_type") String grantType,
            @FormParam("username") String username, @FormParam("password") String password) {
        if (!"password".equals(grantType)) {
            return error(400, "unsupported_grant_type");
        }
        JsonObject tokens = issuer.issue(username, password);
        return tokens == null ? error(401, "invalid_grant") : Response.ok(tokens).build();
    }

    private Response error(int status, String error) {
        return Response.status(status).entity(Json.createObjectBuilder().add("error", error).build()).build();
    }
}
