package org.example.listenerapp.listener;

import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.example.listenerapp.listener.*;

@Component
public class KeycloakEventListenerProviderFactory implements EventListenerProviderFactory {


    public KeycloakEventListenerProviderFactory() {
    }

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        // Создание нового экземпляра KeycloakEventListener с необходимыми зависимостями
        return new KeycloakEventListener();
    }

    @Override
    public void init(org.keycloak.Config.Scope config) {
        // Инициализация, если необходимо
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Пост-инициализация, если необходимо
    }

    @Override
    public void close() {
        // Закрытие ресурсов, если необходимо
    }

    @Override
    public String getId() {
        return "custom-event-listener-2"; // Уникальный ID вашего listener'а
    }
    

}
