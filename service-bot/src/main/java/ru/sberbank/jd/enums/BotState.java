package ru.sberbank.jd.enums;

/**
 * Перечисление состояний бота.
 */
public enum BotState {
    DEFAULT,                     // Состояние по умолчанию
    WAITING_NEW_USER_FIO,        // Ожидание ввода полного имени нового пользователя
    WAITING_START_DATE,          // Ожидание ввода даты начала отпуска
    WAITING_END_DATE,            // Ожидание ввода даты окончания отпуска
    WAITING_NEW_USER_TELEGRAMNAME,  // Ожидание ввода Telegram-имени нового пользователя
    WAITING_VACATION_TO_DELETE   // Ожидание выбора отпуска для удаления
}
