package pro.kensait.jakartaee.greet.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/greet")
@Produces(MediaType.TEXT_PLAIN)
public class GreetResource {
    @GET
    @Path("/hello/{name}")
    public Response sayHello(@PathParam("name") String name) {
        return Response.ok("Hello " + name + "!").build();
    }

    @GET
    @Path("/goodbye/{name}")
    public Response sayGoodbye(@PathParam("name") String name) {
        return Response.ok("Goodbye " + name + "!").build();
    }

    @GET
    @Path("/morning")
    public Response sayGoodMorning(@QueryParam("name") String name) {
        return Response.ok("Good Morning " + name + "!").build();
    }

    @POST
    @Path("/afternoon")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response sayGoodAfternoon(@FormParam("name") String name) {
        return Response.ok("Good Afternoon " + name + "!").build();
    }

    @POST
    @Path("/evening")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sayGoodEvening(GreetingRequest request) {
        return Response.ok("Good Evening " + request.name() + "!").build();
    }

    @GET
    @Path("/night")
    public Response sayGoodNight(@HeaderParam("name") String name) {
        return Response.ok("Good Night " + name + "!").build();
    }
}
