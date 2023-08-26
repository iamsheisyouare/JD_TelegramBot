package ru.sberbank.jd.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EmployeeStatus {
    WORK("WORK"),
    FIRED("FIRED");

    private final String value;

}
