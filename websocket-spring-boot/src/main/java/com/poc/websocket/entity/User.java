package com.poc.websocket.entity;

import java.security.Principal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User implements Principal {

    private String name;

}
