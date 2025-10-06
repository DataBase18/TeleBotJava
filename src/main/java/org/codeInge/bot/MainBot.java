package org.codeInge.bot;

import org.codeInge.command.*;
import org.codeInge.commandTexts.CommandAudioTexts;
import org.codeInge.utilities.Config;
import org.codeInge.utilities.GlobalConstants;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.function.Consumer;

public class MainBot extends CallbackQuery implements LongPollingSingleThreadUpdateConsumer {

    public static TelegramClient telegramClient;
    private Config config;

    public MainBot(Config config) {
        this.config = config;
        telegramClient = new OkHttpTelegramClient(config.getProperty(GlobalConstants.TelegramTokenProperty));
    }

    @Override
    public void consume(Update update) {
        String text = "";
        Long chatId = 0L;
        Consumer<Update> nextStep = null;



        if (update.hasMessage() ) {
            if (update.getMessage().hasText()){
                text = update.getMessage().getText();
            }
            chatId = update.getMessage().getChatId();
            nextStep =  ChatManager.getNextStep(chatId);
        }else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            nextStep =  ChatManager.getNextStep(chatId);
        }

        //Temporal security
        if (chatId != 1534171231){
            text = "---N/A---";
        }

        if (text.startsWith("/")){
            mainMenu(update);
        }else if (nextStep != null  ) {
            nextStep.accept(update);
        }
    }


    public void mainMenu(@NotNull Update update){
        String command = update.getMessage().getText();
        String[] split = command.split(" ");

        switch (split[0]) {
            case Commands.commandStart :
                CommandStart.instance.firstMessageAfterToEnter(update);
                break;
            case Commands.commandNotes:
                CommandNotes.instance.firstMessageAfterToEnter(update);
                break;
            case Commands.commandCamera:
                CommandCamera.instance.firstMessageAfterToEnter(update);
                break;
            case Commands.commandLog:
                CommandLog.instance.firstMessageAfterToEnter(update);
                break;
            case Commands.commandNetwork:
                CommandNetwork.instance.firstMessageAfterToEnter(update);
                break;
            case Commands.commandStatusCpu:
                CommandState.instance.firstMessageAfterToEnter(update);
                break;
            case Commands.commandAudioRecord:
                CommandAudio.instance.firstMessageAfterToEnter(update);
                break;
            case Commands.commandExecute:
                if(split.length == 1){ //Only command
                    CommandExecutor.instance.firstMessageAfterToEnter(update);
                }else{ //Command With params
                    CommandExecutor.instance.processCommand(update, split[1]);
                }
                break;
            default:
                CommandDefault.instance.firstMessageAfterToEnter(update);
                break;
        }
    }


    public static void sendMessageTo(SendMessage message){
        try {
            MainBot.telegramClient.execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }
    public static void editMessageTo(EditMessageText message){
        try {
            MainBot.telegramClient.execute(message);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void sendPhoto(SendPhoto photo){
        try {
            MainBot.telegramClient.execute(photo);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void deleteMessage(DeleteMessage deleteMessage){
        try {
            MainBot.telegramClient.execute(deleteMessage);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void sendAudioMessage(SendAudio audioMessage){
        try {
            MainBot.telegramClient.execute(audioMessage);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

}
