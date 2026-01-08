package com.project.jobportal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

//конфигурация WebSocket (STOMP)
//создайте новый класс конфигурации для включения WebSockets и настройки брокера сообщений.
@Configuration
@EnableWebSocketMessageBroker // Включает обработку сообщений WebSocket на основе брокера
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // префикс для всех исходящих сообщений (от сервера к клиенту).
        // /topic/ или /user/ будут использоваться для отправки уведомлений.
        config.enableSimpleBroker("/topic", "/queue"); // было: config.enableSimpleBroker("/topic", "/user"); стало (Добавьте "/queue"):

        // префикс для сообщений, отправляемых от клиента на сервер(например, /app/send-message).
        config.setApplicationDestinationPrefixes("/app");

        // включение префикса /user для личных сообщений (User-Specific Messaging)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // регистрация WebSocket endpoint, к которому будет подключаться клиент.
        // SockJS используется для обеспечения совместимости со старыми браузерами.
        registry.addEndpoint("/ws").withSockJS();
    }
}