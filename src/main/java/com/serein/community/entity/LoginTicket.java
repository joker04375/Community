package com.serein.community.entity;

import lombok.Data;

import java.util.Date;

@Data
public class LoginTicket {
    private Long id;
    private Long userId;
    private String ticket;
    private int status;
    private Date expired;
}
