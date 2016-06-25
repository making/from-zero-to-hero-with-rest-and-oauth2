package com.example;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Message {
    @Id
    @GeneratedValue
    public Integer id;
    public String text;
    @Temporal(TemporalType.TIMESTAMP)
    public Date createdAt;
}