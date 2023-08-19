package org.example.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EmployeeStatus {
    WORK("WORK"),
    FIRED("FIRED");

    private final String value;

}
