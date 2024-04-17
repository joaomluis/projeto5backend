package aor.paj.websocket;
import aor.paj.bean.MessageBean;
import aor.paj.entity.MessageEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;



@Singleton
@ServerEndpoint("/websocket/notifier/{token}")
public class Notifier {

    @EJB
    private MessageBean messageBean;

    HashMap<String, Session> sessions = new HashMap<String, Session>();


    public void send(String token, String msg){
        Session session = sessions.get(token);
        if (session != null){
            System.out.println("sending.......... "+msg);
            try {
                session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                System.out.println("Something went wrong!");
            }
        }
    }


    @OnOpen
    public void toDoOnOpen(Session session, @PathParam("token") String token){
        System.out.println("A new WebSocket session is opened for client with token: "+ token);
        sessions.put(token,session);
    }
    @OnClose
    public void toDoOnClose(Session session, CloseReason reason){
        System.out.println("Websocket session is closed with CloseCode: "+
                reason.getCloseCode() + ": "+reason.getReasonPhrase());
        for(String key:sessions.keySet()){
            if(sessions.get(key) == session)
                sessions.remove(key);
        }
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

        messageBean.createMessage(content, sender, recipient);


        try {
            session.getBasicRemote().sendText("ack");
        } catch (IOException e) {
            System.out.println("Something went wrong!");
        }
    }
}