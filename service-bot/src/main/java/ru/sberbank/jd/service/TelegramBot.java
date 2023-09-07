package ru.sberbank.jd.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodBoolean;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.sberbank.jd.config.BotConfig;
import ru.sberbank.jd.config.IntegrationConfig;
import ru.sberbank.jd.dto.EmployeeResponse;
import ru.sberbank.jd.enums.BotState;
import ru.sberbank.jd.handler.ChatJoinRequestHandler;
import ru.sberbank.jd.handler.EmployeeApiHandler;
import ru.sberbank.jd.handler.VacationApiHandler;
import ru.sberbank.jd.model.User;
import ru.sberbank.jd.repository.UserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
@Getter
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig botConfig;

    final IntegrationConfig integrationConfig;

    static final String HELP_TEXT = "Этот бот создан для помощи сотрудникам.\n\n" +
            "Вы можете выбрать команды в меню или напечатать:\n\n" +
            "/start - приветственное сообщение\n\n" +
            "/chat - доступ в чат сотрудников\n\n" +
            "/vacations - отображает текущий график отпусков пользователя\n\n" +
            "/new_vacation - заявка на новый отпуск\n\n" +
            "/help - отображает данное сообщение";

    static final String ERROR_TEXT = "Error occurred: ";
    private final RestTemplate restTemplate;
    private final UserService userService;
    private final UserRepository userRepository;

    private Set<String> chatIdSet = new HashSet<>();

    private String telegramName;
    private String telegramUserName;
    private String userFIO;

    private Long employeeId;

    private String inviteLink;

    private Map<String, BotState> botStateMap = new HashMap<>();
    private Map<String, LocalDate> userStartDateMap = new HashMap<>();

    public TelegramBot(BotConfig botConfig, IntegrationConfig integrationConfig, RestTemplate restTemplate, UserService userService, UserRepository userRepository) {
        this.botConfig = botConfig;
        this.integrationConfig = integrationConfig;
        this.restTemplate = restTemplate;
        this.inviteLink = botConfig.getInviteLink();
        this.userService = userService;
        this.userRepository = userRepository;
        this.chatIdSet.add(botConfig.getEmployeeChatId().toString());
    }

    /**
     * Получает имя бота для Telegram.
     *
     * @return имя бота
     */
    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    /**
     * Получает токен бота для Telegram.
     *
     * @return токен бота
     */
    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    /**
     * Обрабатывает полученное обновление от Telegram.
     *
     * @param update обновление от Telegram
     */
    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = null;
        long userId;

        EmployeeApiHandler employeeApiHandler = new EmployeeApiHandler(restTemplate, integrationConfig, userService);
        VacationApiHandler vacationApiHandler = new VacationApiHandler(integrationConfig, restTemplate);

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            chatId = update.getMessage().getChatId();
            userId = update.getMessage().getFrom().getId();

            // Проверка состояний пользователя
            if (botStateMap.containsKey(telegramName)) {
                handleUserState(chatId, telegramName, messageText);
                return; // Важно вернуться после обработки состояния пользователя
            }

            telegramName = update.getMessage().getChat().getUserName();
            employeeId = userService.getByTelegramName(telegramName).get().getEmployeeId();


            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, telegramName, userId);
                    break;
                case "/help":
                    prepareAndSendMessage(chatId, HELP_TEXT);
                    break;
                case "/join":
                    sendInviteLink(chatId);
                    break;
                case "/newUser":
                    prepareAndSendMessage(chatId, "Введите ФИО сотрудника:");
                    botStateMap.put(telegramName, BotState.WAITING_NEW_USER_FIO);
                    break;
                case "/vacations":
                    String vacationsMessage = vacationApiHandler.handleVacationsCommand(employeeId);
                    prepareAndSendMessage(chatId, vacationsMessage);
                    break;
                case "/new_vacation":
                    prepareAndSendMessage(chatId, "Введите дату начала отпуска в формате dd.MM.yyyy:");
                    botStateMap.put(telegramName, BotState.WAITING_START_DATE);
                    break;
                case "/delete_vacation":
                    handleDeleteVacationCommand(chatId, vacationApiHandler);
                    break;
                default:
                    if (!chatId.toString().equals(botConfig.getEmployeeChatId().toString())) {
                        sendMessage(chatId, "Извините! Пока не поддерживается!");
                    }
                    break;
            }
        } else if (update.hasChatJoinRequest()) {
            handleChatJoinRequest(update, employeeApiHandler);
        }
        if (chatId != null) {
            chatIdSet.add(chatId.toString());
        }
    }

    /**
     * Обрабатывает команду /delete_vacation.
     *
     * @param chatId идентификатор чата
     */
    private void handleDeleteVacationCommand(Long chatId, VacationApiHandler vacationApiHandler) {
        botStateMap.put(telegramName, BotState.WAITING_VACATION_TO_DELETE);

        ReplyKeyboardMarkup keyboardMarkup = vacationApiHandler.getVacationButtons(employeeId);
        if (keyboardMarkup != null) {
            prepareAndSendKeyboard(chatId, "Выберите отпуск для удаления:", keyboardMarkup);
        } else {
            sendMessage(chatId, "Нет доступных отпусков для удаления.");
            // Сбросьте состояние
            botStateMap.remove(telegramName);
        }
    }

    /**
     * Обрабатывает запрос на присоединение к чату.
     *
     * @param update обновление от Telegram
     */
    private void handleChatJoinRequest(Update update, EmployeeApiHandler employeeApiHandler) {
        ChatJoinRequest chatJoinRequest = update.getChatJoinRequest();
        Long chatId = chatJoinRequest.getChat().getId();
        telegramName = chatJoinRequest.getUser().getUserName();

        Boolean userFound = (employeeApiHandler.getEmployeeByTelegramName(telegramName, integrationConfig.getAdminLogin()) != null);
        ChatJoinRequestHandler chatJoinRequestHandler = new ChatJoinRequestHandler();
        chatJoinRequestHandler.processChatJoinRequest(this, chatJoinRequest, userFound);
    }

    /**
     * Обрабатывает получение команды /start.
     *
     * @param chatId идентификатор чата
     * @param name   имя пользователя
     * @param userId идентификатор пользователя
     */
    private void startCommandReceived(long chatId, String name, long userId) {
        String answer = "Привет, " + name + ", рад приветствовать тебя в боте!";

        if (userService.getByTelegramName(name).isEmpty()) {
            var user = new User(name, userId);
            userRepository.save(user);
        }

        if (userService.getByTelegramName(name).get().getTelegramUserId() == null) {
            userService.setTelegramUserId(name, userId);
        }

        sendMessage(chatId, answer);
    }

    /**
     * Отправляет сообщение пользователю по указанному идентификатору чата.
     *
     * @param chatId     идентификатор чата
     * @param textToSend текст сообщения
     */
    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    /**
     * Выполняет отправку сообщения.
     *
     * @param message сообщение для отправки
     */
    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    /**
     * Подготавливает и отправляет сообщение пользователю по указанному идентификатору чата.
     *
     * @param chatId     идентификатор чата
     * @param textToSend текст сообщения
     */
    public void prepareAndSendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    /**
     * Подготавливает и отправляет клавиатуру пользователю по указанному идентификатору чата.
     *
     * @param chatId         идентификатор чата
     * @param textToSend     текст сообщения
     * @param keyboardMarkup разметка клавиатуры
     */
    private void prepareAndSendKeyboard(long chatId, String textToSend, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setReplyMarkup(keyboardMarkup);
        executeMessage(message);
    }

    /**
     * Удаляет клавиатуру и отправляет сообщение пользователю по указанному идентификатору чата.
     *
     * @param chatId     идентификатор чата
     * @param textToSend текст сообщения
     */
    private void removeKeyboard(long chatId, String textToSend) {
        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        keyboardRemove.setRemoveKeyboard(true);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setReplyMarkup(keyboardRemove);
        executeMessage(message);
    }

    /**
     * Отправляет пригласительную ссылку пользователю по указанному идентификатору чата.
     *
     * @param chatId идентификатор чата
     */
    private void sendInviteLink(long chatId) {
        prepareAndSendMessage(chatId, "Привет! Вступите в чат сотрудников по ссылке: " + inviteLink);
    }

    /**
     * Обрабатывает текущее состояние пользователя.
     *
     * @param chatId       идентификатор чата
     * @param telegramName имя пользователя в Telegram
     * @param messageText  текст сообщения
     */
    private void handleUserState(long chatId, String telegramName, String messageText) {
        BotState currentState = botStateMap.get(telegramName);
        VacationApiHandler vacationApiHandler = new VacationApiHandler(integrationConfig, restTemplate);

        switch (currentState) {
            case WAITING_START_DATE:
                handleStartDateInput(chatId, telegramName, messageText);
                break;
            case WAITING_END_DATE:
                handleEndDateInput(chatId, telegramName, messageText);
                break;
            case WAITING_VACATION_TO_DELETE:
                Long vacationId = vacationApiHandler.getVacationIdByText(messageText, employeeId);
                if (vacationId != null) {
                    handleDeleteVacationCommand(vacationId, chatId);
                } else {
                    sendMessage(chatId, "Ошибка при выборе отпуска.");
                    botStateMap.remove(telegramName);
                }
                removeKeyboard(chatId, "Выберите следующую команду");
                botStateMap.remove(telegramName);
                break;
            case WAITING_NEW_USER_FIO:
                userFIO = messageText;

                prepareAndSendMessage(chatId, "Введите TelegramName сотрудника:");
                botStateMap.put(telegramName, BotState.WAITING_NEW_USER_TELEGRAMNAME);
                break;
            case WAITING_NEW_USER_TELEGRAMNAME:
                telegramUserName = messageText;
                EmployeeApiHandler employeeApiHandler = new EmployeeApiHandler(restTemplate, integrationConfig, userService);
                EmployeeResponse employeeResponse = new EmployeeResponse();

                if (employeeApiHandler.getEmployeeByTelegramName(telegramUserName, integrationConfig.getAdminLogin()) != null) {
                    prepareAndSendMessage(chatId, "Сотрудник не был создан, т.к. сотрудник с логином = '" + telegramUserName + "' уже существует!");
                    break;
                }
                employeeResponse = employeeApiHandler.createEmployee(telegramUserName, userFIO, telegramName);
                if (employeeResponse != null) {
                    var user = userService.setEmployeeInfo(telegramUserName, employeeResponse.getToken(), employeeResponse.getId());
                    prepareAndSendMessage(chatId, "Создан новый пользователь | ID = " + user.getId());
                } else {
                    prepareAndSendMessage(chatId, "Сотрудник не был создан! У вас не хватает прав!");
                }

                // Очищаем состояния и мапу ФИО
                botStateMap.remove(telegramName);

                break;
        }
    }

    /**
     * Обрабатывает команду удаления отпуска.
     *
     * @param vacationId идентификатор отпуска
     * @param chatId     идентификатор чата
     * @return ответ на удаление отпуска
     */
    public String handleDeleteVacationCommand(long vacationId, long chatId) {
        VacationApiHandler vacationApiHandler = new VacationApiHandler(integrationConfig, restTemplate);
        String deleteResponse = vacationApiHandler.deleteVacation(vacationId);
        prepareAndSendMessage(chatId, deleteResponse);
        return deleteResponse;
    }

    /**
     * Обрабатывает ввод даты начала отпуска.
     *
     * @param chatId       идентификатор чата
     * @param telegramName имя пользователя в Telegram
     * @param messageText  текст сообщения с датой начала отпуска в формате "dd.MM.yyyy"
     */
    private void handleStartDateInput(long chatId, String telegramName, String messageText) {
        LocalDate startDate;
        try {
            startDate = LocalDate.parse(messageText, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (DateTimeParseException e) {
            prepareAndSendMessage(chatId, "Некорректный формат даты. Попробуйте еще раз.");
            return;
        }

        prepareAndSendMessage(chatId, "Введите дату окончания отпуска в формате dd.MM.yyyy:");

        // Обновляем состояние бота для ожидания даты окончания отпуска
        botStateMap.put(telegramName, BotState.WAITING_END_DATE);

        // Сохраняем дату начала отпуска в мапе (или другом месте, зависит от логики) для дальнейшего использования
        userStartDateMap.put(telegramName, startDate);
    }

    /**
     * Обрабатывает ввод даты окончания отпуска.
     *
     * @param chatId       идентификатор чата
     * @param telegramName имя пользователя в Telegram
     * @param messageText  текст сообщения с датой окончания отпуска в формате "dd.MM.yyyy"
     */
    private void handleEndDateInput(long chatId, String telegramName, String messageText) {
        LocalDate endDate;
        try {
            endDate = LocalDate.parse(messageText, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (DateTimeParseException e) {
            prepareAndSendMessage(chatId, "Некорректный формат даты. Попробуйте еще раз.");
            return;
        }

        // Получаем сохраненную дату начала отпуска
        LocalDate savedStartDate = userStartDateMap.get(telegramName);

        if (endDate.isBefore(savedStartDate)) {
            prepareAndSendMessage(chatId, "Дата окончания отпуска должна быть позже даты начала. Попробуйте еще раз.");
            return;
        }

        Long employeeId = getEmployeeIdByTelegramName(telegramName);

        // Обработка дат начала и окончания отпуска
        VacationApiHandler vacationApiHandler = new VacationApiHandler(integrationConfig, restTemplate);
        String addVacationResponse = vacationApiHandler.addVacation(employeeId, savedStartDate, endDate);
        prepareAndSendMessage(chatId, addVacationResponse);

        // Убираем состояние из мапы, так как диалог закончен
        botStateMap.remove(telegramName);
        userStartDateMap.remove(telegramName);
    }

    /**
     * Получает идентификатор сотрудника по его имени в Telegram.
     *
     * @param telegramName имя пользователя в Telegram
     * @return идентификатор сотрудника или null, если пользователь не найден
     */
    private Long getEmployeeIdByTelegramName(String telegramName) {
        Optional<User> userOptional = userService.getByTelegramName(telegramName);
        return userOptional.map(User::getEmployeeId).orElse(null);
    }
}
