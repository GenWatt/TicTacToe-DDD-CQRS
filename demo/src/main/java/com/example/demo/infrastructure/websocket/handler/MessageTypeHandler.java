package com.example.demo.infrastructure.websocket.handler;

import com.example.demo.infrastructure.websocket.message.MessageType;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageTypeHandler {
    MessageType value();
}
