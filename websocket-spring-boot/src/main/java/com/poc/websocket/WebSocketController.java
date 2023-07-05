package com.poc.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/hello")
    public void greeting(String message) {
        messagingTemplate.convertAndSend("/topic/greetings", "Hello, " + message + "!");
        messagingTemplate.convertAndSend("/alert/trigger", "");
    }
}
