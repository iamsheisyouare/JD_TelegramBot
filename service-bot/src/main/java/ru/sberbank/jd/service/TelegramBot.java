package ru.sberbank.jd.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.sberbank.jd.config.BotConfig;
import ru.sberbank.jd.config.IntegrationConfig;
import ru.sberbank.jd.dto.EmployeeResponse;
import ru.sberbank.jd.handler.ChatJoinRequestHandler;
import ru.sberbank.jd.handler.EmployeeApiHandler;
import ru.sberbank.jd.handler.VacationApiHandler;

import java.util.List;

@Slf4j
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

    private String telegramName;
    private String userFirstName;
    private String userLastName;

    private String inviteLink;

    private String adminToken;

    public TelegramBot(BotConfig botConfig, IntegrationConfig integrationConfig, RestTemplate restTemplate) {
        this.botConfig = botConfig;
        this.integrationConfig = integrationConfig;
        this.restTemplate = restTemplate;
        this.inviteLink = botConfig.getInviteLink();
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

        long chatId;
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            chatId = update.getMessage().getChatId();

            EmployeeApiHandler employeeApiHandler = new EmployeeApiHandler(restTemplate, integrationConfig);
            EmployeeResponse employeeResponse = new EmployeeResponse();
            EmployeeResponse adminResponse = new EmployeeResponse();
            VacationApiHandler vacationApiHandler = new VacationApiHandler(integrationConfig, restTemplate);

            telegramName = update.getMessage().getChat().getUserName();
            userFirstName = update.getMessage().getChat().getFirstName();
            userLastName = update.getMessage().getChat().getLastName();

            // TODO вынести выше чтобы каждый раз не запрашивать токен админа при каждом сообщении в чате !!!
            adminResponse = employeeApiHandler.getAdminInfo();
            adminToken = adminResponse.getToken();
            // TODO здесь брать не админский токен всегда а берем токен пользователя и передаем для создания нового - если тот не админ, то не сможет создать

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, telegramName);
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
                case "/newUser":
                    String userFIO = userFirstName + " " + userLastName;
                    employeeResponse = employeeApiHandler.createEmployee(telegramName, userFIO, adminToken);
                    prepareAndSendMessage(chatId, "Create new Employee | ID = " + employeeResponse.getId());
                    break;
                case "/vacations":
                    String vacationsMessage = vacationApiHandler.handleVacationsCommand(telegramName);
                    prepareAndSendMessage(chatId, vacationsMessage);
                    break;
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
    }


    private void startCommandReceived(long chatId, String name) {
        String answer = "Привет, " + name + ", рад приветствовать тебя в боте!";
        //log.info("Replied to user " + name );

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

    private void prepareAndSendMessage(long chatId, String textToSend){
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
