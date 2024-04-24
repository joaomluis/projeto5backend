package aor.paj.service;



import aor.paj.bean.DashboardBean;
import aor.paj.bean.UserBean;

import jakarta.inject.Inject;
import jakarta.json.JsonObject;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/dashboard")
public class DashboardService {


    @Inject
    UserBean userBean;


    @Inject
    DashboardBean dashboardBean;



    @GET
    @Path("/users-stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatistics(@HeaderParam("token") String token) {
        Response response;

        if (userBean.getUserByToken(token) == null) {
            response = Response.status(403).entity("Invalid token").build();

        } else {
            JsonObject statistics = dashboardBean.getUsersDataAsJson(token);
            response = Response.status(200).entity(statistics).build();
        }

        return response;
    }

    @GET
    @Path("/tasks-stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasksStatistics(@HeaderParam("token") String token) {
        Response response;

        if (userBean.getUserByToken(token) == null) {
            response = Response.status(403).entity("Invalid token").build();

        } else {
            JsonObject statistics = dashboardBean.getTasksDataAsJson(token);
            response = Response.status(200).entity(statistics).build();
        }

        return response;
    }


}
