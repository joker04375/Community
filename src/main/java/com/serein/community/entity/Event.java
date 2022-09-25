package com.serein.community.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Event {
    private String topic;
    private Long userId;
    private int entityType;
    private Long entityId;
    private Long entityUserId;

    private Map<String,Object> data = new HashMap<>();

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(String key,Object value) {
        this.data.put(key, value);
    }
}
