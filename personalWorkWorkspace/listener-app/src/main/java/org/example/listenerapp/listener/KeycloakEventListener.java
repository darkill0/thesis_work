package org.example.listenerapp.listener;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class KeycloakEventListener implements EventListenerProvider {




    @Override
    public void onEvent(Event event) {
        if (event.getType().name().equalsIgnoreCase("register")) {
            String userId = event.getUserId();
            String username = event.getDetails().get("username");

            sendNotification("Пользователь зарегистрирован: " + username);
            System.out.println("user");

        }
    }

    private void sendNotification(String message) {
        try {
            URL url = new URL("http://host.docker.internal:8081/notify"); // Укажите URL для получения уведомлений на хосте
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            String jsonInputString = "{\"message\": \"" + message + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
        if(adminEvent.getOperationType().name().equalsIgnoreCase("create")) {
            sendNotification("mustwork");
            System.out.println("admin");
        }
    }

    @Override
    public void close() {}
}
