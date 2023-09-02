package ru.sberbank.jd.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodBoolean;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.sberbank.jd.config.BotConfig;
import ru.sberbank.jd.config.IntegrationConfig;
import ru.sberbank.jd.dto.EmployeeResponse;
import ru.sberbank.jd.handler.ChatJoinRequestHandler;
import ru.sberbank.jd.handler.EmployeeApiHandler;
import ru.sberbank.jd.handler.VacationApiHandler;
import ru.sberbank.jd.model.User;
import ru.sberbank.jd.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Getter
@Component
public class TelegramBot extends TelegramLongPollingBot {

    /* TODO
        * ссылка на чат приглашение и ее обработка
        * отпуска
        * джоба с автоматическим удалением лишних
        *
     */


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
    private String userFirstName;
    private String userLastName;

    private String inviteLink;

    private String adminToken;

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
     * @return
     */
    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    /**
     * @param update
     */
    @Override
    public void onUpdateReceived(Update update) {

        Long chatId = null;
        long userId;
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            chatId = update.getMessage().getChatId();
            userId = update.getMessage().getFrom().getId();

            EmployeeApiHandler employeeApiHandler = new EmployeeApiHandler(restTemplate, integrationConfig);
            EmployeeResponse employeeResponse = new EmployeeResponse();
            EmployeeResponse adminResponse = new EmployeeResponse();
            VacationApiHandler vacationApiHandler = new VacationApiHandler(integrationConfig, restTemplate);

            telegramName = update.getMessage().getChat().getUserName();
            userFirstName = update.getMessage().getChat().getFirstName();
            userLastName = update.getMessage().getChat().getLastName();

            // TODO вынести выше чтобы каждый раз не запрашивать токен админа при каждом сообщении в чате !!!
//            adminResponse = employeeApiHandler.getAdminInfo();
//            adminToken = adminResponse.getToken();
            // TODO здесь брать не админский токен всегда а берем токен пользователя и передаем для создания нового - если тот не админ, то не сможет создать

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
                case "/userByName":
                    employeeResponse = employeeApiHandler.getEmployeeByTelegramName(telegramName);
                    prepareAndSendMessage(chatId, "Find by Name | ID = " + employeeResponse.getId() + " | token = " + employeeResponse.getToken());
                    break;
                case "/userById":
                    employeeResponse = employeeApiHandler.getEmployeeById(2L);
                    prepareAndSendMessage(chatId, "Find by ID | ID = " + employeeResponse.getId() + " | name = " + employeeResponse.getName() + " | status = " + employeeResponse.getStatus());
                    break;
                case "/newUser":        // TODO сделать создание под админом и фио передавать не из чата а запрашивать
                    String userFIO = userFirstName + " " + userLastName;
                    employeeResponse = employeeApiHandler.createEmployee(telegramName, userFIO);      // adminToken
                    if (employeeResponse != null) {
                        var user = userService.setEmployeeInfo(telegramName, employeeResponse.getToken(), userId, employeeResponse.getId());
                        prepareAndSendMessage(chatId, "Create new Employee | ID = " + employeeResponse.getId());
                    } else {
                        prepareAndSendMessage(chatId, "Сотрудник не был создан! У вас не хватает прав!");
                    }
                    break;
                case "/vacations":
                    String vacationsMessage = vacationApiHandler.handleVacationsCommand(telegramName);
                    prepareAndSendMessage(chatId, vacationsMessage);
                    break;
                case "/banUser":
                    banUser("@TestTelegramBot", userId);
                    break;
                case "/testListStatus":
                    prepareAndSendMessage(chatId, employeeApiHandler.getListEmployeeAndStatus().toString());
                default:
                    sendMessage(chatId, "Извините! Пока не поддерживается!");
                    break;
            }
        } else if (update.hasChatJoinRequest()) {
            ChatJoinRequest chatJoinRequest = update.getChatJoinRequest();
            chatId = chatJoinRequest.getChat().getId();
            ChatJoinRequestHandler chatJoinRequestHandler = new ChatJoinRequestHandler();
            chatJoinRequestHandler.processChatJoinRequest(this, chatJoinRequest);
        }
        if (chatId != null) {
            chatIdSet.add(chatId.toString());
        }
    }

    private void banUser(String chatId, long userID) {
        BotApiMethodBoolean chatMemberBanOrUnban = null;        // TODO переименовать
        userID = 1920004508L;
        chatId = "-1001917485473";
        chatMemberBanOrUnban = new BanChatMember(chatId, userID);
        log.info("ban user id = " + userID + ", chat id = " + chatId + " result = " + chatMemberBanOrUnban.toString());

        try{
             Boolean result = execute(chatMemberBanOrUnban);
             log.info("result = " + result.toString());
        }
        catch (TelegramApiException e){
            log.error(ERROR_TEXT + e.getMessage());
        }
    }


    private void startCommandReceived(long chatId, String name, long userId) {
        String answer = "Привет, " + name + ", рад приветствовать тебя в боте!";
        //log.info("Replied to user " + name );
        if (userService.getByTelegramName(name).isEmpty()){
            var user = new User(name, userId);
            userRepository.save(user);
        }

        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try{
            execute(message);
        }
        catch (TelegramApiException e){
            //log.error(ERROR_TEXT + e.getMessage());
        }

    }

    private void executeMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
            //log.error(ERROR_TEXT + e.getMessage());
        }
    }

    public void prepareAndSendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    private void sendInviteLink(long chatId) {
        SendMessage message = new SendMessage();
        prepareAndSendMessage(chatId, "Привет! Вступите в чат сотрудников по ссылке: " + inviteLink);
    }



}
