package ru.sberbank.jd.enums;

import lombok.RequiredArgsConstructor;

/**
 * Перечисление статусов сотрудника.
 */
@RequiredArgsConstructor
public enum EmployeeStatus {
    WORK("WORK"),     // Статус "Работает"
    FIRED("FIRED");   // Статус "Уволен"

    private final String value;

}
