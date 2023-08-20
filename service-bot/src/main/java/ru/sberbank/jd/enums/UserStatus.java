package ru.sberbank.jd.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE("ACTIVE"),
    DELETED("DELETED");
    //REQUESTED("REQUESTED");

    private final String value;
}
