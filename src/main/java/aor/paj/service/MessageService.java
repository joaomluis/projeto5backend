package aor.paj.service;

import aor.paj.bean.MessageBean;
import aor.paj.bean.UserBean;
import aor.paj.dto.Message;
import aor.paj.dto.User;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/messages")
public class MessageService {

    @EJB
    MessageBean messageBean;

    @EJB
    UserBean userBean;


    @GET
    @Path("/{sender}/{recipient}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllMessages(@HeaderParam("token") String token, @PathParam("sender") String sender, @PathParam("recipient") String recipient) {
        List<Message> messages;

        User user = userBean.getUserByToken(token);
        User recipientUser = userBean.getUserByUsername(recipient);

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token").build();
        } else if (recipientUser == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Recipient not found").build();
        } else {
            messages = messageBean.getMessages(sender, recipient);
            messageBean.markMessagesAsRead(sender, recipient);
        }


        return Response.ok(messages).build();
    }


}
