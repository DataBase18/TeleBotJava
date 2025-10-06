package org.codeInge.command;

import org.codeInge.bot.ChatManager;
import org.codeInge.bot.MainBot;
import org.codeInge.commandTexts.CommandExecutorTexts;
import org.codeInge.utilities.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandExecutor extends Command {
    public static CommandExecutor instance = new CommandExecutor();
    @Override
    public void firstMessageAfterToEnter(Update update) {
        Long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }


        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(CommandExecutorTexts.WELCOME_TEXT)
                .build();

        MainBot.sendMessageTo(message);
        ChatManager.registerNextStep(chatId, this::handleCommandInput);
    }

    @Override
    public void processButtons(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
    }

    public void handleCommandInput(Update update) {
        String command = update.getMessage().getText();
        processCommand(update,  command);
    }

    public void processCommand(Update update, String command) {
        Long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }

        StringBuilder result = new StringBuilder();
        boolean success = false;
        try {
            Process proceso = Runtime.getRuntime().exec(new String[]{"cmd", "/c", command});
            BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            success = true;
        } catch (Exception e) {
            Logger.saveLog(Logger.formatLog(e.getMessage()));
            Logger.saveLog(Logger.formatLog(e.getCause().getMessage()));
            Logger.saveLog(Logger.formatLog(e.getLocalizedMessage()));
            Logger.saveLog("---------------------------------");
        }
        SendMessage response = SendMessage.builder()
                .chatId(chatId.toString())
                .text(success ?   result.toString() : "Failded to execute")
                .build();

        MainBot.sendMessageTo(response);
        CommandStart.instance.firstMessageAfterToEnter(update);
    }
}