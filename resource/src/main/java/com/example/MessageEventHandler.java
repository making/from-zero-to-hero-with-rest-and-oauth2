package com.example;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RepositoryEventHandler(Message.class)
public class MessageEventHandler {

    @HandleBeforeCreate
    public void beforeCreate(Message message) {
        message.createdAt = new Date();
    }
}