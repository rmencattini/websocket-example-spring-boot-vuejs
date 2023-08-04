package com.poc.websocket.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/hello")
    public void greeting(@Payload String message, Principal principal){
        messagingTemplate.convertAndSend("/topic/greetings", "Hello, " + message + "!");
        messagingTemplate.convertAndSend("/alert/trigger", "");
        log.warn("\n\nUsername: " + principal.getName());
    }
}
