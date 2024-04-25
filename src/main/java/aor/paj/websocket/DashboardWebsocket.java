package aor.paj.websocket;


import jakarta.ejb.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Singleton
@ServerEndpoint("/websocket/dashboard")
public class DashboardWebsocket {

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {

        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {

        sessions.remove(session);
    }

    public void notifyUserUpdatesForDashboard() {

        for (Session s : sessions) {
            try {
                s.getBasicRemote().sendText("New users info detected");
            } catch (IOException e) {
                System.out.println("Error sending message: " + e.getMessage());
            }
        }
    }

    public void notifyTaskUpdatesForDashboard() {

        for (Session s : sessions) {
            try {
                s.getBasicRemote().sendText("New tasks info detected");
            } catch (IOException e) {
                System.out.println("Error sending message: " + e.getMessage());
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {

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

        System.out.println("Error: " + throwable.getMessage());
    }
}

