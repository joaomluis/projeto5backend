package aor.paj.websocket;


import aor.paj.bean.MessageBean;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Singleton
@ServerEndpoint("/websocket/categories")
public class CategoryWebsocket {

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        // Add the new session to the set
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        // Remove the closed session from the set
        sessions.remove(session);
    }

    public void notifyCategoriesUpdated() {
        // Send the message to all connected clients
        for (Session s : sessions) {
            try {
                s.getBasicRemote().sendText("Category changed");
            } catch (IOException e) {
                System.out.println("Error sending message: " + e.getMessage());
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {


        // Send the message to all connected clients
        for (Session s : sessions) {
            try {
                s.getBasicRemote().sendText(message);
            } catch (IOException e) {
                System.out.println("Error sending message: " + e.getMessage());
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Handle error
        System.out.println("Error: " + throwable.getMessage());
    }
}

