package aor.paj.websocket;
import aor.paj.bean.MessageBean;
import aor.paj.dto.Message;
import aor.paj.entity.MessageEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.json.*;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Singleton
@ServerEndpoint("/websocket/notifications/{token}")
public class NotificationsWebsocket {

    @EJB
    private MessageBean messageBean;

    ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    public Session getSessionByToken(String token) {
        return sessions.get(token);
    }


    @OnOpen
    public void toDoOnOpen(Session session, @PathParam("token") String token){
        System.out.println("A new WebSocket session is opened for client with token: "+ token);
        sessions.put(token,session);

        // Get the unread messages for the user
        List<Message> unreadMessages = messageBean.getUnreadMessages(token);

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        // Send the unread messages to the user
        for (Message message : unreadMessages) {

                JsonObject jsonObject = Json.createObjectBuilder()
                        .add("sender", message.getSender())
                        .add("timestamp", message.getSentTimestamp().toString())
                        .build();
                jsonArrayBuilder.add(jsonObject);

            }

            JsonArray jsonArrayNotifications = jsonArrayBuilder.build();

            try {
                session.getBasicRemote().sendText(jsonArrayNotifications.toString());
            } catch (IOException e) {
                System.out.println("Error sending message: " + e.getMessage());
            }


    }
    @OnClose
    public void toDoOnClose(Session session, CloseReason reason){
        System.out.println("Websocket session is closed with CloseCode: "+
                reason.getCloseCode() + ": "+reason.getReasonPhrase());
        sessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }

    @OnMessage
    public void toDoOnMessage(Session session, String msg){
        System.out.println("A new message is received: "+ msg);

        String sender;
        String recipient;
        String content;



        try {
            JsonObject jsonObject = Json.createReader(new StringReader(msg)).readObject();
            sender = jsonObject.getString("sender");
            recipient = jsonObject.getString("recipient");
            content = jsonObject.getString("content");

        } catch (JsonException e) {
            System.out.println("Error parsing JSON: " + e.getMessage());
            return;
        }

        messageBean.handleMessage(session, content, sender, recipient);



    }
}