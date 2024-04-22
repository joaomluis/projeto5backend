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
@ServerEndpoint("/websocket/tasks")
public class TasksWebsocket {


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

    public void notifyTasksUpdated() {
        // Send the message to all connected clients
        for (Session s : sessions) {
            try {
                s.getBasicRemote().sendText("Task changed");
            } catch (IOException e) {
                System.out.println("Error sending message: " + e.getMessage());
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // A message was received from a client.
        // You would put your logic here to handle the task change.
        // For now, we'll just echo the message back to all connected clients.

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
