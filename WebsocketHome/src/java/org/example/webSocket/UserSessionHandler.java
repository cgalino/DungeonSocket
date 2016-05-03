package org.example.websocket;

import java.io.IOException;
import java.util.ArrayList;
import javax.enterprise.context.ApplicationScoped;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.websocket.Session;
import org.example.model.User;

@ApplicationScoped
public class UserSessionHandler {

    private int userId = 0;
    private final Set sessions = new HashSet<>();
    private final Set users = new HashSet<>();
    
    public void addSession(Session session) {
        sessions.add(session);
        users.stream().map((user) -> createAddMessage((User) user)).forEach((addMessage) -> {
            sendToSession(session, (JsonObject) addMessage);
        });
    }

    public void removeSession(Session session) {
        sessions.remove(session);
    }
    
    public List getUsers() {
        return new ArrayList<>(users);
    }

    public void addUser(User user) {
        System.out.println("entra addUser sessionhandler");
        user.setId(userId);
        users.add(user);
        userId++;
        JsonObject addMessage = createAddMessage(user);
        sendToAllConnectedSessions(addMessage);
    }

    public void removeUser(int id) {
      User user = getUserById(id);
        if (user != null) {
            users.remove(user);
            JsonProvider provider = JsonProvider.provider();
            JsonObject removeMessage = provider.createObjectBuilder()
                    .add("action", "remove")
                    .add("id", id)
                    .build();
            sendToAllConnectedSessions(removeMessage);
        }
    }

    public void toggleUser(int id) {
        JsonProvider provider = JsonProvider.provider();
        User user = getUserById(id);
        if (user != null) {
            if ("On".equals(user.getStatus())) {
                user.setStatus("Off");
            } else {
                user.setStatus("On");
            }
            JsonObject updateDevMessage = provider.createObjectBuilder()
                    .add("action", "toggle")
                    .add("id", user.getId())
                    .add("status", user.getStatus())
                    .build();
            sendToAllConnectedSessions(updateDevMessage);
        }
    }

    private User getUserById(int id) {
        User user;
        for (Iterator it = users.iterator(); it.hasNext();) {
            user = (User) it.next();
            if (user.getId() == id) {
                return (User) user;
            }
        }
        return null;
    }

    private JsonObject createAddMessage(User user) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject addMessage = provider.createObjectBuilder()
                .add("action", "addUser")
                .add("id", user.getId())
                .add("name", user.getName())
                .add("status", user.getStatus())
                .build();
        return addMessage;
    }

    private void sendToAllConnectedSessions(JsonObject message) {
        sessions.stream().forEach((session) -> {
            sendToSession((Session) session, message);
        });
    }

    private void sendToSession(Session session, JsonObject message) {
        try {
            session.getBasicRemote().sendText(message.toString());
        } catch (IOException ex) {
            sessions.remove(session);
            Logger.getLogger(UserSessionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
