package com.ocds.access.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum EventType {

    TENDER("access"),

    AMENDMENT("amendment"),

    BUDGET("budget"),

    RELATED_NOTICE("related notice");

    @Getter
    private String text;
}
