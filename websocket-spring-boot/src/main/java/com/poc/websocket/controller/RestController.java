package com.poc.websocket.controller;

import org.springframework.web.bind.annotation.GetMapping;

@org.springframework.web.bind.annotation.RestController
public class RestController {
    @GetMapping("/external")
    public String index() {
        return "external";
    }
}
