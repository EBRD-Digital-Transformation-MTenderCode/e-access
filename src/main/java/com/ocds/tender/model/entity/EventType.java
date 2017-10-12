package com.ocds.tender.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum EventType {

    TENDER("tender"),

    AMENDMENT("amendment"),

    BUDGET("budget"),

    RELATED_NOTICE("related notice");

    @Getter
    private String text;
}
