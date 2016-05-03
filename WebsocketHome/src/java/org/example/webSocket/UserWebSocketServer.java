package org.example.websocket;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.server.ServerEndpoint;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.example.model.User;

@ApplicationScoped
@ServerEndpoint("/actions")
public class UserWebSocketServer {

    @Inject
    private UserSessionHandler sessionHandler;

    @OnOpen
    public void open(Session session) {
        sessionHandler.addSession(session);
    }

    @OnClose
    public void close(Session session) {
        sessionHandler.removeSession(session);
    }

    @OnError
    public void onError(Throwable error) {
        Logger.getLogger(UserWebSocketServer.class.getName()).log(Level.SEVERE, null, error);
    }

    @OnMessage
    public void handleMessage(String message, Session session) {

        try (JsonReader reader = Json.createReader(new StringReader(message))) {
            JsonObject jsonMessage = reader.readObject();

            if ("addUser".equals(jsonMessage.getString("action"))) {
                User user = new User();
                user.setName(jsonMessage.getString("name"));
                user.setStatus(jsonMessage.getString("status"));
                sessionHandler.addUser(user);
            }

            if ("remove".equals(jsonMessage.getString("action"))) {
                int id = (int) jsonMessage.getInt("id");
                sessionHandler.removeUser(id);
            }

            if ("toggle".equals(jsonMessage.getString("action"))) {
                int id = (int) jsonMessage.getInt("id");
                sessionHandler.toggleUser(id);
            }
        }
    }
}
